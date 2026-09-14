package com.solvix.tabungan

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Checks supported biometric authenticators on the device.
 */
fun supportedBiometricAuthenticators(context: Context): Int {
  val manager = BiometricManager.from(context)
  val strong = manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
  if (strong == BiometricManager.BIOMETRIC_SUCCESS) {
    return BiometricManager.Authenticators.BIOMETRIC_STRONG
  }
  val weak = manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
  if (weak == BiometricManager.BIOMETRIC_SUCCESS) {
    return BiometricManager.Authenticators.BIOMETRIC_WEAK
  }
  return 0
}

/**
 * Returns true if fingerprint/biometric authentication is supported and device security is set up.
 */
fun canUseFingerprint(context: Context, keyguardManager: android.app.KeyguardManager): Boolean {
  return supportedBiometricAuthenticators(context) != 0 && keyguardManager.isDeviceSecure
}

/**
 * Prompts the native system biometric dialog.
 */
fun launchBiometricAuth(
  context: Context,
  keyguardManager: android.app.KeyguardManager,
  strings: AppStrings,
  title: String,
  subtitle: String,
  allowedAuthenticators: Int,
  onAlert: (String) -> Unit,
  onSuccess: () -> Unit,
) {
  val activity = context as? FragmentActivity
  if (activity == null) {
    onAlert(strings["fingerprint_activity_required"])
    return
  }
  if (!canUseFingerprint(context, keyguardManager)) {
    onAlert(strings["fingerprint_required"])
    return
  }
  val executor = ContextCompat.getMainExecutor(context)
  val prompt = BiometricPrompt(
    activity,
    executor,
    object : BiometricPrompt.AuthenticationCallback() {
      override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        onSuccess()
      }

      override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        onAlert(errString.toString())
      }
    },
  )
  val promptInfo = BiometricPrompt.PromptInfo.Builder()
    .setTitle(title)
    .setSubtitle(subtitle)
    .setNegativeButtonText(strings["confirm_cancel"])
    .setAllowedAuthenticators(allowedAuthenticators)
    .build()
  prompt.authenticate(promptInfo)
}

/**
 * Convenience prompt launcher specifically for fingerprint login.
 */
fun launchFingerprintAuth(
  context: Context,
  keyguardManager: android.app.KeyguardManager,
  strings: AppStrings,
  onAlert: (String) -> Unit,
  onSuccess: () -> Unit,
) {
  val authenticators = supportedBiometricAuthenticators(context)
  if (authenticators == 0) {
    onAlert(strings["fingerprint_required"])
    return
  }
  launchBiometricAuth(
    context = context,
    keyguardManager = keyguardManager,
    strings = strings,
    title = strings["fingerprint_prompt_title"],
    subtitle = strings["fingerprint_prompt_subtitle"],
    allowedAuthenticators = authenticators,
    onAlert = onAlert,
    onSuccess = onSuccess,
  )
}
