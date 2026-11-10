package com.solvix.tabungan

import kotlin.math.roundToInt

internal fun evaluateAllInsightRules(ctx: InsightContext, createdAt: String): List<InsightItem> {
  return listOfNotNull(
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
}

internal fun incomeDrop(ctx: InsightContext, createdAt: String): InsightItem? {
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

internal fun incomeDelayed(ctx: InsightContext, createdAt: String): InsightItem? {
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

internal fun incomeVariance(ctx: InsightContext, createdAt: String): InsightItem? {
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

internal fun savingsRateLow(ctx: InsightContext, createdAt: String): InsightItem? {
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

internal fun savingsRateStrong(ctx: InsightContext, createdAt: String): InsightItem? {
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
