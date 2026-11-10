package com.solvix.tabungan

import kotlin.math.roundToInt

internal fun debtToIncomeRatio(ctx: InsightContext, createdAt: String): InsightItem? {
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

internal fun debtPayoffRisk(ctx: InsightContext, createdAt: String): InsightItem? {
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

internal fun debtInterestWarning(ctx: InsightContext, createdAt: String): InsightItem? {
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

internal fun debtTimelineWarning(ctx: InsightContext, createdAt: String): InsightItem? {
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
