package com.solvix.tabungan

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

/**
 * Manajer keamanan PIN 6-digit aplikasi Tabungan CashFlow.
 * Menyimpan hash PIN dan salt secara lokal di SharedPreferences terproteksi,
 * serta menyinkronkan ke database Supabase (user_security_settings).
 */
object AppPinSecurityManager {
  private const val PREFS_NAME = "app_pin_security_prefs"
  private const val KEY_PIN_HASH = "pin_hash"
  private const val KEY_PIN_SALT = "pin_salt"
  private const val KEY_PIN_ENABLED = "pin_enabled"

  val versionState = mutableIntStateOf(0)
  val isPinEnabledState = mutableStateOf(false)

  fun init(context: Context) {
    isPinEnabledState.value = isPinSet(context)
  }

  fun isPinSet(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val hash = prefs.getString(KEY_PIN_HASH, null)
    val enabled = prefs.getBoolean(KEY_PIN_ENABLED, false)
    return !hash.isNullOrBlank() && enabled
  }

  fun verifyPin(context: Context, enteredPin: String): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
    val salt = prefs.getString(KEY_PIN_SALT, "") ?: ""
    val computedHash = hashWithSalt(enteredPin, salt)
    return storedHash == computedHash
  }

  fun setPin(context: Context, userId: String, pin: String) {
    val salt = UUID.randomUUID().toString()
    val hash = hashWithSalt(pin, salt)
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit()
      .putString(KEY_PIN_HASH, hash)
      .putString(KEY_PIN_SALT, salt)
      .putBoolean(KEY_PIN_ENABLED, true)
      .apply()

    isPinEnabledState.value = true
    versionState.intValue++

    if (userId.isNotBlank()) {
      CoroutineScope(Dispatchers.IO).launch {
        runCatching {
          SupabaseRepository.upsertSecurityPin(userId, hash, salt, true)
        }.onFailure {
          SyncQueueManager.enqueueUpsertPin(context, userId, hash, salt)
        }
      }
    }
  }

  fun removePin(context: Context, userId: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit()
      .remove(KEY_PIN_HASH)
      .remove(KEY_PIN_SALT)
      .putBoolean(KEY_PIN_ENABLED, false)
      .apply()

    isPinEnabledState.value = false
    versionState.intValue++

    if (userId.isNotBlank()) {
      CoroutineScope(Dispatchers.IO).launch {
        runCatching {
          SupabaseRepository.deleteSecurityPin(userId)
        }.onFailure {
          SyncQueueManager.enqueueDeletePin(context, userId)
        }
      }
    }
  }

  suspend fun syncFromCloud(context: Context, userId: String) {
    if (userId.isBlank()) return
    val cloud = runCatching { SupabaseRepository.fetchSecurityPin(userId) }.getOrNull() ?: return
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    if (cloud.pinEnabled && cloud.pinHash.isNotBlank()) {
      prefs.edit()
        .putString(KEY_PIN_HASH, cloud.pinHash)
        .putString(KEY_PIN_SALT, cloud.pinSalt)
        .putBoolean(KEY_PIN_ENABLED, true)
        .apply()
      isPinEnabledState.value = true
      versionState.intValue++
    } else if (!cloud.pinEnabled) {
      prefs.edit()
        .remove(KEY_PIN_HASH)
        .remove(KEY_PIN_SALT)
        .putBoolean(KEY_PIN_ENABLED, false)
        .apply()
      isPinEnabledState.value = false
      versionState.intValue++
    }
  }

  fun hashWithSalt(pin: String, salt: String): String {
    val input = "$salt:$pin"
    val digest = MessageDigest.getInstance("SHA-256")
    val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
  }
}

