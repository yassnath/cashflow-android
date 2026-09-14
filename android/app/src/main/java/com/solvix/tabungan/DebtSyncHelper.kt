package com.solvix.tabungan

import android.content.SharedPreferences
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.content.edit
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Handles sync, detection, loading, and persistence of Debt/Loan entries.
 */
fun loanStorageKey(userId: String): String = "loan_entries_$userId"

fun persistLoanEntriesToPrefs(
  userId: String,
  entries: List<LoanEntry>,
  securePrefs: SharedPreferences,
  localJson: Json,
) {
  if (userId.isBlank()) return
  val jsonStr = runCatching { localJson.encodeToString(entries) }.getOrDefault("[]")
  securePrefs.edit { putString(loanStorageKey(userId), jsonStr) }
}

fun loadLoanEntriesFromPrefs(
  userId: String,
  securePrefs: SharedPreferences,
  localJson: Json,
): List<LoanEntry> {
  if (userId.isBlank()) return emptyList()
  val jsonStr = securePrefs.getString(loanStorageKey(userId), "[]") ?: "[]"
  return runCatching { localJson.decodeFromString<List<LoanEntry>>(jsonStr) }.getOrDefault(emptyList())
}

fun expenseDateOnly(entry: MoneyEntry): String {
  val datePart = entry.date.split(" ").firstOrNull().orEmpty()
  return datePart.ifBlank { entry.date }
}

fun deriveLoanTitleFromExpense(entry: MoneyEntry): String {
  if (entry.note.isNotBlank()) return entry.note
  if (entry.category.isNotBlank()) return entry.category
  return "Pinjaman"
}

fun detectDebtTypeHeuristic(entry: MoneyEntry): String? {
  val text = "${entry.category} ${entry.note} ${entry.sourceOrMethod}".lowercase()
  return when {
    text.contains("pinjam") || text.contains("hutang") || text.contains("utang") || text.contains("borrow") || text.contains("loan") -> "borrow"
    text.contains("piutang") || text.contains("lent") || text.contains("lend") -> "lend"
    else -> null
  }
}

fun syncDebtFromExpense(
  entry: MoneyEntry,
  detectedType: String?,
  userId: String,
  loanEntries: SnapshotStateList<LoanEntry>,
  securePrefs: SharedPreferences,
  localJson: Json,
) {
  fun saveLoan() = persistLoanEntriesToPrefs(userId, loanEntries.toList(), securePrefs, localJson)
  val existingIndex = loanEntries.indexOfFirst { it.linkedExpenseId == entry.id }
  if (detectedType == null) {
    if (existingIndex >= 0) {
      loanEntries.removeAt(existingIndex)
      saveLoan()
    }
    return
  }
  val due = expenseDateOnly(entry)
  if (existingIndex >= 0) {
    val e = loanEntries[existingIndex]
    loanEntries[existingIndex] = e.copy(
      type = detectedType,
      title = if (e.title.isBlank()) deriveLoanTitleFromExpense(entry) else e.title,
      dueDate = if (e.dueDate.isBlank()) due else e.dueDate,
      note = if (e.note.isBlank()) entry.note else e.note,
    )
    saveLoan()
    return
  }
  loanEntries.add(
    LoanEntry(
      id = java.util.UUID.randomUUID().toString(),
      type = detectedType,
      title = deriveLoanTitleFromExpense(entry),
      principal = entry.amount.coerceAtLeast(1),
      paid = 0,
      annualInterestRate = 0.0,
      monthlyPayment = (entry.amount / 6).coerceAtLeast(1),
      dueDate = due,
      note = entry.note,
      linkedExpenseId = entry.id,
    )
  )
  saveLoan()
}

fun syncAllDebtFromExpenses(
  userId: String,
  expenseEntries: List<MoneyEntry>,
  loanEntries: SnapshotStateList<LoanEntry>,
  securePrefs: SharedPreferences,
  localJson: Json,
) {
  if (userId.isBlank()) return
  var changed = false
  val expenseIds = expenseEntries.map { it.id }.toSet()
  val toRemove = loanEntries.filter { it.linkedExpenseId != null && it.linkedExpenseId !in expenseIds }
  if (toRemove.isNotEmpty()) {
    loanEntries.removeAll(toRemove.toSet())
    changed = true
  }
  expenseEntries.forEach { expense ->
    val type = detectDebtTypeHeuristic(expense)
    val existing = loanEntries.any { it.linkedExpenseId == expense.id }
    if (type != null && !existing) {
      loanEntries.add(
        LoanEntry(
          id = java.util.UUID.randomUUID().toString(),
          type = type,
          title = deriveLoanTitleFromExpense(expense),
          principal = expense.amount.coerceAtLeast(1),
          paid = 0,
          annualInterestRate = 0.0,
          monthlyPayment = (expense.amount / 6).coerceAtLeast(1),
          dueDate = expenseDateOnly(expense),
          note = expense.note,
          linkedExpenseId = expense.id,
        )
      )
      changed = true
    }
    if (type == null && existing) {
      loanEntries.removeAll { it.linkedExpenseId == expense.id }
      changed = true
    }
  }
  if (changed) {
    persistLoanEntriesToPrefs(userId, loanEntries.toList(), securePrefs, localJson)
  }
}

fun loadLoanEntries(
  userId: String,
  loanEntries: SnapshotStateList<LoanEntry>,
  securePrefs: SharedPreferences,
  localJson: Json,
) {
  val entries = loadLoanEntriesFromPrefs(userId, securePrefs, localJson)
  loanEntries.clear()
  loanEntries.addAll(entries)
  if (userId.isNotBlank()) {
    persistLoanEntriesToPrefs(userId, loanEntries.toList(), securePrefs, localJson)
  }
}
