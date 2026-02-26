package com.solvix.tabungan

import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.serialization.Serializable

@Serializable
enum class InsightSeverity { INFO, WARN, ALERT }

@Serializable
enum class InsightCategory { Spending, Income, Savings, Debt, Goals, Habits, Runway }

@Serializable
enum class InsightTimeframe { D7, D30, MONTH, YEAR, ALL }

@Serializable
data class InsightItem(
  val id: String,
  val title: String,
  val message: String,
  val severity: InsightSeverity,
  val category: InsightCategory,
  val metricChips: List<String>,
  val actionCta: String,
  val actionTargetPage: String,
  val createdAt: String,
  val timeframe: InsightTimeframe,
)

@Serializable
data class InsightSummaryBucket(
  val key: String,
  val value: String,
)

@Serializable
data class InsightSummaryPayload(
  val timeframe: String,
  val incomeTotal: String,
  val expenseTotal: String,
  val balanceTotal: String,
  val savingsRate: String,
  val topExpenseCategories: List<InsightSummaryBucket>,
  val goals: List<InsightSummaryBucket>,
  val debts: List<InsightSummaryBucket>,
)

private data class InsightContext(
  val timeframe: InsightTimeframe,
  val filteredIncome: List<MoneyEntry>,
  val filteredExpense: List<MoneyEntry>,
  val allIncome: List<MoneyEntry>,
  val allExpense: List<MoneyEntry>,
  val goals: List<DreamEntry>,
  val debts: List<LoanEntry>,
  val nowMillis: Long,
) {
  val incomeTotal: Int = filteredIncome.sumOf { it.amount }
  val expenseTotal: Int = filteredExpense.sumOf { it.amount }
  val balance: Int = incomeTotal - expenseTotal
}

fun generateInsights(
  incomeEntries: List<MoneyEntry>,
  expenseEntries: List<MoneyEntry>,
  goals: List<DreamEntry>,
  debts: List<LoanEntry>,
  timeframe: InsightTimeframe,
  nowMillis: Long = System.currentTimeMillis(),
): List<InsightItem> {
  val ctx = InsightContext(
    timeframe = timeframe,
    filteredIncome = filterByInsightTimeframe(incomeEntries, timeframe, nowMillis),
    filteredExpense = filterByInsightTimeframe(expenseEntries, timeframe, nowMillis),
    allIncome = incomeEntries,
    allExpense = expenseEntries,
    goals = goals,
    debts = debts,
    nowMillis = nowMillis,
  )
  val createdAt = nowJakartaText()
  val rules = listOfNotNull(
    spendingWeekVsWeek(ctx, createdAt),
    spendingMonthVsMonth(ctx, createdAt),
    spendingCategorySpike(ctx, createdAt),
    spendingUnusualSingle(ctx, createdAt),
    incomeDrop(ctx, createdAt),
    incomeDelayed(ctx, createdAt),
    incomeVariance(ctx, createdAt),
    savingsRateLow(ctx, createdAt),
    savingsRateStrong(ctx, createdAt),
    goalDailyNeed(ctx, createdAt),
    goalBehindSchedule(ctx, createdAt),
    goalAheadSchedule(ctx, createdAt),
    debtToIncomeRatio(ctx, createdAt),
    debtPayoffRisk(ctx, createdAt),
    debtInterestWarning(ctx, createdAt),
    debtTimelineWarning(ctx, createdAt),
    runwayInsight(ctx, createdAt),
    habitStreakInsight(ctx, createdAt),
    habitGapInsight(ctx, createdAt),
  ).sortedByDescending { it.severity.ordinal }
  if (rules.isNotEmpty()) return rules
  return listOf(
    InsightItem(
      id = "default_${timeframe.name}",
      title = "No critical signal",
      message = "Current data does not show urgent risk in this timeframe. Keep your routine and refresh after new transactions.",
      severity = InsightSeverity.INFO,
      category = InsightCategory.Habits,
      metricChips = listOf("Timeframe ${timeframeLabel(timeframe)}"),
      actionCta = "Open History",
      actionTargetPage = Page.History.name,
      createdAt = createdAt,
      timeframe = timeframe,
    ),
  )
}

