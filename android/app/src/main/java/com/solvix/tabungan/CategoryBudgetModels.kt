package com.solvix.tabungan

import android.content.Context
import org.json.JSONObject

data class CategoryBudget(
  val category: String,
  val monthlyLimit: Int,
)

object CategoryBudgetManager {
  private const val PREFS_NAME = "category_budgets"
  private const val KEY_BUDGETS = "budgets_json"

  fun loadBudgets(context: Context): Map<String, Int> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val jsonString = prefs.getString(KEY_BUDGETS, null) ?: return emptyMap()
    return try {
      val json = JSONObject(jsonString)
      val map = mutableMapOf<String, Int>()
      json.keys().forEach { key ->
        map[key] = json.getInt(key)
      }
      map
    } catch (_: Exception) {
      emptyMap()
    }
  }

  fun saveBudget(context: Context, category: String, monthlyLimit: Int) {
    val current = loadBudgets(context).toMutableMap()
    if (monthlyLimit <= 0) {
      current.remove(category)
    } else {
      current[category] = monthlyLimit
    }
    val json = JSONObject()
    current.forEach { (k, v) -> json.put(k, v) }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .putString(KEY_BUDGETS, json.toString())
      .apply()
  }

  fun deleteBudget(context: Context, category: String) {
    saveBudget(context, category, 0)
  }
}
