package com.solvix.tabungan.pages.settings

import com.solvix.tabungan.DreamEntry
import com.solvix.tabungan.LoanEntry
import com.solvix.tabungan.MoneyEntry
import kotlinx.serialization.Serializable

@Serializable
data class FullAppBackup(
  val version: Int = 1,
  val exportedAt: Long = System.currentTimeMillis(),
  val appVersion: String = "1.0.0",
  val userId: String = "",
  val income: List<MoneyEntry> = emptyList(),
  val expense: List<MoneyEntry> = emptyList(),
  val transfers: List<MoneyEntry> = emptyList(),
  val dreams: List<DreamEntry> = emptyList(),
  val loans: List<LoanEntry> = emptyList(),
  val categoryBudgets: Map<String, Int> = emptyMap(),
)