fun buildCompactInsightSummary(
  incomeEntries: List<MoneyEntry>,
  expenseEntries: List<MoneyEntry>,
  goals: List<DreamEntry>,
  debts: List<LoanEntry>,
  timeframe: InsightTimeframe,
  privateMode: Boolean,
  nowMillis: Long = System.currentTimeMillis(),
): InsightSummaryPayload {
  val income = filterByInsightTimeframe(incomeEntries, timeframe, nowMillis)
  val expense = filterByInsightTimeframe(expenseEntries, timeframe, nowMillis)
  val incomeTotal = income.sumOf { it.amount }
  val expenseTotal = expense.sumOf { it.amount }
  val balance = incomeTotal - expenseTotal
  val savingsRate = if (incomeTotal > 0) ((balance.toDouble() / incomeTotal) * 100).roundToInt() else 0
  val topCategories = expense.groupBy { it.category.ifBlank { "Unknown" } }
    .mapValues { it.value.sumOf { row -> row.amount } }
    .entries
    .sortedByDescending { it.value }
    .take(5)
    .map { InsightSummaryBucket(it.key, if (privateMode) amountBand(it.value) else formatRupiah(it.value)) }
  val goalBuckets = goals.take(6).map { goal ->
    val progress = when (goal.sourceType) {
      "expense" -> expenseTotal
      "balance" -> balance
      else -> incomeTotal
    }.coerceAtLeast(0).coerceAtMost(goal.target.coerceAtLeast(0))
    val pct = if (goal.target > 0) ((progress.toDouble() / goal.target) * 100).roundToInt() else 0
    InsightSummaryBucket(goal.title, "$pct%")
  }
  val debtBuckets = debts.take(8).map { debt ->
    val remaining = remainingLoanBalance(debt)
    InsightSummaryBucket(debt.title, if (privateMode) amountBand(remaining) else formatRupiah(remaining))
  }
  return InsightSummaryPayload(
    timeframe = timeframeLabel(timeframe),
    incomeTotal = if (privateMode) amountBand(incomeTotal) else formatRupiah(incomeTotal),
    expenseTotal = if (privateMode) amountBand(expenseTotal) else formatRupiah(expenseTotal),
    balanceTotal = if (privateMode) amountBand(balance) else formatRupiah(balance),
    savingsRate = "$savingsRate%",
    topExpenseCategories = topCategories,
    goals = goalBuckets,
    debts = debtBuckets,
  )
}

