package com.solvix.tabungan

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

fun isIncomeType(type: String): Boolean {
  val t = type.trim()
  return t.equals("Income", ignoreCase = true) ||
    t.equals("income", ignoreCase = true) ||
    t.startsWith("inc", ignoreCase = true) ||
    t == "0"
}

fun isExpenseType(type: String): Boolean {
  val t = type.trim()
  return t.equals("Expense", ignoreCase = true) ||
    t.equals("expense", ignoreCase = true) ||
    t.startsWith("exp", ignoreCase = true) ||
    t == "1"
}

data class LoadedUserData(
  val income: List<MoneyEntry>,
  val expense: List<MoneyEntry>,
  val dreams: List<DreamEntry>,
  val pendingDateUpdates: List<Pair<String, String>>,
)

suspend fun fetchUserData(userId: String, authId: String): LoadedUserData {
  val moneyRows = mutableListOf<SupabaseMoneyEntry>()
  val dreamRows = mutableListOf<SupabaseDreamEntry>()

  runCatching {
    val byUserId = SupabaseClient.client
      .from("money_entries")
      .select { filter { eq("user_id", userId) } }
      .decodeList<SupabaseMoneyEntry>()
    moneyRows.addAll(byUserId)

    if (authId.isNotBlank() && authId != userId) {
      val byAuthId = SupabaseClient.client
        .from("money_entries")
        .select { filter { eq("user_id", authId) } }
        .decodeList<SupabaseMoneyEntry>()
      moneyRows.addAll(byAuthId)
    }
  }

  if (moneyRows.isEmpty()) {
    runCatching {
      val rpcResult = SupabaseClient.client.postgrest.rpc(
        function = "fetch_user_money_entries",
        parameters = buildJsonObject { put("p_user_id", userId) },
      ).decodeList<SupabaseMoneyEntry>()
      moneyRows.addAll(rpcResult)
    }
    if (moneyRows.isEmpty() && authId.isNotBlank() && authId != userId) {
      runCatching {
        val rpcResult = SupabaseClient.client.postgrest.rpc(
          function = "fetch_user_money_entries",
          parameters = buildJsonObject { put("p_user_id", authId) },
        ).decodeList<SupabaseMoneyEntry>()
        moneyRows.addAll(rpcResult)
      }
    }
  }

  runCatching {
    val byUserId = SupabaseClient.client
      .from("dream_entries")
      .select { filter { eq("user_id", userId) } }
      .decodeList<SupabaseDreamEntry>()
    dreamRows.addAll(byUserId)

    if (authId.isNotBlank() && authId != userId) {
      val byAuthId = SupabaseClient.client
        .from("dream_entries")
        .select { filter { eq("user_id", authId) } }
        .decodeList<SupabaseDreamEntry>()
      dreamRows.addAll(byAuthId)
    }
  }

  if (dreamRows.isEmpty()) {
    runCatching {
      val rpcResult = SupabaseClient.client.postgrest.rpc(
        function = "fetch_user_dream_entries",
        parameters = buildJsonObject { put("p_user_id", userId) },
      ).decodeList<SupabaseDreamEntry>()
      dreamRows.addAll(rpcResult)
    }
    if (dreamRows.isEmpty() && authId.isNotBlank() && authId != userId) {
      runCatching {
        val rpcResult = SupabaseClient.client.postgrest.rpc(
          function = "fetch_user_dream_entries",
          parameters = buildJsonObject { put("p_user_id", authId) },
        ).decodeList<SupabaseDreamEntry>()
        dreamRows.addAll(rpcResult)
      }
    }
  }

  val distinctMoney = moneyRows.distinctBy { it.id }
  val distinctDreams = dreamRows.distinctBy { it.id }
  val pendingDateUpdates = mutableListOf<Pair<String, String>>()

  val income = distinctMoney.filter { isIncomeType(it.type) }.map { row ->
    val fallbackTime = formatTimeFromCreatedAt(row.createdAt)
    val normalizedDate = if (fallbackTime != null) ensureDateHasTime(row.date, fallbackTime) else row.date
    if (normalizedDate != row.date && fallbackTime != null) pendingDateUpdates.add(row.id to normalizedDate)
    MoneyEntry(
      id = row.id, type = EntryType.Income, amount = row.amountInt, date = normalizedDate,
      category = row.category, note = row.note, sourceOrMethod = row.sourceOrMethod,
      channelOrBank = row.channelOrBank, createdAt = row.createdAt,
    )
  }

  val expense = distinctMoney.filter { isExpenseType(it.type) }.map { row ->
    val fallbackTime = formatTimeFromCreatedAt(row.createdAt)
    val normalizedDate = if (fallbackTime != null) ensureDateHasTime(row.date, fallbackTime) else row.date
    if (normalizedDate != row.date && fallbackTime != null) pendingDateUpdates.add(row.id to normalizedDate)
    MoneyEntry(
      id = row.id, type = EntryType.Expense, amount = row.amountInt, date = normalizedDate,
      category = row.category, note = row.note, sourceOrMethod = row.sourceOrMethod,
      channelOrBank = row.channelOrBank, createdAt = row.createdAt,
    )
  }

  val dreams = distinctDreams.map { row ->
    DreamEntry(
      id = row.id, title = row.title, target = row.targetInt, current = row.currentInt,
      deadline = row.deadline, note = row.note, sourceType = row.sourceType.ifBlank { "income" },
    )
  }

  return LoadedUserData(income, expense, dreams, pendingDateUpdates)
}
