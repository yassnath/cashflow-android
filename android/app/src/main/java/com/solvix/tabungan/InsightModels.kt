package com.solvix.tabungan

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

data class InsightContext(
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
