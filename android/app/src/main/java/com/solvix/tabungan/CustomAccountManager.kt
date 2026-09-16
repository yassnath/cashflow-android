package com.solvix.tabungan

import android.content.Context

object CustomAccountManager {
  private const val PREFS_NAME = "custom_accounts_prefs"
  private const val KEY_CUSTOM_ACCOUNTS = "saved_custom_accounts"
  private const val KEY_CUSTOM_SOURCES = "saved_custom_sources"

  fun getCustomAccounts(context: Context): List<String> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return (prefs.getStringSet(KEY_CUSTOM_ACCOUNTS, emptySet()) ?: emptySet())
      .filter { it.isNotBlank() }
      .sorted()
  }

  fun addCustomAccount(context: Context, name: String) {
    val trimmed = name.trim()
    if (trimmed.isBlank() || trimmed.equals("Lainnya", ignoreCase = true) || trimmed.equals("Other", ignoreCase = true)) return
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val set = (prefs.getStringSet(KEY_CUSTOM_ACCOUNTS, emptySet()) ?: emptySet()).toMutableSet()
    set.add(trimmed)
    prefs.edit().putStringSet(KEY_CUSTOM_ACCOUNTS, set).apply()
  }

  fun getCustomSources(context: Context): List<String> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return (prefs.getStringSet(KEY_CUSTOM_SOURCES, emptySet()) ?: emptySet())
      .filter { it.isNotBlank() }
      .sorted()
  }

  fun addCustomSource(context: Context, name: String) {
    val trimmed = name.trim()
    if (trimmed.isBlank() || trimmed.equals("Lainnya", ignoreCase = true) || trimmed.equals("Other", ignoreCase = true)) return
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val set = (prefs.getStringSet(KEY_CUSTOM_SOURCES, emptySet()) ?: emptySet()).toMutableSet()
    set.add(trimmed)
    prefs.edit().putStringSet(KEY_CUSTOM_SOURCES, set).apply()
  }
}
