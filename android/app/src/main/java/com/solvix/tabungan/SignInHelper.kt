package com.solvix.tabungan

import android.util.Log
import androidx.core.content.edit
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Encapsulates the sign-in with credentials flow.
 *
 * All state mutations are delegated back to the caller via the provided lambdas so
 * this helper remains free of Compose state references.
 */
fun performSignInWithCredentials(
  username: String,
  password: String,
  scope: CoroutineScope,
  strings: AppStrings,
  signInLockedUntil: Long,
  failedSignInAttempts: Int,
  insightsTimeframe: InsightTimeframe,
  onAlert: (String) -> Unit,
  onLockUpdate: (attempts: Int, lockedUntil: Long) -> Unit,
  onLoginSuccess: (user: UserProfile) -> Unit,
  onAdminLogin: () -> Unit,
  onLoadData: suspend (userId: String) -> Unit,
) {
  val now = System.currentTimeMillis()
  if (signInLockedUntil > now) {
    val remaining = ((signInLockedUntil - now) / 1000L).coerceAtLeast(1L)
    onAlert(strings["signin_locked"].replace("{seconds}", remaining.toString()))
    return
  }
  if (username.isBlank() || password.isBlank()) {
    onAlert(strings["signin_missing"]); return
  }
  if (username.trim() == BuildConfig.ADMIN_USERNAME && password == BuildConfig.ADMIN_PASSWORD) {
    onAdminLogin(); return
  }
  scope.launch(Dispatchers.IO) {
    try {
      val matchedUser = SupabaseRepository.authenticateUser(username.trim(), password)
      withContext(Dispatchers.Main) {
        if (matchedUser == null) {
          val newAttempts = failedSignInAttempts + 1
          val newLockedUntil = if (newAttempts >= 5) System.currentTimeMillis() + 30_000L else signInLockedUntil
          onLockUpdate(if (newAttempts >= 5) 0 else newAttempts, newLockedUntil)
          onAlert(
            if (newAttempts >= 5) strings["signin_locked"].replace("{seconds}", "30")
            else strings["signin_failed"],
          )
        } else {
          onLockUpdate(0, 0L)
          onLoginSuccess(matchedUser)
          scope.launch(Dispatchers.IO) { onLoadData(matchedUser.id) }
        }
      }
    } catch (e: Exception) {
      Log.e("Auth", "signInWithCredentials failed", e)
      withContext(Dispatchers.Main) {
        val kind = if (e is SignInFlowException) e.kind else classifySignInFailure(e)
        onAlert(
          when (kind) {
            SignInFailureKind.Policy -> strings["signin_failed_policy"]
            SignInFailureKind.Network -> strings["signin_failed_network"]
            SignInFailureKind.Credentials -> strings["signin_failed"]
            SignInFailureKind.Unknown -> strings["signin_failed_network"]
          },
        )
      }
    }
  }
}

/**
 * Encapsulates the biometric / saved-session sign-in flow.
 */
fun performSignInWithSavedBiometricIdentity(
  scope: CoroutineScope,
  strings: AppStrings,
  savedUserId: String,
  savedUsername: String,
  savedAuthId: String,
  savedAccessToken: String,
  savedRefreshToken: String,
  insightsTimeframe: InsightTimeframe,
  onAlert: (String) -> Unit,
  onLoginSuccess: (user: UserProfile) -> Unit,
  onLoadData: suspend (userId: String) -> Unit,
) {
  if (savedUserId.isBlank() && savedUsername.isBlank() && savedAuthId.isBlank()) {
    onAlert(strings["signin_biometric_missing"]); return
  }
  scope.launch(Dispatchers.IO) {
    try {
      SupabaseClient.client.auth.awaitInitialization()
      if (SupabaseClient.client.auth.currentSessionOrNull() == null && savedAccessToken.isNotBlank()) {
        runCatching {
          SupabaseClient.client.auth.importAuthToken(
            accessToken = savedAccessToken,
            refreshToken = savedRefreshToken,
            retrieveUser = false,
          )
        }
      }
      val activeAuthId = SupabaseClient.client.auth.currentSessionOrNull()?.user?.id
        ?: runCatching { SupabaseClient.client.auth.retrieveUserForCurrentSession().id }.getOrNull()
        ?: ""
      val matchedUser = sequenceOf(
        runCatching { if (activeAuthId.isNotBlank()) SupabaseRepository.fetchUserByAuthId(activeAuthId) else null }.getOrNull(),
        runCatching { if (savedAuthId.isNotBlank()) SupabaseRepository.fetchUserByAuthId(savedAuthId) else null }.getOrNull(),
        runCatching { if (savedUserId.isNotBlank()) SupabaseRepository.fetchUserById(savedUserId) else null }.getOrNull(),
        runCatching { if (savedUsername.isNotBlank()) SupabaseRepository.fetchUserByUsername(savedUsername) else null }.getOrNull(),
      ).firstOrNull { it != null }?.toUserProfile()
      withContext(Dispatchers.Main) {
        if (matchedUser == null) {
          onAlert(strings["signin_biometric_expired"])
        } else {
          onLoginSuccess(matchedUser)
          scope.launch(Dispatchers.IO) { onLoadData(matchedUser.id) }
        }
      }
    } catch (_: Exception) {
      withContext(Dispatchers.Main) { onAlert(strings["signin_biometric_expired"]) }
    }
  }
}
