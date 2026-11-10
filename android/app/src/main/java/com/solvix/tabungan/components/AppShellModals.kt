package com.solvix.tabungan.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.solvix.tabungan.ActionSnackbar
import com.solvix.tabungan.AppLanguage
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.AuthTab
import com.solvix.tabungan.ToastMessage

@Composable
fun BoxScope.AppShellModals(
  strings: AppStrings,
  currentLang: AppLanguage,
  // Toast
  toastVisible: Boolean,
  toastMessage: String,
  // Splash
  showSplash: Boolean,
  fingerprintEnabled: Boolean,
  biometricAllowed: Boolean,
  canUseFingerprint: Boolean,
  onSplashDismiss: () -> Unit,
  onSplashPrimaryClick: () -> Unit,
  // Auth
  showAuth: Boolean,
  authTab: AuthTab,
  signInUsername: String,
  onSignInUsernameChange: (String) -> Unit,
  signInPassword: String,
  onSignInPasswordChange: (String) -> Unit,
  signUpName: String,
  onSignUpNameChange: (String) -> Unit,
  signUpEmail: String,
  onSignUpEmailChange: (String) -> Unit,
  signUpCountry: String,
  onSignUpCountryChange: (String) -> Unit,
  signUpBirthdate: String,
  onSignUpBirthdateChange: (String) -> Unit,
  signUpBio: String,
  onSignUpBioChange: (String) -> Unit,
  signUpUsername: String,
  onSignUpUsernameChange: (String) -> Unit,
  signUpPassword: String,
  onSignUpPasswordChange: (String) -> Unit,
  hasRegistered: Boolean,
  onAuthTabSelect: (AuthTab) -> Unit,
  onFingerprintLogin: () -> Unit,
  onSignInClick: () -> Unit,
  onSignUpClick: () -> Unit,
  onAuthCloseClick: () -> Unit,
  // Loading
  showLoading: Boolean,
  loadingAlpha: Float,
  // Confirm
  showConfirm: Boolean,
  confirmMessage: String,
  onConfirmCancel: () -> Unit,
  onConfirmExecute: () -> Unit,
  // Alert
  showAlert: Boolean,
  alertMessage: String,
  onAlertDismiss: () -> Unit,
  // Undo Action Snackbar
  undoMessage: String = "",
  undoVisible: Boolean = false,
  onUndoClick: () -> Unit = {},
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 76.dp)
      .align(Alignment.BottomCenter),
    contentAlignment = Alignment.Center,
  ) {
    ToastMessage(text = toastMessage, visible = toastVisible && !undoVisible)
    ActionSnackbar(
      text = undoMessage,
      actionLabel = "URUNGKAN",
      visible = undoVisible,
      onActionClick = onUndoClick,
    )
  }

  if (showSplash) {
    SplashModal(
      strings = strings,
      fingerprintEnabled = fingerprintEnabled,
      biometricAllowed = biometricAllowed,
      canUseFingerprint = canUseFingerprint,
      onDismiss = onSplashDismiss,
      onPrimaryClick = onSplashPrimaryClick,
    )
  }

  if (showAuth) {
    AuthModalSheet(
      strings = strings,
      authTab = authTab,
      currentLang = currentLang,
      signInUsername = signInUsername,
      onSignInUsernameChange = onSignInUsernameChange,
      signInPassword = signInPassword,
      onSignInPasswordChange = onSignInPasswordChange,
      signUpName = signUpName,
      onSignUpNameChange = onSignUpNameChange,
      signUpEmail = signUpEmail,
      onSignUpEmailChange = onSignUpEmailChange,
      signUpCountry = signUpCountry,
      onSignUpCountryChange = onSignUpCountryChange,
      signUpBirthdate = signUpBirthdate,
      onSignUpBirthdateChange = onSignUpBirthdateChange,
      signUpBio = signUpBio,
      onSignUpBioChange = onSignUpBioChange,
      signUpUsername = signUpUsername,
      onSignUpUsernameChange = onSignUpUsernameChange,
      signUpPassword = signUpPassword,
      onSignUpPasswordChange = onSignUpPasswordChange,
      fingerprintEnabled = fingerprintEnabled,
      hasRegistered = hasRegistered,
      biometricAllowed = biometricAllowed,
      onTabSelect = onAuthTabSelect,
      onFingerprintLogin = onFingerprintLogin,
      onSignInClick = onSignInClick,
      onSignUpClick = onSignUpClick,
      onCloseClick = onAuthCloseClick,
    )
  }

  if (showLoading) {
    LoadingOverlay(showLoading = showLoading, loadingAlpha = loadingAlpha)
  }

  if (showConfirm) {
    ConfirmModal(
      strings = strings,
      confirmMessage = confirmMessage,
      onCancel = onConfirmCancel,
      onConfirm = onConfirmExecute,
    )
  }

  if (showAlert) {
    AlertModal(
      strings = strings,
      alertMessage = alertMessage,
      onDismiss = onAlertDismiss,
    )
  }
}
