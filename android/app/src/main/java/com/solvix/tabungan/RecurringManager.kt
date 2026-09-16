package com.solvix.tabungan

import android.content.Context
import androidx.core.content.edit
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object RecurringManager {
  private const val PREFS_NAME = "tabungan_recurring"
  private const val KEY_ITEMS = "recurring_items"
  private val json = Json { ignoreUnknownKeys = true }

  fun getAll(context: Context): List<RecurringTransaction> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val raw = prefs.getString(KEY_ITEMS, null) ?: return defaultTemplates()
    return try {
      json.decodeFromString<List<RecurringTransaction>>(raw)
    } catch (_: Exception) {
      defaultTemplates()
    }
  }

  fun save(context: Context, items: List<RecurringTransaction>) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val raw = json.encodeToString(items)
    prefs.edit { putString(KEY_ITEMS, raw) }
  }

  fun add(context: Context, item: RecurringTransaction) {
    val current = getAll(context).toMutableList()
    current.add(item)
    save(context, current)
  }

  fun update(context: Context, updated: RecurringTransaction) {
    val current = getAll(context).map { if (it.id == updated.id) updated else it }
    save(context, current)
  }

  fun delete(context: Context, id: String) {
    val current = getAll(context).filter { it.id != id }
    save(context, current)
  }

  fun toggleActive(context: Context, id: String) {
    val current = getAll(context).map {
      if (it.id == id) it.copy(isActive = !it.isActive) else it
    }
    save(context, current)
  }

  /**
   * Mengecek apakah ada transaksi rutin yang jatuh tempo di bulan berjalan
   * dan belum pernah dicatat secara otomatis.
   */
  fun checkAndGenerateDueTransactions(
    context: Context,
    onGenerate: (MoneyEntry) -> Unit,
  ): Int {
    val now = Calendar.getInstance()
    val currentDay = now.get(Calendar.DAY_OF_MONTH)
    val currentPeriod = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
    val dateText = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US).format(Date())

    val all = getAll(context)
    var generatedCount = 0
    val updatedList = all.map { item ->
      if (item.isActive && item.lastAppliedPeriod != currentPeriod && currentDay >= item.dueDay) {
        val entryType = if (item.type.equals("Income", ignoreCase = true)) EntryType.Income else EntryType.Expense
        val entry = MoneyEntry(
          type = entryType,
          amount = item.amount,
          date = dateText,
          category = item.category,
          note = "[Rutin] ${item.title}${if (item.note.isNotBlank()) " - ${item.note}" else ""}",
          sourceOrMethod = item.sourceOrMethod,
          channelOrBank = item.channelOrBank,
          createdAt = dateText,
        )
        onGenerate(entry)
        generatedCount++
        item.copy(lastAppliedPeriod = currentPeriod)
      } else {
        item
      }
    }

    if (generatedCount > 0) {
      save(context, updatedList)
    }
    return generatedCount
  }

  private fun defaultTemplates(): List<RecurringTransaction> {
    return listOf(
      RecurringTransaction(
        title = "Langganan Netflix",
        amount = 186000,
        type = "Expense",
        category = "Hiburan & Langganan",
        dueDay = 5,
        note = "Paket Premium 4K",
        channelOrBank = "BCA",
        isActive = false,
      ),
      RecurringTransaction(
        title = "Tagihan Internet & WiFi",
        amount = 350000,
        type = "Expense",
        category = "Tagihan & Utilitas",
        dueDay = 15,
        note = "IndiHome / Biznet",
        channelOrBank = "Mandiri",
        isActive = false,
      ),
    )
  }
}
