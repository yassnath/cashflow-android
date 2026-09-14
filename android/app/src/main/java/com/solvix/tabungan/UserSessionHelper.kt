package com.solvix.tabungan

import android.content.SharedPreferences
import androidx.core.content.edit
import io.github.jan.supabase.gotrue.auth


/**
 * Persists the current user's biometric/session identity to secure storage.
 * Returns the updated token strings so callers can update their state.
 */
fun persistBiometricIdentityToPrefs(
  user: UserProfile,
  securePrefs: SharedPreferences,
  prefs: SharedPreferences,
): Triple<String, String, String> { // accessToken, refreshToken, authId
  val session = SupabaseClient.client.auth.currentSessionOrNull()
  val accessToken = session?.accessToken.orEmpty()
  val refreshToken = session?.refreshToken.orEmpty()
  securePrefs.edit {
    putBoolean("has_registered", true)
    putString("saved_username", user.username)
    putString("saved_user_id", user.id)
    putString("saved_auth_id", user.authId)
    putString("saved_access_token", accessToken)
    putString("saved_refresh_token", refreshToken)
  }
  return Triple(accessToken, refreshToken, user.authId)
}

/**
 * Clears all biometric/session identity from both prefs stores.
 */
fun clearBiometricIdentityFromPrefs(
  securePrefs: SharedPreferences,
  prefs: SharedPreferences,
) {
  prefs.edit { putBoolean("biometric_allowed", false) }
  securePrefs.edit {
    putBoolean("has_registered", false)
    putString("saved_username", "")
    putString("saved_user_id", "")
    putString("saved_auth_id", "")
    putString("saved_access_token", "")
    putString("saved_refresh_token", "")
  }
}

/**
 * Validates a password and returns a localized error string or null if valid.
 */
fun validatePasswordLocalized(password: String, strings: AppStrings): String? = when (
  PasswordSecurity.validatePassword(password)
) {
  PasswordSecurity.ValidationResult.Valid -> null
  PasswordSecurity.ValidationResult.TooShort -> strings["password_validation_length"]
  PasswordSecurity.ValidationResult.MissingUppercase -> strings["password_validation_uppercase"]
  PasswordSecurity.ValidationResult.MissingLowercase -> strings["password_validation_lowercase"]
  PasswordSecurity.ValidationResult.MissingDigit -> strings["password_validation_digit"]
}



