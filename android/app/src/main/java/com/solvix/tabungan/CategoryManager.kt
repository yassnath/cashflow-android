package com.solvix.tabungan

import android.content.Context

object CategoryManager {
  private const val PREFS_NAME = "custom_categories"
  private const val KEY_EXPENSE_CUSTOM = "custom_expense_cats"
  private const val KEY_INCOME_CUSTOM = "custom_income_cats"

  fun getExpenseCategories(context: Context): List<String> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val custom = prefs.getStringSet(KEY_EXPENSE_CUSTOM, emptySet()) ?: emptySet()
    return custom
      .filter { it.isNotBlank() && !it.equals("Lainnya", ignoreCase = true) && !it.equals("Other", ignoreCase = true) }
      .sorted()
  }

  fun addExpenseCategory(context: Context, name: String) {
    val trimmed = name.trim()
    if (trimmed.isBlank() || trimmed.equals("Lainnya", ignoreCase = true) || trimmed.equals("Other", ignoreCase = true)) return
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val set = (prefs.getStringSet(KEY_EXPENSE_CUSTOM, emptySet()) ?: emptySet()).toMutableSet()
    set.add(trimmed)
    prefs.edit().putStringSet(KEY_EXPENSE_CUSTOM, set).apply()
  }

  fun removeExpenseCategory(context: Context, name: String) {
    val trimmed = name.trim()
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val set = (prefs.getStringSet(KEY_EXPENSE_CUSTOM, emptySet()) ?: emptySet()).toMutableSet()
    set.remove(trimmed)
    prefs.edit().putStringSet(KEY_EXPENSE_CUSTOM, set).apply()
  }

  fun getIncomeCategories(context: Context): List<String> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val custom = prefs.getStringSet(KEY_INCOME_CUSTOM, emptySet()) ?: emptySet()
    return custom
      .filter { it.isNotBlank() && !it.equals("Lainnya", ignoreCase = true) && !it.equals("Other", ignoreCase = true) }
      .sorted()
  }

  fun addIncomeCategory(context: Context, name: String) {
    val trimmed = name.trim()
    if (trimmed.isBlank() || trimmed.equals("Lainnya", ignoreCase = true) || trimmed.equals("Other", ignoreCase = true)) return
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val set = (prefs.getStringSet(KEY_INCOME_CUSTOM, emptySet()) ?: emptySet()).toMutableSet()
    set.add(trimmed)
    prefs.edit().putStringSet(KEY_INCOME_CUSTOM, set).apply()
  }

  fun removeIncomeCategory(context: Context, name: String) {
    val trimmed = name.trim()
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val set = (prefs.getStringSet(KEY_INCOME_CUSTOM, emptySet()) ?: emptySet()).toMutableSet()
    set.remove(trimmed)
    prefs.edit().putStringSet(KEY_INCOME_CUSTOM, set).apply()
  }
}

