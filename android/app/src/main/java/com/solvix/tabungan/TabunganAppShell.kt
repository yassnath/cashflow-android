package com.solvix.tabungan

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TabunganAppShell(state: TabunganAppState) {
  val context = LocalContext.current
  val activeUserId = state.currentUser?.id.orEmpty()
  val displayName = if (state.adminLoggedIn) state.strings["menu_admin"] else (state.currentUser?.name ?: state.strings["guest"])
  val displayUsername = if (state.adminLoggedIn) "admin" else (state.currentUser?.username ?: state.strings["guest"])

  AppShellLayout(
    strings = state.strings,
    currentTheme = state.currentTheme,
    currentLang = state.currentLang,
    currentPage = state.currentPage,
    summaryRange = state.summaryRange,
    insightsTimeframe = state.insightsTimeframe,
    insightsRefreshing = state.insightsRefreshing,
    aiReplyLoading = state.aiReplyLoading,
    showSplash = state.showSplash,
    showAuth = state.showAuth,
    authTab = state.authTab,
    showLoading = state.showLoading,
    loadingFadeOut = state.loadingFadeOut,
    loadingTarget = state.loadingTarget,
    showConfirm = state.showConfirm,
    confirmMessage = state.confirmMessage,
    showAlert = state.showAlert,
    alertMessage = state.alertMessage,
    toastVisible = state.toastVisible,
    toastMessage = state.toastMessage,
    showProfileMenu = state.showProfileMenu,
    adminLoggedIn = state.adminLoggedIn,
    adminUsers = state.adminUsers,
    displayName = displayName,
    displayUsername = displayUsername,
    fingerprintEnabled = state.fingerprintEnabled,
    hasRegistered = state.hasRegistered,
    biometricAllowed = state.biometricAllowed,
    canUseFingerprint = state.canUseFingerprint(),
    activeUserId = activeUserId,
    currentUser = state.currentUser,
    incomeEntries = state.incomeEntries,
    expenseEntries = state.expenseEntries,
    dreamEntries = state.dreamEntries,
    loanEntries = state.loanEntries,
    cachedInsights = state.cachedInsights,
    chatMessages = state.chatMessages,
    pendingEdit = state.pendingEdit,
    goalReachEvent = state.goalReachEvent,
    aiInsightsEnabled = state.aiInsightsEnabled,
    aiInsightsPrivateMode = state.aiInsightsPrivateMode,
    fadeSeed = state.fadeSeed,
    pageFadeSeed = state.pageFadeSeed,
    scope = state.scope,
    onNavigateTo = { page ->
      state.pendingEdit = if (page == Page.Income || page == Page.Expense) state.pendingEdit else null
      state.navigateTo(page)
    },
    onShowProfileMenuChange = { state.showProfileMenu = it },
    onLogoutClick = {
      state.showProfileMenu = false
      state.requestConfirm(state.strings["logout_confirm"]) {
        if (state.adminLoggedIn) {
          state.exitAdminMode()
          clearAuthFields(
            onSignInUsername = { state.signInUsername = it }, onSignInPassword = { state.signInPassword = it },
            onSignUpName = { state.signUpName = it }, onSignUpEmail = { state.signUpEmail = it },
            onSignUpCountry = { state.signUpCountry = it }, onSignUpBirthdate = { state.signUpBirthdate = it },
            onSignUpBio = { state.signUpBio = it }, onSignUpUsername = { state.signUpUsername = it },
            onSignUpPassword = { state.signUpPassword = it },
          )
          state.toastMessage = state.strings["logout_success"]; state.toastVisible = true
        } else {
          state.clearUserData(); state.clearBiometricIdentity()
          state.scope.launch(Dispatchers.IO) { runCatching { SupabaseClient.client.auth.signOut() } }
          state.showSplash = false; state.showAuth = false
          state.loadingTarget = LoadingTarget.Logout; state.showLoading = true
          clearAuthFields(
            onSignInUsername = { state.signInUsername = it }, onSignInPassword = { state.signInPassword = it },
            onSignUpName = { state.signUpName = it }, onSignUpEmail = { state.signUpEmail = it },
            onSignUpCountry = { state.signUpCountry = it }, onSignUpBirthdate = { state.signUpBirthdate = it },
            onSignUpBio = { state.signUpBio = it }, onSignUpUsername = { state.signUpUsername = it },
            onSignUpPassword = { state.signUpPassword = it },
          )
          state.toastMessage = state.strings["logout_success"]; state.toastVisible = true
        }
      }
    },
    onUpdateGoalMilestones = { state.updateGoalMilestones() },
    onPersistLoanEntries = { uid -> state.persistLoanEntries(uid) },
    onRefreshInsights = { uid, tf, force -> state.scope.launch { state.refreshInsights(uid, tf, force) } },
    onRequestConfirm = { msg, action -> state.requestConfirm(msg, action) },
    onAlert = { msg -> state.alertMessage = msg; state.showAlert = true },
    onPendingEditConsumed = { state.pendingEdit = null },
    onGoalReachDismiss = { state.goalReachEvent = null },
    onAiReplyLoadingChange = { state.aiReplyLoading = it },
    onInsightsTimeframeChange = { state.insightsTimeframe = it },
    onFingerprintToggle = { enabled ->
      if (enabled) {
        if (state.canUseFingerprint()) {
          state.fingerprintEnabled = true
          state.prefs.edit { putBoolean("fingerprint_enabled", true) }
          state.alertMessage = state.strings["fingerprint_enabled"]; state.showAlert = true
        } else {
          state.fingerprintEnabled = false
          state.prefs.edit { putBoolean("fingerprint_enabled", false) }
          state.alertMessage = state.strings["fingerprint_required"]; state.showAlert = true
        }
      } else {
        state.fingerprintEnabled = false
        state.prefs.edit { putBoolean("fingerprint_enabled", false) }
      }
    },
    onAiInsightsToggle = { enabled ->
      state.aiInsightsEnabled = enabled
      state.prefs.edit { putBoolean("ai_insights_enabled", enabled) }
      if (enabled && activeUserId.isNotBlank()) {
        state.scope.launch(Dispatchers.IO) { state.refreshInsights(activeUserId, state.insightsTimeframe, forceAi = true) }
      }
    },
    onAiInsightsPrivateModeToggle = { enabled ->
      state.aiInsightsPrivateMode = enabled
      state.prefs.edit { putBoolean("ai_insights_private_mode", enabled) }
      if (activeUserId.isNotBlank()) {
        state.scope.launch(Dispatchers.IO) { state.refreshInsights(activeUserId, state.insightsTimeframe, false) }
      }
    },
    onLanguageChange = { lang ->
      state.currentLang = lang
      state.prefs.edit { putString("app_language", if (lang == AppLanguage.ID) "ID" else "EN") }
    },
    onChangePassword = { currentPassword, newPassword, confirmPassword ->
      performChangePassword(
        userId = state.currentUser?.id.orEmpty(), currentUser = state.currentUser,
        currentPassword = currentPassword, newPassword = newPassword, confirmPassword = confirmPassword,
        strings = state.strings, scope = state.scope, securePrefs = state.securePrefs, prefs = state.prefs,
        onAlert = { msg -> state.alertMessage = msg; state.showAlert = true },
        onPasswordChangeSuccess = {
          state.clearUserData(); state.clearBiometricIdentity()
          state.showSplash = false; state.showAuth = false
          state.loadingTarget = LoadingTarget.Logout; state.showLoading = true; state.authTab = AuthTab.SignIn
          clearAuthFields(onSignInUsername = { state.signInUsername = it }, onSignInPassword = { state.signInPassword = it }, onSignUpName = { state.signUpName = it }, onSignUpEmail = { state.signUpEmail = it }, onSignUpCountry = { state.signUpCountry = it }, onSignUpBirthdate = { state.signUpBirthdate = it }, onSignUpBio = { state.signUpBio = it }, onSignUpUsername = { state.signUpUsername = it }, onSignUpPassword = { state.signUpPassword = it })
          state.toastMessage = state.strings["password_change_relogin"]; state.toastVisible = true
        },
      )
    },
    onDeleteAccount = {
      performDeleteAccount(
        userId = state.currentUser?.id.orEmpty(), strings = state.strings, scope = state.scope, securePrefs = state.securePrefs, prefs = state.prefs,
        onRequestConfirm = { msg, act -> state.requestConfirm(msg, act) },
        onAlert = { msg -> state.alertMessage = msg; state.showAlert = true },
        onAccountDeletedSuccess = {
          state.securePrefs.edit { remove(loanStorageKey(state.currentUser?.id.orEmpty())); remove(insightsStorageKey(state.currentUser?.id.orEmpty())) }
          state.clearUserData(); state.clearBiometricIdentity()
          state.fingerprintEnabled = false
          state.prefs.edit { putBoolean("fingerprint_enabled", false); putString("selected_theme", ThemeName.StandardLight.name) }
          state.currentTheme = ThemeName.StandardLight
          state.showSplash = false; state.showAuth = false
          state.loadingTarget = LoadingTarget.Logout; state.showLoading = true; state.authTab = AuthTab.SignIn
          clearAuthFields(onSignInUsername = { state.signInUsername = it }, onSignInPassword = { state.signInPassword = it }, onSignUpName = { state.signUpName = it }, onSignUpEmail = { state.signUpEmail = it }, onSignUpCountry = { state.signUpCountry = it }, onSignUpBirthdate = { state.signUpBirthdate = it }, onSignUpBio = { state.signUpBio = it }, onSignUpUsername = { state.signUpUsername = it }, onSignUpPassword = { state.signUpPassword = it })
          state.toastMessage = state.strings["account_deleted"]; state.toastVisible = true
        },
      )
    },
    onThemeChange = { selected -> state.currentTheme = selected },
    onProfileSave = { updated ->
      state.currentUser = updated.copy(password = "")
      if (updated.id.isNotBlank()) {
        state.scope.launch {
          try {
            withContext(Dispatchers.IO) { SupabaseRepository.updateUserProfile(updated) }
            if (updated.id == state.savedUserId) {
              state.savedUsername = updated.username
              state.securePrefs.edit { putString("saved_username", updated.username) }
            }
            state.toastMessage = state.strings["save_profile"]; state.toastVisible = true
          } catch (_: Exception) { state.toastMessage = state.strings["save_profile_failed"]; state.toastVisible = true }
        }
      } else { state.toastMessage = state.strings["save_profile_failed"]; state.toastVisible = true }
    },
    onProfileLogout = {
      state.clearUserData(); state.clearBiometricIdentity()
      state.scope.launch(Dispatchers.IO) { runCatching { SupabaseClient.client.auth.signOut() } }
      state.showSplash = false; state.showAuth = false
      state.loadingTarget = LoadingTarget.Logout; state.showLoading = true
      clearAuthFields(onSignInUsername = { state.signInUsername = it }, onSignInPassword = { state.signInPassword = it }, onSignUpName = { state.signUpName = it }, onSignUpEmail = { state.signUpEmail = it }, onSignUpCountry = { state.signUpCountry = it }, onSignUpBirthdate = { state.signUpBirthdate = it }, onSignUpBio = { state.signUpBio = it }, onSignUpUsername = { state.signUpUsername = it }, onSignUpPassword = { state.signUpPassword = it })
      state.toastMessage = state.strings["logout_success"]; state.toastVisible = true
    },
    onInsightFeedback = { insight, helpful ->
      if (activeUserId.isNotBlank()) {
        state.scope.launch(Dispatchers.IO) { state.submitInsightFeedback(activeUserId, insight, helpful) }
      }
    },
    onRequestAiReply = { history -> state.requestAiReply(history) },
    resolveStartYear = { state.resolveStartYear() },
    onSplashDismiss = {
      state.hasSeenWelcome = true
      state.prefs.edit { putBoolean("has_seen_welcome", true) }
      state.showSplash = false; state.showAuth = true; state.authTab = AuthTab.SignIn
    },
    onSplashPrimaryClick = {
      state.hasSeenWelcome = true
      state.prefs.edit { putBoolean("has_seen_welcome", true) }
      if (state.fingerprintEnabled && state.biometricAllowed && state.canUseFingerprint()) {
        state.launchFingerprintAuth {
          state.showSplash = false; state.isLoggedIn = true
          state.toastMessage = state.strings["login_success"]; state.toastVisible = true
        }
      } else {
        state.showSplash = false; state.showAuth = true
      }
    },
    onAuthTabSelect = { state.authTab = it },
    onFingerprintLogin = { state.launchFingerprintAuth { state.signInWithSavedBiometricIdentity() } },
    onSignInClick = { state.signInWithCredentials(state.signInUsername, state.signInPassword) },
    onSignUpClick = {
      if (state.signUpName.isBlank() || state.signUpEmail.isBlank() || state.signUpUsername.isBlank() || state.signUpPassword.isBlank()) {
        state.alertMessage = state.strings["signup_missing"]; state.showAlert = true; return@AppShellLayout
      }
      if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.signUpEmail.trim()).matches()) {
        state.alertMessage = state.strings["signup_email_invalid"]; state.showAlert = true; return@AppShellLayout
      }
      val passwordError = state.passwordValidationMessage(state.signUpPassword)
      if (passwordError != null) { state.alertMessage = passwordError; state.showAlert = true; return@AppShellLayout }
      performSignUp(
        scope = state.scope, strings = state.strings,
        signUpName = state.signUpName, signUpEmail = state.signUpEmail, signUpCountry = state.signUpCountry,
        signUpBirthdate = state.signUpBirthdate, signUpBio = state.signUpBio,
        signUpUsername = state.signUpUsername, signUpPassword = state.signUpPassword,
        onAlert = { msg -> state.alertMessage = msg; state.showAlert = true },
        onSuccess = { normalizedUsername ->
          state.isLoggedIn = false; state.showAuth = true; state.authTab = AuthTab.SignIn
          state.signInUsername = normalizedUsername; state.signInPassword = ""
          state.clearBiometricIdentity()
          state.toastMessage = state.strings["signup_success"]; state.toastVisible = true
        },
      )
    },
    onAuthCloseClick = {
      state.requestConfirm(state.strings["exit_confirm"]) {
        (context as? Activity)?.finish()
      }
    },
    onConfirmCancel = { state.showConfirm = false },
    onConfirmExecute = { state.showConfirm = false; state.confirmAction?.invoke() },
    onAlertDismiss = { state.showAlert = false },
    signInUsername = state.signInUsername, onSignInUsernameChange = { state.signInUsername = it },
    signInPassword = state.signInPassword, onSignInPasswordChange = { state.signInPassword = it },
    signUpName = state.signUpName, onSignUpNameChange = { state.signUpName = it },
    signUpEmail = state.signUpEmail, onSignUpEmailChange = { state.signUpEmail = it },
    signUpCountry = state.signUpCountry, onSignUpCountryChange = { state.signUpCountry = it },
    signUpBirthdate = state.signUpBirthdate, onSignUpBirthdateChange = { state.signUpBirthdate = it },
    signUpBio = state.signUpBio, onSignUpBioChange = { state.signUpBio = it },
    signUpUsername = state.signUpUsername, onSignUpUsernameChange = { state.signUpUsername = it },
    signUpPassword = state.signUpPassword, onSignUpPasswordChange = { state.signUpPassword = it },
  )
}
