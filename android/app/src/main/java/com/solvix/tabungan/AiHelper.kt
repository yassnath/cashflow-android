package com.solvix.tabungan

import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.serialization.decodeFromString
import kotlin.math.roundToInt

fun insightsStorageKey(userId: String): String = "insights_cache_$userId"

fun persistInsightsCacheToPrefs(
  userId: String,
  items: List<InsightItem>,
  securePrefs: SharedPreferences,
  localJson: Json,
) {
  if (userId.isBlank()) return
  val jsonStr = runCatching { localJson.encodeToString(items) }.getOrDefault("[]")
  securePrefs.edit { putString(insightsStorageKey(userId), jsonStr) }
}

fun loadInsightsCacheFromPrefs(
  userId: String,
  securePrefs: SharedPreferences,
  localJson: Json,
): List<InsightItem> {
  if (userId.isBlank()) return emptyList()
  val jsonStr = securePrefs.getString(insightsStorageKey(userId), "[]") ?: "[]"
  return runCatching { localJson.decodeFromString<List<InsightItem>>(jsonStr) }.getOrDefault(emptyList())
}

/**
 * Builds a compact app-level context string for the AI assistant.
 */
fun buildAiAppContext(currentLang: AppLanguage, currentTheme: ThemeName): String = buildString {
  appendLine("app=CashFlow Android")
  appendLine("language=${if (currentLang == AppLanguage.ID) "ID" else "EN"}")
  appendLine("theme=${currentTheme.name}")
  appendLine("features=income,expense,goals,history,insights,report,calculator,debt_tracker,admin_dashboard,fingerprint_login,theme_switch")
  appendLine("goal_rules=goals support progress source (income|expense|balance) and progress cards with percentage")
  appendLine("debt_tracker_rules=supports friend debt, installment, credit card, paylater, remaining balance, interest simulation")
  appendLine("insights_rules=rule-based personal insights with severity, metric chips, and action CTA")
}

/**
 * Builds a compact per-user financial snapshot string for the AI assistant.
 */
fun buildAiUserDataContext(
  currentUser: UserProfile?,
  incomeEntries: List<MoneyEntry>,
  expenseEntries: List<MoneyEntry>,
  dreamEntries: List<DreamEntry>,
  loanEntries: List<LoanEntry>,
  cachedInsights: List<InsightItem>,
): String {
  val incomeTotal = incomeEntries.sumOf { it.amount }
  val expenseTotal = expenseEntries.sumOf { it.amount }
  val balanceTotal = incomeTotal - expenseTotal

  val goalsSnapshot = dreamEntries.takeLast(8).joinToString("; ") { goal ->
    val source = goal.sourceType.ifBlank { "income" }
    val progressRaw = when (source) {
      "expense" -> expenseTotal
      "balance" -> balanceTotal
      else -> incomeTotal
    }
    val progress = progressRaw.coerceAtLeast(0).coerceAtMost(goal.target.coerceAtLeast(0))
    val percent = if (goal.target > 0) ((progress.toFloat() / goal.target) * 100f).roundToInt() else 0
    "${goal.title}: $progress/${goal.target} ($percent%) source=$source deadline=${goal.deadline}"
  }
  val debtSnapshot = loanEntries.takeLast(10).joinToString("; ") { loan ->
    val remaining = remainingLoanBalance(loan)
    "${loan.title} type=${loan.type} principal=${loan.principal} paid=${loan.paid} remaining=$remaining due=${loan.dueDate}"
  }
  val historySnapshot = (incomeEntries + expenseEntries)
    .sortedByDescending { parseDateTimeMillis(it.date) ?: 0L }
    .take(12)
    .joinToString("; ") { entry ->
      val kind = if (entry.type == EntryType.Income) "income" else "expense"
      "$kind amount=${entry.amount} category=${entry.category} date=${entry.date} note=${entry.note}"
    }
  val insightsSnapshot = cachedInsights.take(6).joinToString("; ") { insight ->
    "${insight.title} [${insight.severity.name}] chips=${insight.metricChips.joinToString("|")}"
  }
  return buildString {
    appendLine("profile=name=${currentUser?.name.orEmpty()} country=${currentUser?.country.orEmpty()}")
    appendLine("summary=income=$incomeTotal expense=$expenseTotal balance=$balanceTotal")
    appendLine("goals=$goalsSnapshot")
    appendLine("debts=$debtSnapshot")
    appendLine("recent_history=$historySnapshot")
    appendLine("latest_insights=$insightsSnapshot")
  }
}

