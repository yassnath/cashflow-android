package com.solvix.tabungan

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

object InitialBalanceManager {
  private const val PREFS_NAME = "tabungan_prefs"
  private const val KEY_INITIAL_BALANCES = "account_initial_balances"

  // Live reactive state agar seluruh layar (Dashboard, Assets, Modal) otomatis sinkron seketika
  val balancesState = mutableStateOf<Map<String, Long>>(emptyMap())
  val versionState = mutableIntStateOf(0)

  // Rekening standar yang umum dimiliki pengguna
  val DEFAULT_ACCOUNTS = listOf(
    "Bank BCA",
    "Bank Mandiri",
    "Bank BRI",
    "Bank BNI",
    "Bank BSI",
    "Bank Jago",
    "SeaBank",
    "GoPay",
    "OVO",
    "DANA",
    "ShopeePay",
    "Uang Tunai (Cash)",
    "Investasi / Saham",
    "Aset Kripto (Crypto)",
  )

  fun getInitialBalances(context: Context): Map<String, Long> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val raw = prefs.getString(KEY_INITIAL_BALANCES, null)
    if (raw == null) {
      balancesState.value = emptyMap()
      return emptyMap()
    }
    val result = mutableMapOf<String, Long>()
    try {
      val json = JSONObject(raw)
      val keys = json.keys()
      while (keys.hasNext()) {
        val key = keys.next()
        val amount = json.optLong(key, 0L)
        if (amount != 0L) {
          result[key] = amount
        }
      }
    } catch (_: Exception) {
      // ignore
    }
    balancesState.value = result
    return result
  }

  fun setInitialBalance(
    context: Context,
    accountName: String,
    amount: Long,
    userId: String = "",
    scope: kotlinx.coroutines.CoroutineScope? = null,
  ) {
    val current = getInitialBalances(context).toMutableMap()
    val trimmed = accountName.trim()
    if (amount > 0L) {
      current[trimmed] = amount
    } else {
      current.remove(trimmed)
    }
    saveMap(context, current)

    if (userId.isNotBlank()) {
      val coroutineScope = scope ?: kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
      coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        if (amount > 0L) {
          SyncQueueManager.enqueueUpsertInitialBalance(context, userId, trimmed, amount)
        } else {
          SyncQueueManager.enqueueDeleteInitialBalance(context, userId, trimmed)
        }
        SyncQueueManager.processQueue(context)
      }
    }
  }

  fun removeInitialBalance(
    context: Context,
    accountName: String,
    userId: String = "",
    scope: kotlinx.coroutines.CoroutineScope? = null,
  ) {
    val trimmed = accountName.trim()
    val current = getInitialBalances(context).toMutableMap()
    current.remove(trimmed)
    saveMap(context, current)

    if (userId.isNotBlank()) {
      val coroutineScope = scope ?: kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
      coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        SyncQueueManager.enqueueDeleteInitialBalance(context, userId, trimmed)
        SyncQueueManager.processQueue(context)
      }
    }
  }

  /**
   * Melakukan sinkronisasi saldo awal antara lokal dan cloud Supabase.
   * Tidak akan menimpa data lokal yang baru diubah pengguna dengan data cloud lama.
   */
  suspend fun syncFromCloud(context: Context, userId: String): Map<String, Long> {
    if (userId.isBlank()) return getInitialBalances(context)
    return try {
      val cloudBalances = SupabaseRepository.fetchInitialBalances(userId)
      val localBalances = getInitialBalances(context)

      if (localBalances.isEmpty() && cloudBalances.isNotEmpty()) {
        // Kasus 1: Perangkat baru / baru login, unduh saldo awal dari cloud
        saveMap(context, cloudBalances)
        cloudBalances
      } else if (localBalances.isNotEmpty()) {
        // Kasus 2: Perangkat memiliki saldo lokal (perubahan user).
        // Gabungkan: utamakan lokal, dan tambahkan akun dari cloud jika lokal belum punya
        val merged = (cloudBalances + localBalances).toMutableMap()
        saveMap(context, merged)

        // Pastikan perubahan lokal dikirimkan ke cloud Supabase
        localBalances.forEach { (acc, bal) ->
          SupabaseRepository.upsertInitialBalance(userId, acc, bal)
        }
        merged
      } else {
        emptyMap()
      }
    } catch (_: Exception) {
      getInitialBalances(context)
    }
  }

  suspend fun deleteAllInitialBalancesFromCloud(context: Context, userId: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit { remove(KEY_INITIAL_BALANCES) }
    balancesState.value = emptyMap()
    versionState.intValue++
    if (userId.isNotBlank()) {
      SupabaseRepository.deleteAllInitialBalances(userId)
    }
  }

  fun saveMap(context: Context, map: Map<String, Long>) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val json = JSONObject()
    map.forEach { (k, v) ->
      json.put(k, v)
    }
    prefs.edit { putString(KEY_INITIAL_BALANCES, json.toString()) }
    balancesState.value = map
    versionState.intValue++
  }

  fun getTotalInitialBalance(context: Context): Long {
    val map = balancesState.value.ifEmpty { getInitialBalances(context) }
    return map.values.sum()
  }
}
