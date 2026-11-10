package com.solvix.tabungan

import kotlin.math.roundToInt

internal fun goalDailyNeed(ctx: InsightContext, createdAt: String): InsightItem? {
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

internal fun goalBehindSchedule(ctx: InsightContext, createdAt: String): InsightItem? {
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

internal fun goalAheadSchedule(ctx: InsightContext, createdAt: String): InsightItem? {
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

internal fun runwayInsight(ctx: InsightContext, createdAt: String): InsightItem? {
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

internal fun habitStreakInsight(ctx: InsightContext, createdAt: String): InsightItem? {
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

internal fun habitGapInsight(ctx: InsightContext, createdAt: String): InsightItem? {
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
