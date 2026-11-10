package com.solvix.tabungan

import android.content.Intent
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val PREF_SELECTED_THEME = "selected_theme"

class MainActivity : FragmentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    CurrencyHelper.init(this)
    val prefs = getSharedPreferences("tabungan_prefs", MODE_PRIVATE)
    if (prefs.getBoolean("privacy_screen_enabled", false)) {
      window.setFlags(
        android.view.WindowManager.LayoutParams.FLAG_SECURE,
        android.view.WindowManager.LayoutParams.FLAG_SECURE,
      )
    }
    setContent {
      TabunganApp()
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    intent.getStringExtra("target_page")?.let { target ->
      targetPageCallback?.invoke(target)
    }
  }

  companion object {
    var targetPageCallback: ((String) -> Unit)? = null
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

  fun navigateToTarget(target: String) {
    when (target.lowercase()) {
      "expense" -> state.currentPage = Page.Expense
      "income" -> state.currentPage = Page.Income
      "transfer" -> state.currentPage = Page.Transfer
      "calculator" -> state.currentPage = Page.Calculator
      else -> Unit
    }
  }

  LaunchedEffect(Unit) {
    val initialTarget = (context as? MainActivity)?.intent?.getStringExtra("target_page")
    if (!initialTarget.isNullOrBlank()) {
      navigateToTarget(initialTarget)
    }
  }

  DisposableEffect(Unit) {
    MainActivity.targetPageCallback = { target ->
      navigateToTarget(target)
    }
    onDispose {
      MainActivity.targetPageCallback = null
    }
  }

  LaunchedEffect(Unit) {
    AppPinSecurityManager.init(context)
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

  var lastBackgroundTimestamp by rememberSaveable { mutableStateOf(0L) }
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_STOP -> {
          lastBackgroundTimestamp = System.currentTimeMillis()
        }
        Lifecycle.Event.ON_START -> {
          state.fadeSeed += 1
          CurrencyHelper.checkAndAutoRefresh(context)
          val hasPin = AppPinSecurityManager.isPinSet(context)
          val hasFingerprint = state.fingerprintEnabled && state.canUseFingerprint()
          if (lastBackgroundTimestamp > 0L && state.isLoggedIn && (hasFingerprint || hasPin)) {
            val timeoutSetting = state.prefs.getString("auto_lock_timeout", "off") ?: "off"
            val timeoutMs = when (timeoutSetting) {
              "instant", "Segera" -> 0L
              "1min", "1 Menit" -> 60_000L
              "5min", "5 Menit" -> 300_000L
              else -> -1L
            }
            if (timeoutMs >= 0L && System.currentTimeMillis() - lastBackgroundTimestamp >= timeoutMs) {
              state.isAppLocked = true
              if (hasFingerprint) {
                state.launchFingerprintAuth {
                  state.isAppLocked = false
                }
              }
            }
          }
          lastBackgroundTimestamp = 0L
        }
        else -> Unit
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  LaunchedEffect(Unit) {
    DailyReminderHelper.setDailyReminder(context, state.prefs.getBoolean("daily_reminder_enabled", true))
  }

  LaunchedEffect(state.isLoggedIn, state.currentUser?.id) {
    val uid = state.currentUser?.id
    if (state.isLoggedIn && uid?.isNotBlank() == true) {
      RecurringManager.checkAndGenerateDueTransactions(context) { entry ->
        if (entry.type == EntryType.Income) {
          state.incomeEntries.add(entry)
        } else {
          state.expenseEntries.add(entry)
        }
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
          SupabaseRepository.insertMoneyEntry(uid, entry)
        }
      }
    }
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