private fun filterByInsightTimeframe(entries: List<MoneyEntry>, timeframe: InsightTimeframe, nowMillis: Long): List<MoneyEntry> {
  if (timeframe == InsightTimeframe.ALL) return entries
  val nowCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
  val start = when (timeframe) {
    InsightTimeframe.D7 -> nowMillis - 6L * 24L * 60L * 60L * 1000L
    InsightTimeframe.D30 -> nowMillis - 29L * 24L * 60L * 60L * 1000L
    InsightTimeframe.MONTH -> Calendar.getInstance().apply {
      timeInMillis = nowMillis
      set(Calendar.DAY_OF_MONTH, 1)
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    InsightTimeframe.YEAR -> Calendar.getInstance().apply {
      timeInMillis = nowMillis
      set(Calendar.MONTH, Calendar.JANUARY)
      set(Calendar.DAY_OF_MONTH, 1)
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    InsightTimeframe.ALL -> 0L
  }
  return entries.filter { entry ->
    val ts = parseDateTimeMillis(entry.date) ?: return@filter false
    val sameYear = if (timeframe == InsightTimeframe.YEAR) {
      val c = Calendar.getInstance().apply { timeInMillis = ts }
      c.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
    } else true
    ts in start..nowMillis && sameYear
  }
}

private fun timeframeLabel(timeframe: InsightTimeframe): String = when (timeframe) {
  InsightTimeframe.D7 -> "7d"
  InsightTimeframe.D30 -> "30d"
  InsightTimeframe.MONTH -> "month"
  InsightTimeframe.YEAR -> "year"
  InsightTimeframe.ALL -> "all"
}

private fun amountBand(value: Int): String {
  val absolute = abs(value)
  val band = when {
    absolute < 100_000 -> "<100k"
    absolute < 500_000 -> "100k-500k"
    absolute < 1_000_000 -> "500k-1m"
    absolute < 5_000_000 -> "1m-5m"
    absolute < 10_000_000 -> "5m-10m"
    else -> ">=10m"
  }
  return if (value < 0) "-$band" else band
}

private fun insight(
  id: String,
  title: String,
  message: String,
  severity: InsightSeverity,
  category: InsightCategory,
  chips: List<String>,
  cta: String,
  target: Page,
  createdAt: String,
  timeframe: InsightTimeframe,
): InsightItem = InsightItem(
  id = id,
  title = title,
  message = message,
  severity = severity,
  category = category,
  metricChips = chips,
  actionCta = cta,
  actionTargetPage = target.name,
  createdAt = createdAt,
  timeframe = timeframe,
)

private fun sumLastDays(entries: List<MoneyEntry>, days: Int, nowMillis: Long): Int {
  val start = nowMillis - (days - 1).toLong() * 24L * 60L * 60L * 1000L
  return entries.filter {
    val ts = parseDateTimeMillis(it.date) ?: return@filter false
    ts in start..nowMillis
  }.sumOf { it.amount }
}

private fun sumBetweenDays(entries: List<MoneyEntry>, startDaysBack: Int, endDaysBack: Int, nowMillis: Long): Int {
  val start = nowMillis - endDaysBack.toLong() * 24L * 60L * 60L * 1000L
  val end = nowMillis - (startDaysBack - 1L) * 24L * 60L * 60L * 1000L
  return entries.filter {
    val ts = parseDateTimeMillis(it.date) ?: return@filter false
    ts in start..end
  }.sumOf { it.amount }
}

private fun lastNWeeklySums(entries: List<MoneyEntry>, weeks: Int, nowMillis: Long): List<Double> {
  return (0 until weeks).map { index ->
    val end = nowMillis - (index * 7L) * 24L * 60L * 60L * 1000L
    sumLastDays(entries, 7, end).toDouble()
  }.reversed()
}

private fun spendingWeekVsWeek(ctx: InsightContext, createdAt: String): InsightItem? {
  val current = sumLastDays(ctx.allExpense, 7, ctx.nowMillis)
  val previous = sumBetweenDays(ctx.allExpense, 8, 14, ctx.nowMillis)
  if (previous <= 0) return null
  val change = (((current - previous).toDouble() / previous) * 100).roundToInt()
  if (change < 20) return null
  return insight(
    id = "spending_ww_${ctx.timeframe}",
    title = "Weekly spending climbed",
    message = "Your spending in the last 7 days is higher than the previous week. Check recent categories and cut low-priority purchases this week.",
    severity = if (change >= 40) InsightSeverity.ALERT else InsightSeverity.WARN,
    category = InsightCategory.Spending,
    chips = listOf("$change% vs prev week", "Now ${formatRupiah(current)}"),
    cta = "Review category",
    target = Page.History,
    createdAt = createdAt,
    timeframe = ctx.timeframe,
  )
}

private fun spendingMonthVsMonth(ctx: InsightContext, createdAt: String): InsightItem? {
  val now = Calendar.getInstance().apply { timeInMillis = ctx.nowMillis }
  val thisMonth = ctx.allExpense.filter {
    val c = Calendar.getInstance().apply { timeInMillis = parseDateTimeMillis(it.date) ?: return@filter false }
    c.get(Calendar.YEAR) == now.get(Calendar.YEAR) && c.get(Calendar.MONTH) == now.get(Calendar.MONTH)
  }.sumOf { it.amount }
  val prev = Calendar.getInstance().apply { timeInMillis = ctx.nowMillis; add(Calendar.MONTH, -1) }
  val prevMonth = ctx.allExpense.filter {
    val c = Calendar.getInstance().apply { timeInMillis = parseDateTimeMillis(it.date) ?: return@filter false }
    c.get(Calendar.YEAR) == prev.get(Calendar.YEAR) && c.get(Calendar.MONTH) == prev.get(Calendar.MONTH)
  }.sumOf { it.amount }
  if (prevMonth <= 0) return null
  val change = (((thisMonth - prevMonth).toDouble() / prevMonth) * 100).roundToInt()
  if (change < 15) return null
  return insight(
    id = "spending_mm_${ctx.timeframe}",
    title = "Month spending trend up",
    message = "This month spending is above last month. Re-check high-growth categories before month end.",
    severity = if (change >= 30) InsightSeverity.ALERT else InsightSeverity.WARN,
    category = InsightCategory.Spending,
    chips = listOf("$change% vs last month", "This month ${formatRupiah(thisMonth)}"),
    cta = "Review category",
    target = Page.Expense,
    createdAt = createdAt,
    timeframe = ctx.timeframe,
  )
}

private fun spendingCategorySpike(ctx: InsightContext, createdAt: String): InsightItem? {
  if (ctx.filteredExpense.isEmpty()) return null
  val grouped = ctx.filteredExpense.groupBy { it.category.ifBlank { "Unknown" } }.mapValues { it.value.sumOf { row -> row.amount } }
  val top = grouped.maxByOrNull { it.value } ?: return null
  val average = if (grouped.size > 1) (grouped.values.sum() - top.value).toDouble() / (grouped.size - 1) else 0.0
  if (average <= 0.0 || top.value < average * 1.8) return null
  return insight(
    id = "spending_category_spike_${ctx.timeframe}",
    title = "Category spike detected",
    message = "One spending category is significantly above your normal mix. Consider setting a mini cap for this category for the rest of the period.",
    severity = InsightSeverity.WARN,
    category = InsightCategory.Spending,
    chips = listOf(top.key, "Spike ${(top.value / average).roundToInt()}x"),
    cta = "Review category",
    target = Page.Expense,
    createdAt = createdAt,
    timeframe = ctx.timeframe,
  )
}

private fun spendingUnusualSingle(ctx: InsightContext, createdAt: String): InsightItem? {
  if (ctx.filteredExpense.isEmpty() || ctx.expenseTotal <= 0) return null
  val largest = ctx.filteredExpense.maxByOrNull { it.amount } ?: return null
  val portion = ((largest.amount.toDouble() / ctx.expenseTotal) * 100).roundToInt()
  if (portion < 35) return null
  return insight(
    id = "spending_unusual_single_${ctx.timeframe}",
    title = "Large single expense",
    message = "A single expense contributes a large part of total spending in this timeframe. Validate if this was planned or one-off.",
    severity = if (portion >= 50) InsightSeverity.ALERT else InsightSeverity.WARN,
    category = InsightCategory.Spending,
    chips = listOf("$portion% of total", formatRupiah(largest.amount)),
    cta = "Open history",
    target = Page.History,
    createdAt = createdAt,
    timeframe = ctx.timeframe,
  )
}

private fun incomeDrop(ctx: InsightContext, createdAt: String): InsightItem? {
  val current = sumLastDays(ctx.allIncome, 30, ctx.nowMillis)
  val previous = sumBetweenDays(ctx.allIncome, 31, 60, ctx.nowMillis)
  if (previous <= 0 || current >= previous) return null
  val drop = (((previous - current).toDouble() / previous) * 100).roundToInt()
  if (drop < 15) return null
  return insight(
    id = "income_drop_${ctx.timeframe}",
    title = "Income declined",
    message = "Income in the recent period is lower than before. Prioritize fixed expenses and avoid adding new liabilities this cycle.",
    severity = if (drop >= 30) InsightSeverity.ALERT else InsightSeverity.WARN,
    category = InsightCategory.Income,
    chips = listOf("-$drop% vs previous period", "Now ${formatRupiah(current)}"),
    cta = "Open income",
    target = Page.Income,
    createdAt = createdAt,
    timeframe = ctx.timeframe,
  )
}

private fun incomeDelayed(ctx: InsightContext, createdAt: String): InsightItem? {
  val recentIncome = sumLastDays(ctx.allIncome, 7, ctx.nowMillis)
  val previousIncome = sumBetweenDays(ctx.allIncome, 8, 21, ctx.nowMillis)
  if (recentIncome > 0 || previousIncome <= 0) return null
  return insight(
    id = "income_delayed_${ctx.timeframe}",
    title = "No recent income flow",
    message = "There is no recorded income in the last 7 days while previous weeks had income. Track incoming cashflow timing to avoid liquidity pressure.",
    severity = InsightSeverity.WARN,
    category = InsightCategory.Income,
    chips = listOf("Last 7d ${formatRupiah(recentIncome)}", "Prev window ${formatRupiah(previousIncome)}"),
    cta = "Open income",
    target = Page.Income,
    createdAt = createdAt,
    timeframe = ctx.timeframe,
  )
}

private fun incomeVariance(ctx: InsightContext, createdAt: String): InsightItem? {
  val weekly = lastNWeeklySums(ctx.allIncome, 6, ctx.nowMillis)
  if (weekly.size < 4) return null
  val mean = weekly.average()
  if (mean <= 0.0) return null
  val variance = weekly.sumOf { (it - mean) * (it - mean) } / weekly.size
  val cv = kotlin.math.sqrt(variance) / mean
  if (cv < 0.65) return null
  return insight(
    id = "income_variance_${ctx.timeframe}",
    title = "Income is volatile",
    message = "Your income pattern is volatile across recent weeks. Consider a larger emergency buffer and conservative spending baseline.",
    severity = InsightSeverity.WARN,
    category = InsightCategory.Income,
    chips = listOf("Variance ${(cv * 100).roundToInt()}%", "Avg/week ${formatRupiah(mean.roundToInt())}"),
    cta = "Set mini-goal",
    target = Page.Dreams,
    createdAt = createdAt,
    timeframe = ctx.timeframe,
  )
}

private fun savingsRateLow(ctx: InsightContext, createdAt: String): InsightItem? {
  if (ctx.incomeTotal <= 0) return null
  val rate = ((ctx.balance.toDouble() / ctx.incomeTotal) * 100).roundToInt()
  if (rate >= 10) return null
  val suggestion = (ctx.incomeTotal * 0.15).roundToInt().coerceAtLeast(0)
  return insight(
    id = "savings_low_${ctx.timeframe}",
    title = "Savings rate is low",
    message = "Your savings rate is below the healthy threshold for this period. Aim to secure at least 15% of income as retained balance.",
    severity = InsightSeverity.WARN,
    category = InsightCategory.Savings,
    chips = listOf("Savings $rate%", "Target ${formatRupiah(suggestion)}"),
    cta = "Set mini-goal",
    target = Page.Dreams,
    createdAt = createdAt,
    timeframe = ctx.timeframe,
  )
}

private fun savingsRateStrong(ctx: InsightContext, createdAt: String): InsightItem? {
  if (ctx.incomeTotal <= 0) return null
  val rate = ((ctx.balance.toDouble() / ctx.incomeTotal) * 100).roundToInt()
  if (rate < 30) return null
  return insight(
    id = "savings_strong_${ctx.timeframe}",
    title = "Strong savings momentum",
    message = "Your retained balance ratio is strong in this period. Consider allocating a part of surplus to priority goals.",
    severity = InsightSeverity.INFO,
    category = InsightCategory.Savings,
    chips = listOf("Savings $rate%", "Surplus ${formatRupiah(ctx.balance)}"),
    cta = "Open goals",
    target = Page.Dreams,
    createdAt = createdAt,
    timeframe = ctx.timeframe,
  )
}

private fun goalDailyNeed(ctx: InsightContext, createdAt: String): InsightItem? {
  val risky = ctx.goals.mapNotNull { goal ->
    if (goal.deadline.isBlank() || goal.target <= 0) return@mapNotNull null
    val deadlineMillis = parseDate(goal.deadline) ?: return@mapNotNull null
    val daysLeft = ((deadlineMillis - ctx.nowMillis) / (24L * 60L * 60L * 1000L)).toInt().coerceAtLeast(1)
    val progressRaw = when (goal.sourceType) {
      "expense" -> ctx.expenseTotal
      "balance" -> ctx.balance
      else -> ctx.incomeTotal
    }
    val progress = progressRaw.coerceAtLeast(0).coerceAtMost(goal.target)
    val needPerDay = ((goal.target - progress).coerceAtLeast(0) / daysLeft).coerceAtLeast(0)
    Triple(goal, daysLeft, needPerDay)
  }.maxByOrNull { it.third } ?: return null
  if (risky.third <= 0) return null
  return insight(
    id = "goal_daily_need_${ctx.timeframe}",
    title = "Goal pace required",
    message = "To hit your goal on time, your daily contribution needs to stay consistent. Split target into weekly checkpoints to reduce slippage.",
    severity = if (risky.second <= 14) InsightSeverity.ALERT else InsightSeverity.WARN,
    category = InsightCategory.Goals,
    chips = listOf(risky.first.title, "Need/day ${formatRupiah(risky.third)}", "${risky.second} days left"),
    cta = "Open goals",
    target = Page.Dreams,
    createdAt = createdAt,
    timeframe = ctx.timeframe,
  )
}

private fun goalBehindSchedule(ctx: InsightContext, createdAt: String): InsightItem? {
  val behind = ctx.goals.firstOrNull { goal ->
    if (goal.target <= 0) return@firstOrNull false
    val deadlineMillis = parseDate(goal.deadline) ?: return@firstOrNull false
    val daysLeft = ((deadlineMillis - ctx.nowMillis) / (24L * 60L * 60L * 1000L)).toInt()
    if (daysLeft < 0) return@firstOrNull false
    val progressRaw = when (goal.sourceType) {
      "expense" -> ctx.expenseTotal
      "balance" -> ctx.balance
      else -> ctx.incomeTotal
    }
    val progressPct = (progressRaw.toDouble() / goal.target).coerceIn(0.0, 1.0)
    val elapsedPct = (1.0 - (daysLeft.toDouble() / 30.0)).coerceIn(0.0, 1.0)
    progressPct + 0.08 < elapsedPct
  } ?: return null
  return insight(
    id = "goal_behind_${ctx.timeframe}",
    title = "Goal is behind schedule",
    message = "At least one goal is progressing below time-based expectation. Reduce non-essential spending or add temporary income to catch up.",
    severity = InsightSeverity.WARN,
    category = InsightCategory.Goals,
    chips = listOf(behind.title, "Deadline ${behind.deadline}"),
    cta = "Open goals",
    target = Page.Dreams,
    createdAt = createdAt,
    timeframe = ctx.timeframe,
  )
}

private fun goalAheadSchedule(ctx: InsightContext, createdAt: String): InsightItem? {
  val ahead = ctx.goals.firstOrNull { goal ->
    if (goal.target <= 0) return@firstOrNull false
    val progressRaw = when (goal.sourceType) {
      "expense" -> ctx.expenseTotal
      "balance" -> ctx.balance
      else -> ctx.incomeTotal
    }
    (progressRaw.toDouble() / goal.target) * 100 >= 85
  } ?: return null
  return insight(
    id = "goal_ahead_${ctx.timeframe}",
    title = "Goal is ahead",
    message = "One goal is very close to target before deadline. Keep consistency and prepare the next goal milestone.",
    severity = InsightSeverity.INFO,
    category = InsightCategory.Goals,
    chips = listOf(ahead.title, "Deadline ${ahead.deadline}"),
    cta = "Open goals",
    target = Page.Dreams,
    createdAt = createdAt,
    timeframe = ctx.timeframe,
  )
}

private fun debtToIncomeRatio(ctx: InsightContext, createdAt: String): InsightItem? {
  val liabilities = ctx.debts.sumOf { remainingLoanBalance(it) }
  val monthlyIncome = sumLastDays(ctx.allIncome, 30, ctx.nowMillis)
  if (liabilities <= 0 || monthlyIncome <= 0) return null
  val ratio = liabilities.toDouble() / monthlyIncome
  if (ratio < 0.8) return null
  return insight(
    id = "debt_income_ratio_${ctx.timeframe}",
    title = "Debt-to-income is high",
    message = "Current liabilities are high compared to monthly income flow. Prioritize high-interest debt and avoid adding new obligations.",
    severity = if (ratio >= 1.2) InsightSeverity.ALERT else InsightSeverity.WARN,
    category = InsightCategory.Debt,
    chips = listOf("DTI ${(ratio * 100).roundToInt()}%", "Debt ${formatRupiah(liabilities)}"),
    cta = "Open debt detail",
    target = Page.Loans,
    createdAt = createdAt,
    timeframe = ctx.timeframe,
  )
}

private fun debtPayoffRisk(ctx: InsightContext, createdAt: String): InsightItem? {
  val monthlyIncome = sumLastDays(ctx.allIncome, 30, ctx.nowMillis)
  if (monthlyIncome <= 0) return null
  val monthlyDebtPay = ctx.debts.sumOf { it.monthlyPayment.coerceAtLeast(0) }
  val ratio = monthlyDebtPay.toDouble() / monthlyIncome
  if (ratio < 0.4) return null
  return insight(
    id = "debt_payoff_risk_${ctx.timeframe}",
    title = "Debt payment pressure",
    message = "Planned monthly debt payments consume a large share of income. Rework payment sequence and focus on expensive debt first.",
    severity = if (ratio >= 0.6) InsightSeverity.ALERT else InsightSeverity.WARN,
    category = InsightCategory.Debt,
    chips = listOf("Debt pay ${(ratio * 100).roundToInt()}%", "Monthly ${formatRupiah(monthlyDebtPay)}"),
    cta = "Open debt detail",
    target = Page.Loans,
    createdAt = createdAt,
    timeframe = ctx.timeframe,
  )
}

private fun debtInterestWarning(ctx: InsightContext, createdAt: String): InsightItem? {
  val highRate = ctx.debts.maxByOrNull { it.annualInterestRate } ?: return null
  if (highRate.annualInterestRate < 20.0) return null
  return insight(
    id = "debt_interest_${ctx.timeframe}",
    title = "High interest debt found",
    message = "At least one debt has a high annual interest rate. Fast-tracking this debt can reduce long-term cost.",
    severity = if (highRate.annualInterestRate >= 30.0) InsightSeverity.ALERT else InsightSeverity.WARN,
    category = InsightCategory.Debt,
    chips = listOf(highRate.title, "Rate ${highRate.annualInterestRate}%"),
    cta = "Open debt detail",
    target = Page.Loans,
    createdAt = createdAt,
    timeframe = ctx.timeframe,
  )
}

private fun debtTimelineWarning(ctx: InsightContext, createdAt: String): InsightItem? {
  val longest = ctx.debts.mapNotNull { debt ->
    val months = simulateLoanPayoffMonths(debt) ?: return@mapNotNull null
    debt to months
  }.maxByOrNull { it.second } ?: return null
  if (longest.second < 24) return null
  return insight(
    id = "debt_timeline_${ctx.timeframe}",
    title = "Long payoff timeline",
    message = "One debt is projected to take a long time to close. Increasing monthly payment even slightly can shorten the timeline materially.",
    severity = InsightSeverity.WARN,
    category = InsightCategory.Debt,
    chips = listOf(longest.first.title, "${longest.second} months"),
    cta = "Open debt detail",
    target = Page.Loans,
    createdAt = createdAt,
    timeframe = ctx.timeframe,
  )
}

private fun runwayInsight(ctx: InsightContext, createdAt: String): InsightItem? {
  val balance = (ctx.allIncome.sumOf { it.amount } - ctx.allExpense.sumOf { it.amount }).coerceAtLeast(0)
  val avgDaily = sumLastDays(ctx.allExpense, 30, ctx.nowMillis) / 30.0
  if (avgDaily <= 0.0 || balance <= 0) return null
  val days = (balance / avgDaily).roundToInt()
  if (days > 30) return null
  return insight(
    id = "runway_${ctx.timeframe}",
    title = "Balance runway is short",
    message = "At current expense pace, available balance may run out soon. Reduce variable expenses and secure near-term cash inflow.",
    severity = if (days <= 14) InsightSeverity.ALERT else InsightSeverity.WARN,
    category = InsightCategory.Runway,
    chips = listOf("Runway $days days", "Avg/day ${formatRupiah(avgDaily.roundToInt())}"),
    cta = "Open expense",
    target = Page.Expense,
    createdAt = createdAt,
    timeframe = ctx.timeframe,
  )
}

private fun habitStreakInsight(ctx: InsightContext, createdAt: String): InsightItem? {
  val dates = (ctx.filteredIncome + ctx.filteredExpense).mapNotNull { parseDate(it.date) }.distinct().sortedDescending()
  if (dates.isEmpty()) return null
  var streak = 1
  for (i in 0 until dates.lastIndex) {
    val diff = ((dates[i] - dates[i + 1]) / (24L * 60L * 60L * 1000L)).toInt()
    if (diff == 1) streak++ else break
  }
  if (streak < 7) return null
  return insight(
    id = "habit_streak_${ctx.timeframe}",
    title = "Strong tracking streak",
    message = "You have a solid transaction logging streak. Consistent tracking improves forecasting accuracy and spending control.",
    severity = InsightSeverity.INFO,
    category = InsightCategory.Habits,
    chips = listOf("$streak day streak"),
    cta = "Open history",
    target = Page.History,
    createdAt = createdAt,
    timeframe = ctx.timeframe,
  )
}

private fun habitGapInsight(ctx: InsightContext, createdAt: String): InsightItem? {
  val all = (ctx.filteredIncome + ctx.filteredExpense).mapNotNull { parseDate(it.date) }.sorted()
  if (all.size < 2) return null
  var maxGap = 0
  for (i in 1 until all.size) {
    val gap = ((all[i] - all[i - 1]) / (24L * 60L * 60L * 1000L)).toInt()
    if (gap > maxGap) maxGap = gap
  }
  if (maxGap < 4) return null
  return insight(
    id = "habit_gap_${ctx.timeframe}",
    title = "Tracking gap detected",
    message = "There are multi-day gaps in your transaction logging. Missing logs reduce insight quality and can hide budget drift.",
    severity = InsightSeverity.WARN,
    category = InsightCategory.Habits,
    chips = listOf("Max gap $maxGap days"),
    cta = "Open history",
    target = Page.History,
    createdAt = createdAt,
    timeframe = ctx.timeframe,
  )
}
