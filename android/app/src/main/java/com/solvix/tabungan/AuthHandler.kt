package com.solvix.tabungan

import android.content.Context
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.content.edit
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Encapsulates authentication action flows (change password, delete account, logout).
 */
fun performChangePassword(
  userId: String,
  currentUser: UserProfile?,
  currentPassword: String,
  newPassword: String,
  confirmPassword: String,
  strings: AppStrings,
  scope: CoroutineScope,
  securePrefs: android.content.SharedPreferences,
  prefs: android.content.SharedPreferences,
  onAlert: (String) -> Unit,
  onPasswordChangeSuccess: () -> Unit,
) {
  if (userId.isBlank()) {
    onAlert(strings["password_change_failed"])
    return
  }
  if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
    onAlert(strings["password_validation_empty"])
    return
  }
  if (newPassword == currentPassword) {
    onAlert(strings["password_validation_same_as_current"])
    return
  }
  if (newPassword != confirmPassword) {
    onAlert(strings["password_validation_mismatch"])
    return
  }
  val passwordError = validatePasswordLocalized(newPassword, strings)
  if (passwordError != null) {
    onAlert(passwordError)
    return
  }
  scope.launch(Dispatchers.IO) {
    try {
      val serverUser = SupabaseRepository.fetchUserById(userId)
      val validCurrent = if (serverUser == null) false
      else if (serverUser.authId.isNotBlank() && serverUser.email.isNotBlank())
        runCatching { SupabaseRepository.signInSupabaseAuth(serverUser.email, currentPassword) }.getOrNull() == serverUser.authId
      else PasswordSecurity.verifyPassword(currentPassword, serverUser.password)
      withContext(Dispatchers.Main) {
        if (!validCurrent) {
          onAlert(strings["password_validation_current_invalid"])
          return@withContext
        }
      }
      if (serverUser?.authId?.isNotBlank() == true) {
        SupabaseClient.client.auth.updateUser { password = newPassword }
      } else {
        SupabaseRepository.updateUserPasswordHash(userId, PasswordSecurity.hashPassword(newPassword))
      }
      runCatching { SupabaseClient.client.auth.signOut() }
      withContext(Dispatchers.Main) {
        onPasswordChangeSuccess()
      }
    } catch (_: Exception) {
      withContext(Dispatchers.Main) {
        onAlert(strings["password_change_failed"])
      }
    }
  }
}

fun performDeleteAccount(
  userId: String,
  strings: AppStrings,
  scope: CoroutineScope,
  securePrefs: android.content.SharedPreferences,
  prefs: android.content.SharedPreferences,
  onRequestConfirm: (String, () -> Unit) -> Unit,
  onAlert: (String) -> Unit,
  onAccountDeletedSuccess: () -> Unit,
) {
  if (userId.isBlank()) {
    onAlert(strings["account_delete_failed"])
    return
  }
  onRequestConfirm(strings["confirm_delete_account"]) {
    scope.launch(Dispatchers.IO) {
      try {
        SupabaseRepository.deleteUserAccount(userId)
        withContext(Dispatchers.Main) {
          securePrefs.edit {
            remove("loan_entries_$userId")
            remove("insights_cache_$userId")
          }
          fingerprintEnabledGlobal = false
          prefs.edit {
            putBoolean("fingerprint_enabled", false)
            putString("selected_theme", ThemeName.StandardLight.name)
          }
          runCatching { SupabaseClient.client.auth.signOut() }
          onAccountDeletedSuccess()
        }
      } catch (_: Exception) {
        withContext(Dispatchers.Main) {
          onAlert(strings["account_delete_failed"])
        }
      }
    }
  }
}

private var fingerprintEnabledGlobal = false
