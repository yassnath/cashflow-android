package com.solvix.tabungan

import android.content.Context

object CategoryManager {
  val defaultExpenseCategories = listOf(
    "Makanan & Minuman",
    "Transportasi",
    "Belanja",
    "Tagihan & Utilitas",
    "Hiburan & Rekreasi",
    "Kesehatan & Obat",
    "Pendidikan",
    "Keluarga & Anak",
    "Investasi",
    "Lainnya",
  )

  val defaultIncomeCategories = listOf(
    "Gaji Pokok",
    "Bonus & THR",
    "Penjualan & Bisnis",
    "Investasi & Dividen",
    "Hadiah & Hibah",
    "Freelance",
    "Lainnya",
  )

  private const val PREFS_NAME = "custom_categories"
  private const val KEY_EXPENSE_CUSTOM = "custom_expense_cats"
  private const val KEY_INCOME_CUSTOM = "custom_income_cats"

  fun getExpenseCategories(context: Context): List<String> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val custom = prefs.getStringSet(KEY_EXPENSE_CUSTOM, emptySet()) ?: emptySet()
    return (defaultExpenseCategories + custom.toList()).distinct()
  }

  fun addExpenseCategory(context: Context, name: String) {
    val trimmed = name.trim()
    if (trimmed.isBlank()) return
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val set = (prefs.getStringSet(KEY_EXPENSE_CUSTOM, emptySet()) ?: emptySet()).toMutableSet()
    set.add(trimmed)
    prefs.edit().putStringSet(KEY_EXPENSE_CUSTOM, set).apply()
  }

  fun getIncomeCategories(context: Context): List<String> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val custom = prefs.getStringSet(KEY_INCOME_CUSTOM, emptySet()) ?: emptySet()
    return (defaultIncomeCategories + custom.toList()).distinct()
  }

  fun addIncomeCategory(context: Context, name: String) {
    val trimmed = name.trim()
    if (trimmed.isBlank()) return
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val set = (prefs.getStringSet(KEY_INCOME_CUSTOM, emptySet()) ?: emptySet()).toMutableSet()
    set.add(trimmed)
    prefs.edit().putStringSet(KEY_INCOME_CUSTOM, set).apply()
  }
}
