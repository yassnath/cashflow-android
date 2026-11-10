package com.solvix.tabungan

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.solvix.tabungan.components.AppShellModals

/**
 * Main application layout wrapper containing Scaffold, TopBar, BottomNav, Page Content, and Modals.
 */
@Composable
fun AppShellLayout(
  strings: AppStrings,
  currentTheme: ThemeName,
  currentLang: AppLanguage,
  currentPage: Page,
  summaryRange: SummaryRange,
  insightsTimeframe: InsightTimeframe,
  insightsRefreshing: Boolean,
  aiReplyLoading: Boolean,
  showSplash: Boolean,
  showAuth: Boolean,
  authTab: AuthTab,
  showLoading: Boolean,
  loadingFadeOut: Boolean,
  loadingTarget: LoadingTarget,
  showConfirm: Boolean,
  confirmMessage: String,
  showAlert: Boolean,
  alertMessage: String,
  toastVisible: Boolean,
  toastMessage: String,
  showProfileMenu: Boolean,
  adminLoggedIn: Boolean,
  adminUsers: List<SupabaseUser>,
  displayName: String,
  displayUsername: String,
  fingerprintEnabled: Boolean,
  hasRegistered: Boolean,
  biometricAllowed: Boolean,
  canUseFingerprint: Boolean,
  activeUserId: String,
  currentUser: UserProfile?,
  incomeEntries: SnapshotStateList<MoneyEntry>,
  expenseEntries: SnapshotStateList<MoneyEntry>,
  transferEntries: SnapshotStateList<MoneyEntry> = remember { mutableStateListOf() },
  dreamEntries: SnapshotStateList<DreamEntry>,
  loanEntries: SnapshotStateList<LoanEntry>,
  cachedInsights: SnapshotStateList<InsightItem>,
  chatMessages: SnapshotStateList<ChatMessage>,

  pendingEdit: MoneyEntry?,
  goalReachEvent: GoalReachEvent?,
  aiInsightsEnabled: Boolean,
  aiInsightsPrivateMode: Boolean,
  fadeSeed: Int,
  pageFadeSeed: Int,
  scope: kotlinx.coroutines.CoroutineScope,
  isAppLocked: Boolean = false,
  onUnlockRequest: () -> Unit = {},
  onNavigateTo: (Page) -> Unit,
  onShowProfileMenuChange: (Boolean) -> Unit,
  onLogoutClick: () -> Unit,
  onUpdateGoalMilestones: () -> Unit,
  onPersistLoanEntries: (String) -> Unit,
  onRefreshInsights: (String, InsightTimeframe, Boolean) -> Unit,
  onRequestConfirm: (String, () -> Unit) -> Unit,
  onAlert: (String) -> Unit,
  onPendingEditConsumed: () -> Unit,
  onGoalReachDismiss: () -> Unit,
  onAiReplyLoadingChange: (Boolean) -> Unit,
  onInsightsTimeframeChange: (InsightTimeframe) -> Unit,
  onFingerprintToggle: (Boolean) -> Unit,
  onAiInsightsToggle: (Boolean) -> Unit,
  onAiInsightsPrivateModeToggle: (Boolean) -> Unit,
  onLanguageChange: (AppLanguage) -> Unit,
  onChangePassword: (String, String, String) -> Unit,
  onDeleteAccount: () -> Unit,
  onThemeChange: (ThemeName) -> Unit,
  onProfileSave: (UserProfile) -> Unit,
  onProfileLogout: () -> Unit,
  onInsightFeedback: (InsightItem, Boolean) -> Unit,
  onRequestAiReply: suspend (List<ChatMessage>) -> String,
  resolveStartYear: () -> Int,
  onSplashDismiss: () -> Unit,
  onSplashPrimaryClick: () -> Unit,
  onAuthTabSelect: (AuthTab) -> Unit,
  onFingerprintLogin: () -> Unit,
  onSignInClick: () -> Unit,
  onSignUpClick: () -> Unit,
  onAuthCloseClick: () -> Unit,
  onConfirmCancel: () -> Unit,
  onConfirmExecute: () -> Unit,
  onAlertDismiss: () -> Unit,
  // Auth fields
  signInUsername: String, onSignInUsernameChange: (String) -> Unit,
  signInPassword: String, onSignInPasswordChange: (String) -> Unit,
  signUpName: String, onSignUpNameChange: (String) -> Unit,
  signUpEmail: String, onSignUpEmailChange: (String) -> Unit,
  signUpCountry: String, onSignUpCountryChange: (String) -> Unit,
  signUpBirthdate: String, onSignUpBirthdateChange: (String) -> Unit,
  signUpBio: String, onSignUpBioChange: (String) -> Unit,
  signUpUsername: String, onSignUpUsernameChange: (String) -> Unit,
  signUpPassword: String, onSignUpPasswordChange: (String) -> Unit,
) {
  val colors = LocalAppColors.current
  val configuration = LocalConfiguration.current
  val isSmallScreen = configuration.screenWidthDp < 360
  val horizontalPagePadding = if (isSmallScreen) 12.dp else AppDimens.pagePadding
  val modalOpen = showLoading || showSplash || showAuth || showConfirm || showAlert
  val loadingAlpha by animateFloatAsState(
    targetValue = if (showLoading && !loadingFadeOut) 1f else 0f,
    animationSpec = tween(700),
    label = "loading-alpha",
  )

  Box(modifier = Modifier.fillMaxSize().background(colors.bg)) {
    AmbientBackground()
    Scaffold(
      containerColor = Color.Transparent,
      bottomBar = {
        if (!adminLoggedIn && !showAuth && !showSplash) {
          val navBlur = showSplash || showAuth
          Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter,
          ) {
            Box(modifier = Modifier.widthIn(max = 680.dp)) {
              BottomNav(
                current = currentPage,
                onSelect = { page -> onNavigateTo(page) },
                strings = strings,
                theme = currentTheme,
                modifier = Modifier.then(if (navBlur) Modifier.blur(24.dp) else Modifier),
              )
              if (navBlur) {
                Box(
                  modifier = Modifier
                    .matchParentSize()
                    .background(Color(0xCCFFFFFF))
                    .clickable(enabled = true, onClick = {}),
                )
              }
            }
          }
        }
      },
    ) { padding ->
      val blurRadius = when {
        showAlert || showConfirm -> 0.dp
        showSplash || showAuth -> 72.dp
        modalOpen -> 20.dp
        else -> 0.dp
      }
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp)
            .padding(padding)
            .then(if (blurRadius.value > 0f) Modifier.blur(blurRadius) else Modifier)
            .verticalScroll(rememberScrollState())
            .padding(
              start = horizontalPagePadding,
              end = horizontalPagePadding,
              bottom = 24.dp,
              top = 0.dp,
            ),
          verticalArrangement = Arrangement.spacedBy(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          FadeInPage(key = "${currentPage}_${fadeSeed}_${pageFadeSeed}_page") {
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
              TopBar(
                onProfileClick = { onShowProfileMenuChange(true) },
                showMenu = showProfileMenu,
                onDismissMenu = { onShowProfileMenuChange(false) },
                onNavigate = { page ->
                  if (!adminLoggedIn) {
                    onNavigateTo(page)
                  }
                  onShowProfileMenuChange(false)
                },
                onLogout = onLogoutClick,
                displayName = displayName,
                displayUsername = displayUsername,
                strings = strings,
                theme = currentTheme,
                currentPage = currentPage,
                isAdmin = adminLoggedIn,
              )

              if (adminLoggedIn) {
                AdminDashboard(users = adminUsers, strings = strings)
              } else {
                AppPageContent(
                  currentPage = currentPage,
                  strings = strings,
                  currentLang = currentLang,
                  activeUserId = activeUserId,
                  scope = scope,
                  incomeEntries = incomeEntries,
                  expenseEntries = expenseEntries,
                  transferEntries = transferEntries,
                  dreamEntries = dreamEntries,
                  loanEntries = loanEntries,
                  cachedInsights = cachedInsights,
                  chatMessages = chatMessages,
                  summaryRange = summaryRange,
                  insightsTimeframe = insightsTimeframe,
                  insightsRefreshing = insightsRefreshing,
                  aiReplyLoading = aiReplyLoading,
                  pendingEdit = pendingEdit,
                  goalReachEvent = goalReachEvent,
                  currentUser = currentUser,
                  currentTheme = currentTheme,
                  fingerprintEnabled = fingerprintEnabled,
                  aiInsightsEnabled = aiInsightsEnabled,
                  aiInsightsPrivateMode = aiInsightsPrivateMode,
                  onUpdateGoalMilestones = onUpdateGoalMilestones,
                  onPersistLoanEntries = onPersistLoanEntries,
                  onRefreshInsights = onRefreshInsights,
                  onRequestConfirm = onRequestConfirm,
                  onAlert = onAlert,
                  onNavigateTo = onNavigateTo,
                  onPendingEditConsumed = onPendingEditConsumed,
                  onGoalReachDismiss = onGoalReachDismiss,
                  onAiReplyLoadingChange = onAiReplyLoadingChange,
                  onInsightsTimeframeChange = onInsightsTimeframeChange,
                  onFingerprintToggle = onFingerprintToggle,
                  onAiInsightsToggle = onAiInsightsToggle,
                  onAiInsightsPrivateModeToggle = onAiInsightsPrivateModeToggle,
                  onLanguageChange = onLanguageChange,
                  onChangePassword = onChangePassword,
                  onDeleteAccount = onDeleteAccount,
                  onThemeChange = onThemeChange,
                  onProfileSave = onProfileSave,
                  onProfileLogout = onProfileLogout,
                  onInsightFeedback = onInsightFeedback,
                  onRequestAiReply = onRequestAiReply,
                  resolveStartYear = resolveStartYear,
                )
              }
            }
          }
          Text(
            text = strings["footer"],
            color = colors.muted,
            fontSize = 11.sp,
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 0.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
          )
        }
        if (showSplash || showAuth) {
          Box(
            modifier = Modifier
              .matchParentSize()
              .background(Color.White),
          )
        }
      }
    }

    AppShellModals(
      strings = strings,
      currentLang = currentLang,
      toastVisible = toastVisible,
      toastMessage = toastMessage,
      showSplash = showSplash,
      fingerprintEnabled = fingerprintEnabled,
      biometricAllowed = biometricAllowed,
      canUseFingerprint = canUseFingerprint,
      onSplashDismiss = onSplashDismiss,
      onSplashPrimaryClick = onSplashPrimaryClick,
      showAuth = showAuth,
      authTab = authTab,
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
      hasRegistered = hasRegistered,
      onAuthTabSelect = onAuthTabSelect,
      onFingerprintLogin = onFingerprintLogin,
      onSignInClick = onSignInClick,
      onSignUpClick = onSignUpClick,
      onAuthCloseClick = onAuthCloseClick,
      showLoading = showLoading,
      loadingAlpha = loadingAlpha,
      showConfirm = showConfirm,
      confirmMessage = confirmMessage,
      onConfirmCancel = onConfirmCancel,
      onConfirmExecute = onConfirmExecute,
      showAlert = showAlert,
      alertMessage = alertMessage,
      onAlertDismiss = onAlertDismiss,
    )

    com.solvix.tabungan.components.AppLockScreen(
      isLocked = isAppLocked,
      onUnlockRequest = onUnlockRequest,
    )
  }
}