/**
 * Submits insight helpfulness feedback to Supabase.
 */
suspend fun submitInsightFeedback(
  userId: String,
  insight: InsightItem,
  isHelpful: Boolean,
  reason: String = "",
) {
  if (userId.isBlank()) return
  runCatching {
    SupabaseClient.client
      .from("insight_feedback")
      .insert(
        buildJsonObject {
          put("id", java.util.UUID.randomUUID().toString())
          put("user_id", userId)
          put("insight_id", insight.id)
          put("is_helpful", isHelpful)
          put("reason", reason)
          put("created_at", nowJakartaText())
        },
      )
  }
}

/**
 * Regenerates insights from local data, optionally enriched with AI narrative.
 *
 * @param onRefreshing called on Main thread with the new refreshing state
 * @param onResult called on Main thread with the final list and persist request
 */
suspend fun refreshInsightsData(
  userId: String,
  timeframe: InsightTimeframe,
  forceAi: Boolean,
  aiInsightsEnabled: Boolean,
  aiInsightsPrivateMode: Boolean,
  incomeEntries: List<MoneyEntry>,
  expenseEntries: List<MoneyEntry>,
  dreamEntries: List<DreamEntry>,
  loanEntries: List<LoanEntry>,
  strings: AppStrings,
  localJson: Json,
  onRefreshing: (Boolean) -> Unit,
  onResult: (List<InsightItem>) -> Unit,
) {
  withContext(Dispatchers.Main) { onRefreshing(true) }
  val generated = generateInsights(
    incomeEntries = incomeEntries,
    expenseEntries = expenseEntries,
    goals = dreamEntries,
    debts = loanEntries,
    timeframe = timeframe,
  ).toMutableList()

  if ((aiInsightsEnabled || forceAi) && BuildConfig.CEREBRAS_API_KEY.isNotBlank()) {
    val summary = buildCompactInsightSummary(
      incomeEntries = incomeEntries,
      expenseEntries = expenseEntries,
      goals = dreamEntries,
      debts = loanEntries,
      timeframe = timeframe,
      privateMode = aiInsightsPrivateMode,
    )
    val aiMessage = runCatching {
      CerebrasClient.enrichInsightsNarrative(localJson.encodeToString(summary))
    }.getOrNull()
    if (!aiMessage.isNullOrBlank()) {
      generated.add(
        0,
        InsightItem(
          id = "ai_narrative_${timeframe.name}_${System.currentTimeMillis()}",
          title = strings["insights_ai_title"],
          message = aiMessage,
          severity = InsightSeverity.INFO,
          category = InsightCategory.Savings,
          metricChips = listOf(strings["insights_ai_chip"]),
          actionCta = strings["menu_report"],
          actionTargetPage = Page.Report.name,
          createdAt = nowJakartaText(),
          timeframe = timeframe,
        ),
      )
    }
  }
  withContext(Dispatchers.Main) {
    onRefreshing(false)
    onResult(generated)
  }
}

/**
 * Sends a chat history to the AI and returns the assistant reply.
 */
suspend fun requestAiReply(
  history: List<ChatMessage>,
  currentLang: AppLanguage,
  currentTheme: ThemeName,
  currentUser: UserProfile?,
  incomeEntries: List<MoneyEntry>,
  expenseEntries: List<MoneyEntry>,
  dreamEntries: List<DreamEntry>,
  loanEntries: List<LoanEntry>,
  cachedInsights: List<InsightItem>,
): String = CerebrasClient.requestFinancialAdvice(
  messages = history.takeLast(14),
  appContext = buildAiAppContext(currentLang, currentTheme),
  userDataContext = buildAiUserDataContext(currentUser, incomeEntries, expenseEntries, dreamEntries, loanEntries, cachedInsights),
)
