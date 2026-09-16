package com.solvix.tabungan

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class CachedUserData(
  val userId: String,
  val income: List<MoneyEntry> = emptyList(),
  val expense: List<MoneyEntry> = emptyList(),
  val transfers: List<MoneyEntry> = emptyList(),
  val dreams: List<DreamEntry> = emptyList(),
  val lastUpdated: Long = System.currentTimeMillis(),
)

object LocalDataCache {

  private val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = false
  }

  private fun getCacheFile(context: Context, userId: String): File {
    val dir = File(context.filesDir, "data_cache").apply { mkdirs() }
    val safeId = userId.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_")
    return File(dir, "user_cache_${safeId}.json")
  }

  fun save(
    context: Context,
    userId: String,
    income: List<MoneyEntry>,
    expense: List<MoneyEntry>,
    dreams: List<DreamEntry>,
    transfers: List<MoneyEntry> = emptyList(),
  ) {
    if (userId.isBlank()) return
    try {
      val file = getCacheFile(context, userId)
      val data = CachedUserData(
        userId = userId,
        income = income,
        expense = expense,
        transfers = transfers,
        dreams = dreams,
        lastUpdated = System.currentTimeMillis(),
      )
      file.writeText(json.encodeToString(data))
    } catch (_: Exception) {}
  }

  fun load(context: Context, userId: String): CachedUserData? {
    if (userId.isBlank()) return null
    return try {
      val file = getCacheFile(context, userId)
      if (file.exists() && file.length() > 0) {
        json.decodeFromString<CachedUserData>(file.readText())
      } else null
    } catch (_: Exception) {
      null
    }
  }

  fun clear(context: Context, userId: String) {
    if (userId.isBlank()) return
    runCatching { getCacheFile(context, userId).delete() }
  }
}
