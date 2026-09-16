package com.solvix.tabungan

import androidx.compose.ui.graphics.Color

fun buildCountrySlices(
  users: List<SupabaseUser>,
  strings: AppStrings,
  palette: List<Color>,
  limit: Int = 5,
): List<DonutSlice> {
  if (users.isEmpty()) return emptyList()
  val counts = users
    .map { it.country.trim().ifBlank { strings["admin_unknown_country"] } }
    .groupingBy { it }
    .eachCount()
  val sorted = counts.entries.sortedByDescending { it.value }
  val top = sorted.take(limit).map { it.key to it.value }.toMutableList()
  val otherCount = sorted.drop(limit).sumOf { it.value }
  if (otherCount > 0) {
    top.add(strings["admin_other"] to otherCount)
  }
  return top
    .filter { it.second > 0 }
    .mapIndexed { index, (label, value) ->
      DonutSlice(label = label, value = value, color = palette[index % palette.size])
    }
}

fun buildRecencySlices(
  users: List<SupabaseUser>,
  strings: AppStrings,
  palette: List<Color>,
): List<DonutSlice> {
  if (users.isEmpty()) return emptyList()
  val now = System.currentTimeMillis()
  var last7 = 0
  var last30 = 0
  var last90 = 0
  var older = 0
  var unknown = 0
  users.forEach { user ->
    val millis = parseCreatedAtMillis(user.createdAt)
    if (millis == null) {
      unknown += 1
    } else {
      val days = ((now - millis) / 86_400_000L).coerceAtLeast(0L)
      when {
        days <= 7 -> last7 += 1
        days <= 30 -> last30 += 1
        days <= 90 -> last90 += 1
        else -> older += 1
      }
    }
  }
  val buckets = mutableListOf(
    strings["admin_recency_7d"] to last7,
    strings["admin_recency_30d"] to last30,
    strings["admin_recency_90d"] to last90,
    strings["admin_recency_older"] to older,
  )
  if (unknown > 0) {
    buckets.add(strings["admin_recency_unknown"] to unknown)
  }
  return buckets
    .filter { it.second > 0 }
    .mapIndexed { index, (label, value) ->
      DonutSlice(label = label, value = value, color = palette[index % palette.size])
    }
}
