package com.solvix.tabungan

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BudgetAlertHelper {

  data class BudgetCheckResult(
    val hasBudget: Boolean,
    val category: String,
    val monthlyLimit: Int,
    val projectedTotal: Int,
    val percentage: Int,
    val isExceeded: Boolean,
    val isNearLimit: Boolean,
    val message: String,
  )

  fun checkBudgetStatus(
    context: Context,
    category: String,
    newAmount: Int,
    existingExpenses: List<MoneyEntry>,
    editingEntryId: String? = null,
  ): BudgetCheckResult? {
    if (category.isBlank() || newAmount <= 0) return null
    val budgets = CategoryBudgetManager.loadBudgets(context)
    val limit = budgets[category] ?: return null
    if (limit <= 0) return null

    val currentMonthYear = SimpleDateFormat("MM-yyyy", Locale.US).format(Date())
    val spentThisMonth = existingExpenses
      .filter { it.type == EntryType.Expense && it.category.equals(category, ignoreCase = true) }
      .filter { it.id != editingEntryId }
      .filter {
        val entryDate = it.date.split(" ").firstOrNull().orEmpty()
        val parts = entryDate.split("-")
        if (parts.size == 3) {
          "${parts[1]}-${parts[2]}" == currentMonthYear
        } else false
      }
      .sumOf { it.amount }

    val totalProjected = spentThisMonth + newAmount
    val percentage = ((totalProjected.toDouble() / limit.toDouble()) * 100).toInt()
    val isExceeded = totalProjected > limit
    val isNearLimit = percentage >= 80

    val message = when {
      isExceeded -> "⚠️ Pengeluaran '$category' telah melampaui batas anggaran bulanan ($percentage%)!"
      isNearLimit -> "⚡ Pengeluaran '$category' telah mencapai $percentage% dari anggaran bulanan."
      else -> ""
    }

    return BudgetCheckResult(
      hasBudget = true,
      category = category,
      monthlyLimit = limit,
      projectedTotal = totalProjected,
      percentage = percentage,
      isExceeded = isExceeded,
      isNearLimit = isNearLimit,
      message = message,
    )
  }
}
