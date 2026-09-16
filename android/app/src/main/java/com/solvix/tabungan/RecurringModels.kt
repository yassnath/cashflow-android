package com.solvix.tabungan

import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class RecurringTransaction(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val amount: Int,
  val type: String = "Expense", // "Income" or "Expense"
  val category: String,
  val frequency: String = "Bulanan", // "Bulanan", "Mingguan", "Tahunan"
  val dueDay: Int = 1, // Tanggal 1 - 31
  val note: String = "",
  val sourceOrMethod: String = "Transfer",
  val channelOrBank: String = "Bank",
  val lastAppliedPeriod: String = "", // e.g. "2026-09"
  val isActive: Boolean = true,
)
