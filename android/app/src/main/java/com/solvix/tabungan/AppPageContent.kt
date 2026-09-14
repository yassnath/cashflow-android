package com.solvix.tabungan

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Renders the content area for the currently active [Page].
 * Keeps all per-page wiring out of [MainActivity] / [TabunganApp].
 */
@Composable
fun AppPageContent(
  currentPage: Page,
  strings: AppStrings,
  currentLang: AppLanguage,
  activeUserId: String,
  scope: CoroutineScope,
  // â”€â”€ Entry data â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  incomeEntries: androidx.compose.runtime.snapshots.SnapshotStateList<MoneyEntry>,
  expenseEntries: androidx.compose.runtime.snapshots.SnapshotStateList<MoneyEntry>,
  dreamEntries: androidx.compose.runtime.snapshots.SnapshotStateList<DreamEntry>,
  loanEntries: androidx.compose.runtime.snapshots.SnapshotStateList<LoanEntry>,
  cachedInsights: androidx.compose.runtime.snapshots.SnapshotStateList<InsightItem>,
  chatMessages: androidx.compose.runtime.snapshots.SnapshotStateList<ChatMessage>,
  // â”€â”€ UI State â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  summaryRange: SummaryRange,
  insightsTimeframe: InsightTimeframe,
  insightsRefreshing: Boolean,
  aiReplyLoading: Boolean,
  pendingEdit: MoneyEntry?,
  goalReachEvent: GoalReachEvent?,
  currentUser: UserProfile?,
  currentTheme: ThemeName,
  fingerprintEnabled: Boolean,
  aiInsightsEnabled: Boolean,
  aiInsightsPrivateMode: Boolean,
  // â”€â”€ Callbacks â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  onUpdateGoalMilestones: () -> Unit,
  onPersistLoanEntries: (userId: String) -> Unit,
  onRefreshInsights: suspend (userId: String, timeframe: InsightTimeframe, forceAi: Boolean) -> Unit,
  onRequestConfirm: (message: String, action: () -> Unit) -> Unit,
  onAlert: (String) -> Unit,
  onNavigateTo: (Page) -> Unit,
  onPendingEditConsumed: () -> Unit,
  onGoalReachDismiss: () -> Unit,
  onAiReplyLoadingChange: (Boolean) -> Unit,
  onInsightsTimeframeChange: (InsightTimeframe) -> Unit,
  // Settings-specific
  onFingerprintToggle: (Boolean) -> Unit,
  onAiInsightsToggle: (Boolean) -> Unit,
  onAiInsightsPrivateModeToggle: (Boolean) -> Unit,
  onLanguageChange: (AppLanguage) -> Unit,
  onChangePassword: (current: String, new: String, confirm: String) -> Unit,
  onDeleteAccount: () -> Unit,
  onThemeChange: (ThemeName) -> Unit,
  // Profile
  onProfileSave: (UserProfile) -> Unit,
  onProfileLogout: () -> Unit,
  // Insights
  onInsightFeedback: (InsightItem, Boolean) -> Unit,
  // AI Chat
  onRequestAiReply: suspend (List<ChatMessage>) -> String,
  // Report
  resolveStartYear: () -> Int,
) {
  // â”€â”€ Hero summary (shown on pages that declare showHero) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  if (currentPage.showHero()) {
    val filtered = filterByRange(incomeEntries + expenseEntries, summaryRange)
    val incomeTotal = filtered.filter { it.type == EntryType.Income }.sumOf { it.amount }
    val expenseTotal = filtered.filter { it.type == EntryType.Expense }.sumOf { it.amount }
    HeroSummary(
      range = summaryRange,
      onRangeChange = { /* parent controls summaryRange */ },
      incomeTotal = incomeTotal,
      expenseTotal = expenseTotal,
      strings = strings,
    )
  }

  // â”€â”€ Per-page routing â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  when (currentPage) {
    Page.Dashboard -> DashboardPage(
      incomeEntries = incomeEntries,
      expenseEntries = expenseEntries,
      dreamEntries = dreamEntries,
      loanEntries = loanEntries,
      currentUser = currentUser,
      currentLang = currentLang,
      strings = strings,
      onNavigateTo = onNavigateTo,
    )

    Page.Income -> IncomePage(
      onSave = { entry ->
        incomeEntries.add(entry)
        onUpdateGoalMilestones()
        if (activeUserId.isNotBlank()) {
          scope.launch(Dispatchers.IO) {
            SupabaseRepository.insertMoneyEntry(activeUserId, entry)
            onRefreshInsights(activeUserId, insightsTimeframe, false)
          }
        }
      },
      onUpdate = { entry ->
        val i = incomeEntries.indexOfFirst { it.id == entry.id }
        if (i >= 0) incomeEntries[i] = entry
        onUpdateGoalMilestones()
        if (activeUserId.isNotBlank()) {
          scope.launch(Dispatchers.IO) {
            SupabaseRepository.updateMoneyEntry(entry)
            onRefreshInsights(activeUserId, insightsTimeframe, false)
          }
        }
      },
      editEntry = pendingEdit,
      onEditConsumed = onPendingEditConsumed,
      strings = strings,
    )

    Page.Expense -> ExpensePage(
      onSave = { entry ->
        expenseEntries.add(entry)
        onUpdateGoalMilestones()
        if (activeUserId.isNotBlank()) {
          scope.launch(Dispatchers.IO) {
            SupabaseRepository.insertMoneyEntry(activeUserId, entry)
            onRefreshInsights(activeUserId, insightsTimeframe, false)
          }
        }
      },
      onUpdate = { entry ->
        val i = expenseEntries.indexOfFirst { it.id == entry.id }
        if (i >= 0) expenseEntries[i] = entry
        onUpdateGoalMilestones()
        if (activeUserId.isNotBlank()) {
          scope.launch(Dispatchers.IO) {
            SupabaseRepository.updateMoneyEntry(entry)
            onRefreshInsights(activeUserId, insightsTimeframe, false)
          }
        }
      },
      editEntry = pendingEdit,
      onEditConsumed = onPendingEditConsumed,
      strings = strings,
    )

    Page.Dreams -> DreamsPage(
      entries = dreamEntries,
      incomeTotal = incomeEntries.sumOf { it.amount },
      expenseTotal = expenseEntries.sumOf { it.amount },
      balanceTotal = incomeEntries.sumOf { it.amount } - expenseEntries.sumOf { it.amount },
      onInvalid = { onAlert(strings["goal_missing"]) },
      onSave = { entry ->
        dreamEntries.add(entry)
        onUpdateGoalMilestones()
        if (activeUserId.isNotBlank()) {
          scope.launch(Dispatchers.IO) {
            SupabaseRepository.insertDreamEntry(activeUserId, entry)
            onRefreshInsights(activeUserId, insightsTimeframe, false)
          }
        }
      },
      onUpdate = { entry ->
        val i = dreamEntries.indexOfFirst { it.id == entry.id }
        if (i >= 0) dreamEntries[i] = entry
        onUpdateGoalMilestones()
        if (activeUserId.isNotBlank()) {
          scope.launch(Dispatchers.IO) {
            SupabaseRepository.updateDreamEntry(entry)
            onRefreshInsights(activeUserId, insightsTimeframe, false)
          }
        }
      },
      onDelete = { entry ->
        onRequestConfirm(strings["confirm_delete_dream"]) {
          dreamEntries.removeAll { it.id == entry.id }
          onUpdateGoalMilestones()
          if (activeUserId.isNotBlank()) {
            scope.launch(Dispatchers.IO) {
              SupabaseRepository.deleteDreamEntry(entry.id)
              onRefreshInsights(activeUserId, insightsTimeframe, false)
            }
          }
        }
      },
      goalReachSourceType = if (currentPage == Page.Dreams) goalReachEvent?.sourceType else null,
      onGoalReachDismiss = onGoalReachDismiss,
      strings = strings,
    )

    Page.History -> HistoryPage(
      entries = incomeEntries + expenseEntries,
      strings = strings,
      onEdit = { entry ->
        onNavigateTo(if (entry.type == EntryType.Income) Page.Income else Page.Expense)
      },
      onDelete = { entry ->
        val key = if (entry.type == EntryType.Income) "confirm_delete_income" else "confirm_delete_expense"
        onRequestConfirm(strings[key]) {
          when (entry.type) {
            EntryType.Income -> incomeEntries.removeAll { it.id == entry.id }
            EntryType.Expense -> expenseEntries.removeAll { it.id == entry.id }
          }
          onUpdateGoalMilestones()
          if (activeUserId.isNotBlank()) {
            scope.launch(Dispatchers.IO) {
              SupabaseRepository.deleteMoneyEntry(entry.id)
              onRefreshInsights(activeUserId, insightsTimeframe, false)
            }
          }
        }
      },
    )

    Page.Insights -> InsightsPage(
      insights = cachedInsights.toList(),
      defaultTimeframe = insightsTimeframe,
      onRefresh = { timeframe ->
        onInsightsTimeframeChange(timeframe)
        if (activeUserId.isNotBlank()) {
          scope.launch(Dispatchers.IO) { onRefreshInsights(activeUserId, timeframe, false) }
        }
      },
      onAction = { insight ->
        val target = runCatching { Page.valueOf(insight.actionTargetPage) }.getOrNull()
        if (target != null) onNavigateTo(target)
      },
      onFeedback = { insight, helpful -> onInsightFeedback(insight, helpful) },
      isRefreshing = insightsRefreshing,
      strings = strings,
    )

    Page.Loans -> LoanTrackingPage(
      entries = loanEntries,
      onInvalid = { onAlert(strings["loan_missing"]) },
      onSave = { entry ->
        loanEntries.add(entry)
        onPersistLoanEntries(activeUserId)
        if (activeUserId.isNotBlank()) {
          scope.launch(Dispatchers.IO) { onRefreshInsights(activeUserId, insightsTimeframe, false) }
        }
      },
      onUpdate = { entry ->
        val i = loanEntries.indexOfFirst { it.id == entry.id }
        if (i >= 0) {
          loanEntries[i] = entry
          onPersistLoanEntries(activeUserId)
          if (activeUserId.isNotBlank()) {
            scope.launch(Dispatchers.IO) { onRefreshInsights(activeUserId, insightsTimeframe, false) }
          }
        }
      },
      onDelete = { entry ->
        onRequestConfirm(strings["confirm_delete_loan"]) {
          loanEntries.removeAll { it.id == entry.id }
          onPersistLoanEntries(activeUserId)
          if (activeUserId.isNotBlank()) {
            scope.launch(Dispatchers.IO) { onRefreshInsights(activeUserId, insightsTimeframe, false) }
          }
        }
      },
      strings = strings,
    )

    Page.AIChat -> AiChatPage(
      messages = chatMessages,
      isLoading = aiReplyLoading,
      onClear = { chatMessages.clear() },
      onSend = { prompt ->
        val trimmed = prompt.trim()
        if (trimmed.isBlank() || aiReplyLoading) return@AiChatPage
        chatMessages.add(ChatMessage(role = "user", content = trimmed))
        onAiReplyLoadingChange(true)
        scope.launch(Dispatchers.IO) {
          try {
            val reply = onRequestAiReply(chatMessages.toList())
            withContext(Dispatchers.Main) {
              chatMessages.add(ChatMessage(role = "assistant", content = reply))
              onAiReplyLoadingChange(false)
            }
          } catch (e: Exception) {
            Log.e("AIChat", "Request failed", e)
            withContext(Dispatchers.Main) {
              onAiReplyLoadingChange(false)
              onAlert(
                if (BuildConfig.CEREBRAS_API_KEY.isBlank()) strings["ai_not_configured"]
                else strings["ai_request_failed"],
              )
            }
          }
        }
      },
      strings = strings,
    )

    Page.Calculator -> CalculatorPage()

    Page.Report -> ReportPage(
      incomeEntries,
      expenseEntries,
      strings = strings,
      language = currentLang,
      startYear = resolveStartYear(),
    ) { msg -> /* toast handled by parent */ }

    Page.Profile -> ProfilePage(
      user = currentUser,
      strings = strings,
      onSave = onProfileSave,
      onLogout = onProfileLogout,
    )

    Page.Settings -> SettingsPage(
      fingerprintEnabled = fingerprintEnabled,
      onFingerprintToggle = onFingerprintToggle,
      aiInsightsEnabled = aiInsightsEnabled,
      onAiInsightsToggle = onAiInsightsToggle,
      aiInsightsPrivateMode = aiInsightsPrivateMode,
      onAiInsightsPrivateModeToggle = onAiInsightsPrivateModeToggle,
      language = currentLang,
      onLanguageChange = onLanguageChange,
      strings = strings,
      onChangePassword = onChangePassword,
      canDeleteAccount = currentUser?.id?.isNotBlank() == true,
      onDeleteAccount = onDeleteAccount,
    )

    Page.Themes -> ThemesPage(currentTheme) { selected ->
      onRequestConfirm("${strings["confirm_theme_change"]} ${themeLabel(selected, strings)}?") {
        onThemeChange(selected)
      }
    }
  }
}
