package com.solvix.tabungan

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay

private const val PREF_SELECTED_THEME = "selected_theme"

class MainActivity : FragmentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      TabunganApp()
    }
  }
}

@Composable
fun TabunganApp() {
  val context = LocalContext.current
  val state = rememberTabunganAppState()
  var notificationPermissionRequested by rememberSaveable { mutableStateOf(false) }

  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { granted ->
    if (!granted) {
      state.alertMessage = "Notification permission is required to show reminders."
      state.showAlert = true
    }
  }

  LaunchedEffect(Unit) {
    state.prefs.edit {
      remove("face_unlock_enabled")
      remove("saved_username")
      remove("saved_password")
      remove("has_registered")
      remove("saved_auth_id")
    }
  }

  LaunchedEffect(Unit) {
    if (
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
      ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
      !notificationPermissionRequested
    ) {
      notificationPermissionRequested = true
      notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }
  }

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_START) {
        state.fadeSeed += 1
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  LaunchedEffect(state.currentTheme) {
    state.prefs.edit { putString(PREF_SELECTED_THEME, state.currentTheme.name) }
  }

  LaunchedEffect(state.currentPage) {
    state.pageFadeSeed += 1
  }

  LaunchedEffect(state.showAuth, state.showSplash, state.isLoggedIn) {
    if (!state.showAuth && !state.showSplash && state.isLoggedIn) {
      state.pageFadeSeed += 1
    }
  }

  TabunganTheme(theme = state.currentTheme) {
    CompositionLocalProvider(
      LocalStrings provides state.strings,
      LocalLanguage provides state.currentLang,
    ) {
      LaunchedEffect(state.toastVisible) {
        if (state.toastVisible) {
          delay(2000)
          state.toastVisible = false
        }
      }

      LaunchedEffect(state.showAuth, state.authTab) {
        if (state.showAuth && state.authTab == AuthTab.SignIn) {
          state.biometricPrompted = false
        }
      }

      LaunchedEffect(state.showAuth, state.authTab, state.fingerprintEnabled, state.hasRegistered, state.biometricAllowed) {
        if (!state.showAuth || state.authTab != AuthTab.SignIn) return@LaunchedEffect
        if (!state.hasRegistered || !state.biometricAllowed || state.biometricPrompted) return@LaunchedEffect
        if (!state.fingerprintEnabled || !state.canUseFingerprint()) return@LaunchedEffect
        state.biometricPrompted = true
        state.launchFingerprintAuth {
          state.signInWithSavedBiometricIdentity()
        }
      }

      LaunchedEffect(state.showLoading) {
        if (!state.showLoading) return@LaunchedEffect
        state.loadingFadeOut = false
        delay(5000)
        state.loadingFadeOut = true
        delay(700)
        state.showLoading = false
        when (state.loadingTarget) {
          LoadingTarget.Startup -> {
            if (!state.hasSeenWelcome) {
              state.showSplash = true
            } else {
              state.showAuth = true
              state.authTab = AuthTab.SignIn
            }
          }
          LoadingTarget.Logout -> {
            state.showAuth = true
            state.authTab = AuthTab.SignIn
          }
        }
      }

      TabunganAppShell(state = state)
    }
  }
}
