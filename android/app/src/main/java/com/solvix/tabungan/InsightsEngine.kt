package com.solvix.tabungan

import java.util.Calendar
import kotlin.math.abs
import kotlin.math.roundToInt

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
  val rules = evaluateAllInsightRules(ctx, createdAt)
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
  return entries.filter {
    val ts = parseDateTimeMillis(it.date) ?: return@filter false
    ts in start..nowMillis
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
