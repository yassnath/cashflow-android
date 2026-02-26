package com.solvix.tabungan

import org.junit.Assert.assertTrue
import org.junit.Test

class InsightsEngineTest {

  @Test
  fun generateInsightsDetectsWeeklySpendingIncrease() {
    val now = parseDateTimeMillis("20-02-2026 12:00")!!
    val expenses = listOf(
      expense(220_000, "18-02-2026 10:00"),
      expense(210_000, "16-02-2026 10:00"),
      expense(200_000, "14-02-2026 10:00"),
      expense(70_000, "10-02-2026 10:00"),
      expense(60_000, "08-02-2026 10:00"),
      expense(50_000, "06-02-2026 10:00"),
    )
    val insights = generateInsights(
      incomeEntries = emptyList(),
      expenseEntries = expenses,
      goals = emptyList(),
      debts = emptyList(),
      timeframe = InsightTimeframe.D30,
      nowMillis = now,
    )

    assertTrue(insights.any { it.id.startsWith("spending_ww_") })
  }

  @Test
  fun generateInsightsDetectsLowSavingsRate() {
    val now = parseDateTimeMillis("20-02-2026 12:00")!!
    val income = listOf(
      income(1_000_000, "10-02-2026 09:00"),
    )
    val expense = listOf(
      expense(930_000, "15-02-2026 11:00"),
    )
    val insights = generateInsights(
      incomeEntries = income,
      expenseEntries = expense,
      goals = emptyList(),
      debts = emptyList(),
      timeframe = InsightTimeframe.D30,
      nowMillis = now,
    )

    assertTrue(insights.any { it.id.startsWith("savings_low_") })
  }

  @Test
  fun generateInsightsSupportsAllTimeframe() {
    val now = parseDateTimeMillis("20-02-2026 12:00")!!
    val income = listOf(
      income(700_000, "10-01-2026 09:00"),
      income(900_000, "10-02-2026 09:00"),
    )
    val expense = listOf(
      expense(300_000, "12-01-2026 09:00"),
      expense(350_000, "12-02-2026 09:00"),
    )

    val insights = generateInsights(
      incomeEntries = income,
      expenseEntries = expense,
      goals = emptyList(),
      debts = emptyList(),
      timeframe = InsightTimeframe.ALL,
      nowMillis = now,
    )

    assertTrue(insights.isNotEmpty())
    assertTrue(insights.all { it.timeframe == InsightTimeframe.ALL })
  }

  @Test
  fun compactSummaryPrivateModeMasksAmounts() {
    val now = parseDateTimeMillis("20-02-2026 12:00")!!
    val payload = buildCompactInsightSummary(
      incomeEntries = listOf(income(1_250_000, "10-02-2026 09:00")),
      expenseEntries = listOf(expense(450_000, "12-02-2026 09:00")),
      goals = emptyList(),
      debts = emptyList(),
      timeframe = InsightTimeframe.D30,
      privateMode = true,
      nowMillis = now,
    )

    assertTrue(!payload.incomeTotal.contains("Rp"))
    assertTrue(!payload.expenseTotal.contains("Rp"))
  }

  private fun income(amount: Int, date: String, channel: String = "BCA"): MoneyEntry {
    return MoneyEntry(
      type = EntryType.Income,
      amount = amount,
      date = date,
      category = "Salary",
      note = "",
      sourceOrMethod = "Salary",
      channelOrBank = channel,
    )
  }

  private fun expense(amount: Int, date: String, channel: String = "BCA"): MoneyEntry {
    return MoneyEntry(
      type = EntryType.Expense,
      amount = amount,
      date = date,
      category = "General",
      note = "",
      sourceOrMethod = "Transfer",
      channelOrBank = channel,
    )
  }
}
