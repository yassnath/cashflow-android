package com.solvix.tabungan

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

private const val SECURE_PREFS_FILE = "tabungan_secure_prefs"

object SecurePrefs {
  fun open(context: Context): SharedPreferences {
    return runCatching {
      val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
      EncryptedSharedPreferences.create(
        context,
        SECURE_PREFS_FILE,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
      )
    }.getOrElse {
      context.getSharedPreferences(SECURE_PREFS_FILE, Context.MODE_PRIVATE)
    }
  }
}

