package com.solvix.tabungan

import android.app.KeyguardManager
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.content.edit
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.ZoneId

class TabunganAppState(
  val context: Context,
  val prefs: SharedPreferences,
  val securePrefs: SharedPreferences,
  val scope: CoroutineScope,
) {
  var currentTheme by mutableStateOf(
    runCatching {
      ThemeName.valueOf(prefs.getString("selected_theme", ThemeName.StandardLight.name) ?: ThemeName.StandardLight.name)
    }.getOrDefault(ThemeName.StandardLight)
  )
  var currentPage by mutableStateOf(Page.Dashboard)
  var summaryRange by mutableStateOf(SummaryRange.Month)
  var showProfileMenu by mutableStateOf(false)
  var showSplash by mutableStateOf(false)
  var showLoading by mutableStateOf(true)
  var loadingFadeOut by mutableStateOf(false)
  var loadingTarget by mutableStateOf(LoadingTarget.Startup)
  var showAuth by mutableStateOf(false)
  var authTab by mutableStateOf(AuthTab.SignIn)
  var adminLoggedIn by mutableStateOf(false)
  val adminUsers = mutableStateListOf<SupabaseUser>()
  var showConfirm by mutableStateOf(false)
  var confirmMessage by mutableStateOf("")
  var confirmAction by mutableStateOf<(() -> Unit)?>(null)
  var showAlert by mutableStateOf(false)
  var alertMessage by mutableStateOf("")
  var pendingEdit by mutableStateOf<MoneyEntry?>(null)
  var goalReachEvent by mutableStateOf<GoalReachEvent?>(null)
  var fadeSeed by mutableIntStateOf(0)
  var pageFadeSeed by mutableIntStateOf(0)
  var toastVisible by mutableStateOf(false)
  var toastMessage by mutableStateOf("")

  val defaultLang = prefs.getString("app_language", "EN") ?: "EN"
  var currentLang by mutableStateOf(if (defaultLang == "ID") AppLanguage.ID else AppLanguage.EN)
  var currentUser by mutableStateOf<UserProfile?>(null)
  var isLoggedIn by mutableStateOf(false)
  var hasSeenWelcome by mutableStateOf(prefs.getBoolean("has_seen_welcome", false))
  var fingerprintEnabled by mutableStateOf(prefs.getBoolean("fingerprint_enabled", false))
  var biometricAllowed by mutableStateOf(prefs.getBoolean("biometric_allowed", false))
  var hasRegistered by mutableStateOf(securePrefs.getBoolean("has_registered", false))
  var savedUsername by mutableStateOf(securePrefs.getString("saved_username", "") ?: "")
  var savedUserId by mutableStateOf(securePrefs.getString("saved_user_id", "") ?: "")
  var savedAuthId by mutableStateOf(securePrefs.getString("saved_auth_id", "") ?: "")
  var savedAccessToken by mutableStateOf(securePrefs.getString("saved_access_token", "") ?: "")
  var savedRefreshToken by mutableStateOf(securePrefs.getString("saved_refresh_token", "") ?: "")
  var failedSignInAttempts by mutableIntStateOf(prefs.getInt("signin_failed_attempts", 0))
  var signInLockedUntil by mutableLongStateOf(prefs.getLong("signin_locked_until", 0L))
  var biometricPrompted by mutableStateOf(false)

  val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

  val incomeEntries = mutableStateListOf<MoneyEntry>()
  val expenseEntries = mutableStateListOf<MoneyEntry>()
  val dreamEntries = mutableStateListOf<DreamEntry>()
  val loanEntries = mutableStateListOf<LoanEntry>()
  val cachedInsights = mutableStateListOf<InsightItem>()
  val chatMessages = mutableStateListOf<ChatMessage>()
  var aiReplyLoading by mutableStateOf(false)
  var insightsRefreshing by mutableStateOf(false)
  var insightsTimeframe by mutableStateOf(InsightTimeframe.D30)
  var aiInsightsEnabled by mutableStateOf(prefs.getBoolean("ai_insights_enabled", false))
  var aiInsightsPrivateMode by mutableStateOf(prefs.getBoolean("ai_insights_private_mode", true))
  val localJson = Json { ignoreUnknownKeys = true }

  var signInUsername by mutableStateOf("")
  var signInPassword by mutableStateOf("")
  var signUpName by mutableStateOf("")
  var signUpEmail by mutableStateOf("")
  var signUpCountry by mutableStateOf("")
  var signUpBirthdate by mutableStateOf("")
  var signUpBio by mutableStateOf("")
  var signUpUsername by mutableStateOf("")
  var signUpPassword by mutableStateOf("")
  val achievedGoalIds = mutableStateListOf<String>()
  var lastIncomeTotal by mutableIntStateOf(0)
  var lastExpenseTotal by mutableIntStateOf(0)
  var lastBalanceTotal by mutableIntStateOf(0)

  val strings get() = stringsFor(currentLang)

  fun navigateTo(page: Page) {
    if (currentPage == page) {
      pageFadeSeed += 1
      return
    }
    currentPage = page
  }

  fun resolveStartYear(): Int {
    val fallback = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val createdAt = currentUser?.createdAt.orEmpty()
    if (createdAt.isBlank()) return fallback
    val millis = parseCreatedAtMillis(createdAt) ?: return fallback
    return Instant.ofEpochMilli(millis).atZone(ZoneId.of("Asia/Jakarta")).year
  }

  fun passwordValidationMessage(password: String): String? =
    validatePasswordLocalized(password, strings)

  fun persistBiometricIdentity(user: UserProfile) {
    savedUsername = user.username; savedUserId = user.id; hasRegistered = true
    val (acc, ref, aid) = persistBiometricIdentityToPrefs(user, securePrefs, prefs)
    savedAccessToken = acc; savedRefreshToken = ref; savedAuthId = aid
  }

  fun clearBiometricIdentity() {
    hasRegistered = false; savedUsername = ""; savedUserId = ""
    savedAuthId = ""; savedAccessToken = ""; savedRefreshToken = ""; biometricAllowed = false
    clearBiometricIdentityFromPrefs(securePrefs, prefs)
  }

  fun updateGoalMilestones(notify: Boolean = true) {
    com.solvix.tabungan.updateGoalMilestones(
      incomeEntries = incomeEntries, expenseEntries = expenseEntries, dreamEntries = dreamEntries,
      achievedGoalIds = achievedGoalIds, lastIncomeTotal = lastIncomeTotal,
      lastExpenseTotal = lastExpenseTotal, lastBalanceTotal = lastBalanceTotal,
      strings = strings, context = context, notify = notify,
      onTotalsUpdate = { inc, exp, bal -> lastIncomeTotal = inc; lastExpenseTotal = exp; lastBalanceTotal = bal },
      onGoalReachEvent = { goalReachEvent = it }, onToast = { toastMessage = it; toastVisible = true },
    )
  }

  fun scheduleGoalDeadlineWorker() = com.solvix.tabungan.scheduleGoalDeadlineWorker(context)
  fun cancelGoalDeadlineWorker() = com.solvix.tabungan.cancelGoalDeadlineWorker(context)

  fun syncDebtFromExpense(entry: MoneyEntry, detectedType: String?, userId: String) =
    com.solvix.tabungan.syncDebtFromExpense(entry, detectedType, userId, loanEntries, securePrefs, localJson)

  fun syncAllDebtFromExpenses(userId: String) =
    com.solvix.tabungan.syncAllDebtFromExpenses(userId, expenseEntries, loanEntries, securePrefs, localJson)

  suspend fun loadUserData(userId: String) = com.solvix.tabungan.loadUserData(
    userId = userId, authId = currentUser?.authId.orEmpty(),
    incomeEntries = incomeEntries, expenseEntries = expenseEntries, dreamEntries = dreamEntries,
    achievedGoalIds = achievedGoalIds, strings = strings, context = context,
    onGoalReachEvent = { goalReachEvent = it }, onToast = { toastMessage = it; toastVisible = true },
  )

  fun loadLoanEntries(userId: String) =
    com.solvix.tabungan.loadLoanEntries(userId, loanEntries, securePrefs, localJson)

  fun persistLoanEntries(userId: String) =
    persistLoanEntriesToPrefs(userId, loanEntries.toList(), securePrefs, localJson)

  fun loadInsightsCache(userId: String) {
    val items = loadInsightsCacheFromPrefs(userId, securePrefs, localJson)
    cachedInsights.clear(); cachedInsights.addAll(items)
  }

  fun persistInsightsCache(userId: String) =
    persistInsightsCacheToPrefs(userId, cachedInsights.toList(), securePrefs, localJson)

  suspend fun submitInsightFeedback(userId: String, insight: InsightItem, isHelpful: Boolean) =
    com.solvix.tabungan.submitInsightFeedback(userId, insight, isHelpful, "")

  suspend fun refreshInsights(userId: String, timeframe: InsightTimeframe, forceAi: Boolean = false) {
    refreshInsightsData(
      userId = userId, timeframe = timeframe, forceAi = forceAi,
      aiInsightsEnabled = aiInsightsEnabled, aiInsightsPrivateMode = aiInsightsPrivateMode,
      incomeEntries = incomeEntries.toList(), expenseEntries = expenseEntries.toList(),
      dreamEntries = dreamEntries.toList(), loanEntries = loanEntries.toList(),
      strings = strings, localJson = localJson,
      onRefreshing = { insightsRefreshing = it },
      onResult = { items -> cachedInsights.clear(); cachedInsights.addAll(items); persistInsightsCache(userId) },
    )
  }

  suspend fun requestAiReply(history: List<ChatMessage>): String = com.solvix.tabungan.requestAiReply(
    history, currentLang, currentTheme, currentUser,
    incomeEntries.toList(), expenseEntries.toList(),
    dreamEntries.toList(), loanEntries.toList(), cachedInsights.toList(),
  )

  fun clearUserData() {
    currentUser = null
    isLoggedIn = false
    pendingEdit = null
    goalReachEvent = null
    achievedGoalIds.clear()
    lastIncomeTotal = 0
    lastExpenseTotal = 0
    lastBalanceTotal = 0
    cancelGoalDeadlineWorker()
    incomeEntries.clear()
    expenseEntries.clear()
    dreamEntries.clear()
    loanEntries.clear()
    cachedInsights.clear()
    chatMessages.clear()
    aiReplyLoading = false
    insightsRefreshing = false
  }

  fun enterAdminMode() {
    clearUserData()
    adminLoggedIn = true
    adminUsers.clear()
    showAuth = false
    showSplash = false
    showProfileMenu = false
  }

  fun exitAdminMode() {
    adminLoggedIn = false
    adminUsers.clear()
    showProfileMenu = false
    showAuth = true
    authTab = AuthTab.SignIn
  }

  fun signInWithCredentials(username: String, password: String) {
    performSignInWithCredentials(
      username = username, password = password,
      scope = scope, strings = strings,
      signInLockedUntil = signInLockedUntil,
      failedSignInAttempts = failedSignInAttempts,
      insightsTimeframe = insightsTimeframe,
      onAlert = { msg -> alertMessage = msg; showAlert = true },
      onLockUpdate = { attempts, lockedUntil ->
        failedSignInAttempts = attempts; signInLockedUntil = lockedUntil
        prefs.edit {
          putInt("signin_failed_attempts", attempts)
          putLong("signin_locked_until", lockedUntil)
        }
      },
      onLoginSuccess = { user ->
        currentUser = user; isLoggedIn = true; showAuth = false
        biometricAllowed = true
        prefs.edit { putBoolean("biometric_allowed", true) }
        persistBiometricIdentity(user)
        loadLoanEntries(user.id); loadInsightsCache(user.id)
        scheduleGoalDeadlineWorker()
        toastMessage = strings["login_success"]; toastVisible = true
      },
      onAdminLogin = {
        enterAdminMode()
        scope.launch(Dispatchers.IO) {
          try {
            val users = SupabaseRepository.fetchAllUsers()
            withContext(Dispatchers.Main) { adminUsers.clear(); adminUsers.addAll(users) }
          } catch (_: Exception) {
            withContext(Dispatchers.Main) { alertMessage = strings["admin_restricted"]; showAlert = true }
          }
        }
      },
      onLoadData = { userId -> scope.launch { loadUserData(userId); refreshInsights(userId, insightsTimeframe) } },
    )
  }

  fun signInWithSavedBiometricIdentity() {
    performSignInWithSavedBiometricIdentity(
      scope = scope, strings = strings,
      savedUserId = savedUserId, savedUsername = savedUsername,
      savedAuthId = savedAuthId, savedAccessToken = savedAccessToken,
      savedRefreshToken = savedRefreshToken, insightsTimeframe = insightsTimeframe,
      onAlert = { msg -> alertMessage = msg; showAlert = true },
      onLoginSuccess = { user ->
        currentUser = user; isLoggedIn = true; showAuth = false
        biometricAllowed = true
        prefs.edit { putBoolean("biometric_allowed", true) }
        persistBiometricIdentity(user)
        loadLoanEntries(user.id); loadInsightsCache(user.id)
        scheduleGoalDeadlineWorker()
        toastMessage = strings["login_success"]; toastVisible = true
      },
      onLoadData = { userId -> scope.launch { loadUserData(userId); refreshInsights(userId, insightsTimeframe) } },
    )
  }

  fun requestConfirm(message: String, onConfirm: () -> Unit) {
    confirmMessage = message; confirmAction = onConfirm; showConfirm = true
  }

  fun canUseFingerprint(): Boolean = com.solvix.tabungan.canUseFingerprint(context, keyguardManager)

  fun launchFingerprintAuth(onSuccess: () -> Unit) {
    com.solvix.tabungan.launchFingerprintAuth(
      context = context, keyguardManager = keyguardManager, strings = strings,
      onAlert = { msg -> alertMessage = msg; showAlert = true }, onSuccess = onSuccess,
    )
  }
}

@Composable
fun rememberTabunganAppState(
  context: Context = LocalContext.current,
  prefs: SharedPreferences = remember { context.getSharedPreferences("tabungan_prefs", Context.MODE_PRIVATE) },
  securePrefs: SharedPreferences = remember { SecurePrefs.open(context) },
  scope: CoroutineScope = rememberCoroutineScope(),
): TabunganAppState {
  return remember {
    TabunganAppState(context, prefs, securePrefs, scope)
  }
}
