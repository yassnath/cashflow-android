package com.solvix.tabungan

import java.util.Calendar
import kotlin.math.roundToInt

internal fun spendingWeekVsWeek(ctx: InsightContext, createdAt: String): InsightItem? {
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

internal fun spendingMonthVsMonth(ctx: InsightContext, createdAt: String): InsightItem? {
  val now = Calendar.getInstance().apply { timeInMillis = ctx.nowMillis }
  val thisMonth = ctx.allExpense.filter {
    val d = parseDate(it.date) ?: return@filter false
    val c = Calendar.getInstance().apply { timeInMillis = d }
    c.get(Calendar.YEAR) == now.get(Calendar.YEAR) && c.get(Calendar.MONTH) == now.get(Calendar.MONTH)
  }.sumOf { it.amount }

  val lastMonthCal = (now.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
  val prevMonth = ctx.allExpense.filter {
    val d = parseDate(it.date) ?: return@filter false
    val c = Calendar.getInstance().apply { timeInMillis = d }
    c.get(Calendar.YEAR) == lastMonthCal.get(Calendar.YEAR) && c.get(Calendar.MONTH) == lastMonthCal.get(Calendar.MONTH)
  }.sumOf { it.amount }

  if (prevMonth <= 0) return null
  val pct = (((thisMonth - prevMonth).toDouble() / prevMonth) * 100).roundToInt()
  if (pct < 15) return null
  return insight(
    id = "spending_mm_${ctx.timeframe}",
    title = "Monthly spending is accelerating",
    message = "This month's expenses already exceed the previous month's baseline. Recheck daily discretionary spending.",
    severity = if (pct >= 35) InsightSeverity.ALERT else InsightSeverity.WARN,
    category = InsightCategory.Spending,
    chips = listOf("+$pct% vs last month", "This month ${formatRupiah(thisMonth)}"),
    cta = "Review budget",
    target = Page.Expense,
    createdAt = createdAt,
    timeframe = ctx.timeframe,
  )
}

internal fun spendingCategorySpike(ctx: InsightContext, createdAt: String): InsightItem? {
  val recentEntries = ctx.allExpense.filter {
    val ts = parseDateTimeMillis(it.date) ?: return@filter false
    ts in (ctx.nowMillis - 7L * 24L * 60L * 60L * 1000L)..ctx.nowMillis
  }
  if (recentEntries.isEmpty()) return null
  val grouped = recentEntries.groupBy { it.category }
  val top = grouped.maxByOrNull { entry -> entry.value.sumOf { it.amount } } ?: return null
  val topAmount = top.value.sumOf { it.amount }
  val totalRecent = recentEntries.sumOf { it.amount }
  if (totalRecent <= 0) return null
  val share = ((topAmount.toDouble() / totalRecent) * 100).roundToInt()
  if (share < 45 || topAmount < 150_000) return null
  return insight(
    id = "category_spike_${ctx.timeframe}",
    title = "Category spike in ${top.key}",
    message = "A single category took most of your spending in the last 7 days. Setting a weekly sub-cap will keep total expenses under control.",
    severity = if (share >= 65) InsightSeverity.ALERT else InsightSeverity.WARN,
    category = InsightCategory.Spending,
    chips = listOf("${top.key} $share% of total", "Total ${formatRupiah(topAmount)}"),
    cta = "Check category",
    target = Page.Expense,
    createdAt = createdAt,
    timeframe = ctx.timeframe,
  )
}

internal fun spendingUnusualSingle(ctx: InsightContext, createdAt: String): InsightItem? {
  val recentExpenses = ctx.allExpense.filter {
    val ts = parseDateTimeMillis(it.date) ?: return@filter false
    ts in (ctx.nowMillis - 14L * 24L * 60L * 60L * 1000L)..ctx.nowMillis
  }
  if (recentExpenses.size < 5) return null
  val median = recentExpenses.map { it.amount }.sorted().let { sorted ->
    val mid = sorted.size / 2
    if (sorted.size % 2 == 0) (sorted[mid - 1] + sorted[mid]) / 2 else sorted[mid]
  }
  if (median <= 0) return null
  val largest = recentExpenses.maxByOrNull { it.amount } ?: return null
  if (largest.amount < (median * 3.5).roundToInt() || largest.amount < 250_000) return null
  return insight(
    id = "unusual_transaction_${ctx.timeframe}",
    title = "Unusual transaction detected",
    message = "You had an expense significantly larger than your median transaction size (${formatRupiah(median)}). Verify if this is recurring or one-off.",
    severity = InsightSeverity.WARN,
    category = InsightCategory.Spending,
    chips = listOf(largest.category, formatRupiah(largest.amount), "Median ${formatRupiah(median)}"),
    cta = "Inspect transaction",
    target = Page.History,
    createdAt = createdAt,
    timeframe = ctx.timeframe,
  )
}
