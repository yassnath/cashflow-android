package com.solvix.tabungan

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.solvix.tabungan.pages.settings.FullAppBackup
import com.solvix.tabungan.pages.settings.FullBackupHelper
import com.solvix.tabungan.pages.transfer.TransferPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Renders the content area for the currently active [Page].
 * Keeps all per-page wiring out of [MainActivity] / [TabunganApp].
 */
@Composable
fun AppPageContent(
  currentPage: Page,
  strings: AppStrings,
  currentLang: AppLanguage,
  activeUserId: String,
  scope: CoroutineScope,
  // â”€â”€ Entry data â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  incomeEntries: androidx.compose.runtime.snapshots.SnapshotStateList<MoneyEntry>,
  expenseEntries: androidx.compose.runtime.snapshots.SnapshotStateList<MoneyEntry>,
  transferEntries: androidx.compose.runtime.snapshots.SnapshotStateList<MoneyEntry> = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateListOf() },
  dreamEntries: androidx.compose.runtime.snapshots.SnapshotStateList<DreamEntry>,
  loanEntries: androidx.compose.runtime.snapshots.SnapshotStateList<LoanEntry>,
  cachedInsights: androidx.compose.runtime.snapshots.SnapshotStateList<InsightItem>,
  chatMessages: androidx.compose.runtime.snapshots.SnapshotStateList<ChatMessage>,
  // â”€â”€ UI State â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  summaryRange: SummaryRange,
  insightsTimeframe: InsightTimeframe,
  insightsRefreshing: Boolean,
  aiReplyLoading: Boolean,
  pendingEdit: MoneyEntry?,
  goalReachEvent: GoalReachEvent?,
  currentUser: UserProfile?,
  currentTheme: ThemeName,
  fingerprintEnabled: Boolean,
  aiInsightsEnabled: Boolean,
  aiInsightsPrivateMode: Boolean,
  // â”€â”€ Callbacks â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  onUpdateGoalMilestones: () -> Unit,
  onPersistLoanEntries: (userId: String) -> Unit,
  onRefreshInsights: suspend (userId: String, timeframe: InsightTimeframe, forceAi: Boolean) -> Unit,
  onRequestConfirm: (message: String, action: () -> Unit) -> Unit,
  onAlert: (String) -> Unit,
  onToast: (String) -> Unit = onAlert,
  onNavigateTo: (Page) -> Unit,
  onPendingEditConsumed: () -> Unit,
  onGoalReachDismiss: () -> Unit,
  onAiReplyLoadingChange: (Boolean) -> Unit,
  onInsightsTimeframeChange: (InsightTimeframe) -> Unit,
  // Settings-specific
  onFingerprintToggle: (Boolean) -> Unit,
  onAiInsightsToggle: (Boolean) -> Unit,
  onAiInsightsPrivateModeToggle: (Boolean) -> Unit,
  onLanguageChange: (AppLanguage) -> Unit,
  onChangePassword: (current: String, new: String, confirm: String) -> Unit,
  onDeleteAccount: () -> Unit,
  onThemeChange: (ThemeName) -> Unit,
  // Profile
  onProfileSave: (UserProfile) -> Unit,
  onProfileLogout: () -> Unit,
  // Insights
  onInsightFeedback: (InsightItem, Boolean) -> Unit,
  // AI Chat
  onRequestAiReply: suspend (List<ChatMessage>) -> String,
  // Report
  resolveStartYear: () -> Int,
  onShowUndo: (message: String, onUndo: () -> Unit) -> Unit = { _, _ -> },
) {
  // Hero summary (shown on pages that declare showHero)
  val context = LocalContext.current
  if (currentPage.showHero()) {
    val filtered = filterByRange(incomeEntries + expenseEntries, summaryRange)
    val incomeTotal = filtered.filter { it.type == EntryType.Income }.sumOf { it.amount }
    val expenseTotal = filtered.filter { it.type == EntryType.Expense }.sumOf { it.amount }
    HeroSummary(
      range = summaryRange,
      onRangeChange = { /* parent controls summaryRange */ },
      incomeTotal = incomeTotal,
      expenseTotal = expenseTotal,
      strings = strings,
    )
  }

  // â”€â”€ Per-page routing â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  fun persistLocalCache() {
    if (activeUserId.isNotBlank()) {
      LocalDataCache.save(context, activeUserId, incomeEntries, expenseEntries, dreamEntries, transferEntries)
    }
  }

  when (currentPage) {
    Page.Dashboard -> DashboardPage(
      incomeEntries = incomeEntries,
      expenseEntries = expenseEntries,
      dreamEntries = dreamEntries,
      loanEntries = loanEntries,
      currentUser = currentUser,
      currentLang = currentLang,
      strings = strings,
      onNavigateTo = onNavigateTo,
      transferEntries = transferEntries,
    )

    Page.Income -> IncomePage(
      onSave = { entry ->
        incomeEntries.add(entry)
        onUpdateGoalMilestones()
        persistLocalCache()
        HapticHelper.vibrateSuccess(context)
        onAlert(strings["income_save_success"].ifBlank { "Pemasukan berhasil disimpan! ✅" })
        if (activeUserId.isNotBlank()) {
          scope.launch(Dispatchers.IO) {
            var toSync = entry
            if (entry.receiptUri.isNotBlank() && !entry.receiptUri.startsWith("http")) {
              val uploaded = ReceiptStorageHelper.uploadReceiptToCloud(context, entry.receiptUri, activeUserId)
              if (!uploaded.isNullOrBlank()) {
                toSync = entry.copy(receiptUri = uploaded)
                val idx = incomeEntries.indexOfFirst { it.id == entry.id }
                if (idx >= 0) {
                  incomeEntries[idx] = toSync
                }
                persistLocalCache()
              }
            }
            SyncQueueManager.enqueueInsertMoney(context, activeUserId, toSync)
            SyncQueueManager.processQueue(context)
            onRefreshInsights(activeUserId, insightsTimeframe, false)
          }
        }
      },
      onUpdate = { entry ->
        val i = incomeEntries.indexOfFirst { it.id == entry.id }
        if (i >= 0) incomeEntries[i] = entry
        onUpdateGoalMilestones()
        persistLocalCache()
        HapticHelper.vibrateSuccess(context)
        onAlert(strings["income_update_success"].ifBlank { "Pemasukan berhasil diperbarui! ✅" })
        if (activeUserId.isNotBlank()) {
          scope.launch(Dispatchers.IO) {
            var toSync = entry
            if (entry.receiptUri.isNotBlank() && !entry.receiptUri.startsWith("http")) {
              val uploaded = ReceiptStorageHelper.uploadReceiptToCloud(context, entry.receiptUri, activeUserId)
              if (!uploaded.isNullOrBlank()) {
                toSync = entry.copy(receiptUri = uploaded)
                val idx = incomeEntries.indexOfFirst { it.id == entry.id }
                if (idx >= 0) {
                  incomeEntries[idx] = toSync
                }
                persistLocalCache()
              }
            }
            SyncQueueManager.enqueueUpdateMoney(context, activeUserId, toSync)
            SyncQueueManager.processQueue(context)
            onRefreshInsights(activeUserId, insightsTimeframe, false)
          }
        }
      },
      editEntry = pendingEdit,
      onEditConsumed = onPendingEditConsumed,
      strings = strings,
    )

    Page.Expense -> ExpensePage(
      onSave = { entry ->
        expenseEntries.add(entry)
        onUpdateGoalMilestones()
        persistLocalCache()
        HapticHelper.vibrateSuccess(context)
        onAlert(strings["expense_save_success"].ifBlank { "Pengeluaran berhasil disimpan! ✅" })
        if (activeUserId.isNotBlank()) {
          scope.launch(Dispatchers.IO) {
            var toSync = entry
            if (entry.receiptUri.isNotBlank() && !entry.receiptUri.startsWith("http")) {
              val uploaded = ReceiptStorageHelper.uploadReceiptToCloud(context, entry.receiptUri, activeUserId)
              if (!uploaded.isNullOrBlank()) {
                toSync = entry.copy(receiptUri = uploaded)
                val idx = expenseEntries.indexOfFirst { it.id == entry.id }
                if (idx >= 0) {
                  expenseEntries[idx] = toSync
                }
                persistLocalCache()
              }
            }
            SyncQueueManager.enqueueInsertMoney(context, activeUserId, toSync)
            SyncQueueManager.processQueue(context)
            onRefreshInsights(activeUserId, insightsTimeframe, false)
          }
        }
      },
      onUpdate = { entry ->
        val i = expenseEntries.indexOfFirst { it.id == entry.id }
        if (i >= 0) expenseEntries[i] = entry
        onUpdateGoalMilestones()
        persistLocalCache()
        HapticHelper.vibrateSuccess(context)
        onAlert(strings["expense_update_success"].ifBlank { "Pengeluaran berhasil diperbarui! ✅" })
        if (activeUserId.isNotBlank()) {
          scope.launch(Dispatchers.IO) {
            var toSync = entry
            if (entry.receiptUri.isNotBlank() && !entry.receiptUri.startsWith("http")) {
              val uploaded = ReceiptStorageHelper.uploadReceiptToCloud(context, entry.receiptUri, activeUserId)
              if (!uploaded.isNullOrBlank()) {
                toSync = entry.copy(receiptUri = uploaded)
                val idx = expenseEntries.indexOfFirst { it.id == entry.id }
                if (idx >= 0) {
                  expenseEntries[idx] = toSync
                }
                persistLocalCache()
              }
            }
            SyncQueueManager.enqueueUpdateMoney(context, activeUserId, toSync)
            SyncQueueManager.processQueue(context)
            onRefreshInsights(activeUserId, insightsTimeframe, false)
          }
        }
      },
      editEntry = pendingEdit,
      onEditConsumed = onPendingEditConsumed,
      strings = strings,
      existingExpenses = expenseEntries,
      onBudgetWarning = { warning -> onToast(warning) },
      onEntriesGenerated = { generatedList ->
        generatedList.forEach { entry ->
          if (entry.type == EntryType.Income) {
            incomeEntries.add(entry)
          } else {
            expenseEntries.add(entry)
          }
          if (activeUserId.isNotBlank()) {
            scope.launch(Dispatchers.IO) {
              SyncQueueManager.enqueueInsertMoney(context, activeUserId, entry)
              SyncQueueManager.processQueue(context)
            }
          }
        }
        onUpdateGoalMilestones()
      },
    )

    Page.Transfer -> TransferPage(
      onSave = { entry ->
        transferEntries.add(entry)
        persistLocalCache()
        HapticHelper.vibrateSuccess(context)
        onAlert(strings["transfer_save_success"].ifBlank { "Transfer dana berhasil disimpan! ✅" })
        if (activeUserId.isNotBlank()) {
          scope.launch(Dispatchers.IO) {
            SyncQueueManager.enqueueInsertMoney(context, activeUserId, entry)
            SyncQueueManager.processQueue(context)
          }
        }
      },
      onUpdate = { entry ->
        val i = transferEntries.indexOfFirst { it.id == entry.id }
        if (i >= 0) transferEntries[i] = entry
        persistLocalCache()
        HapticHelper.vibrateSuccess(context)
        onAlert(strings["transfer_update_success"].ifBlank { "Transfer dana berhasil diperbarui! ✅" })
        if (activeUserId.isNotBlank()) {
          scope.launch(Dispatchers.IO) {
            SyncQueueManager.enqueueUpdateMoney(context, activeUserId, entry)
            SyncQueueManager.processQueue(context)
          }
        }
      },
      editEntry = pendingEdit,
      onEditConsumed = onPendingEditConsumed,
      strings = strings,
    )

    Page.Dreams -> DreamsPage(
      entries = dreamEntries,
      incomeTotal = incomeEntries.sumOf { it.amount },
      expenseTotal = expenseEntries.sumOf { it.amount },
      balanceTotal = incomeEntries.sumOf { it.amount } - expenseEntries.sumOf { it.amount },
      onInvalid = { onAlert(strings["goal_missing"]) },
      onSave = { entry ->
        dreamEntries.add(entry)
        onUpdateGoalMilestones()
        persistLocalCache()
        HapticHelper.vibrateSuccess(context)
        onAlert(strings["dream_save_success"].ifBlank { "Target impian berhasil disimpan! ✅" })
        if (activeUserId.isNotBlank()) {
          scope.launch(Dispatchers.IO) {
            SupabaseRepository.insertDreamEntry(activeUserId, entry)
            onRefreshInsights(activeUserId, insightsTimeframe, false)
          }
        }
      },
      onUpdate = { entry ->
        val i = dreamEntries.indexOfFirst { it.id == entry.id }
        if (i >= 0) dreamEntries[i] = entry
        onUpdateGoalMilestones()
        persistLocalCache()
        HapticHelper.vibrateSuccess(context)
        onAlert(strings["dream_update_success"].ifBlank { "Target impian berhasil diperbarui! ✅" })
        if (activeUserId.isNotBlank()) {
          scope.launch(Dispatchers.IO) {
            SupabaseRepository.updateDreamEntry(entry)
            onRefreshInsights(activeUserId, insightsTimeframe, false)
          }
        }
      },
      onDelete = { entry ->
        onRequestConfirm(strings["confirm_delete_dream"]) {
          dreamEntries.removeAll { it.id == entry.id }
          onUpdateGoalMilestones()
          persistLocalCache()
          if (activeUserId.isNotBlank()) {
            scope.launch(Dispatchers.IO) {
              SupabaseRepository.deleteDreamEntry(entry.id)
              onRefreshInsights(activeUserId, insightsTimeframe, false)
            }
          }
        }
      },
      goalReachSourceType = if (currentPage == Page.Dreams) goalReachEvent?.sourceType else null,
      onGoalReachDismiss = onGoalReachDismiss,
      strings = strings,
    )

    Page.History -> HistoryPage(
      entries = incomeEntries + expenseEntries + transferEntries,
      strings = strings,
      onEdit = { entry ->
        onNavigateTo(
          when (entry.type) {
            EntryType.Income -> Page.Income
            EntryType.Expense -> Page.Expense
            EntryType.Transfer -> Page.Transfer
          }
        )
      },
      onDelete = { entry ->
        when (entry.type) {
          EntryType.Income -> incomeEntries.removeAll { it.id == entry.id }
          EntryType.Expense -> expenseEntries.removeAll { it.id == entry.id }
          EntryType.Transfer -> transferEntries.removeAll { it.id == entry.id }
        }
        onUpdateGoalMilestones()
        persistLocalCache()
        HapticHelper.vibrateClick(context)

        onShowUndo("Transaksi berhasil dihapus") {
          when (entry.type) {
            EntryType.Income -> incomeEntries.add(entry)
            EntryType.Expense -> expenseEntries.add(entry)
            EntryType.Transfer -> transferEntries.add(entry)
          }
          onUpdateGoalMilestones()
          persistLocalCache()
          HapticHelper.vibrateSuccess(context)
          onToast("Transaksi berhasil dipulihkan! ↩️")
        }

        if (activeUserId.isNotBlank()) {
          scope.launch(Dispatchers.IO) {
            kotlinx.coroutines.delay(5200)
            val stillDeleted = incomeEntries.none { it.id == entry.id } &&
              expenseEntries.none { it.id == entry.id } &&
              transferEntries.none { it.id == entry.id }
            if (stillDeleted) {
              SyncQueueManager.enqueueDeleteMoney(context, activeUserId, entry.id)
              SyncQueueManager.processQueue(context)
              onRefreshInsights(activeUserId, insightsTimeframe, false)
            }
          }
        }
      },
      onDeleteBatch = { batch ->
        if (batch.isEmpty()) return@HistoryPage
        val ids = batch.map { it.id }.toSet()
        incomeEntries.removeAll { ids.contains(it.id) }
        expenseEntries.removeAll { ids.contains(it.id) }
        transferEntries.removeAll { ids.contains(it.id) }
        onUpdateGoalMilestones()
        persistLocalCache()
        HapticHelper.vibrateClick(context)

        onShowUndo("${batch.size} transaksi berhasil dihapus") {
          batch.forEach { entry ->
            when (entry.type) {
              EntryType.Income -> incomeEntries.add(entry)
              EntryType.Expense -> expenseEntries.add(entry)
              EntryType.Transfer -> transferEntries.add(entry)
            }
          }
          onUpdateGoalMilestones()
          persistLocalCache()
          HapticHelper.vibrateSuccess(context)
          onToast("${batch.size} transaksi berhasil dipulihkan! ↩️")
        }

        if (activeUserId.isNotBlank()) {
          scope.launch(Dispatchers.IO) {
            kotlinx.coroutines.delay(5200)
            val stillDeleted = batch.filter { b ->
              incomeEntries.none { it.id == b.id } &&
                expenseEntries.none { it.id == b.id } &&
                transferEntries.none { it.id == b.id }
            }
            if (stillDeleted.isNotEmpty()) {
              stillDeleted.forEach { entry ->
                SyncQueueManager.enqueueDeleteMoney(context, activeUserId, entry.id)
              }
              SyncQueueManager.processQueue(context)
              onRefreshInsights(activeUserId, insightsTimeframe, false)
            }
          }
        }
      },
      onImportEntries = { importedEntries ->
        importedEntries.forEach { entry ->
          when (entry.type) {
            EntryType.Income -> incomeEntries.add(entry)
            EntryType.Expense -> expenseEntries.add(entry)
            EntryType.Transfer -> transferEntries.add(entry)
          }
          if (activeUserId.isNotBlank()) {
            scope.launch(Dispatchers.IO) {
              SupabaseRepository.insertMoneyEntry(activeUserId, entry)
            }
          }
        }
        onUpdateGoalMilestones()
        persistLocalCache()
        if (activeUserId.isNotBlank()) {
          scope.launch(Dispatchers.IO) {
            onRefreshInsights(activeUserId, insightsTimeframe, false)
          }
        }
        onToast("Berhasil mengimpor ${importedEntries.size} transaksi!")
      },
    )

    Page.Insights -> InsightsPage(
      insights = cachedInsights.toList(),
      defaultTimeframe = insightsTimeframe,
      onRefresh = { timeframe ->
        onInsightsTimeframeChange(timeframe)
        if (activeUserId.isNotBlank()) {
          scope.launch(Dispatchers.IO) { onRefreshInsights(activeUserId, timeframe, false) }
        }
      },
      onAction = { insight ->
        val target = runCatching { Page.valueOf(insight.actionTargetPage) }.getOrNull()
        if (target != null) onNavigateTo(target)
      },
      onFeedback = { insight, helpful -> onInsightFeedback(insight, helpful) },
      isRefreshing = insightsRefreshing,
      strings = strings,
    )

    Page.Loans -> LoanTrackingPage(
      entries = loanEntries,
      onInvalid = { onAlert(strings["loan_missing"]) },
      onSave = { entry ->
        loanEntries.add(entry)
        onPersistLoanEntries(activeUserId)
        HapticHelper.vibrateSuccess(context)
        onAlert(strings["loan_save_success"].ifBlank { "Catatan hutang/piutang berhasil disimpan! ✅" })
        if (activeUserId.isNotBlank()) {
          scope.launch(Dispatchers.IO) {
            SupabaseRepository.insertLoanEntry(activeUserId, entry)
            onRefreshInsights(activeUserId, insightsTimeframe, false)
          }
        }
      },
      onUpdate = { entry ->
        val i = loanEntries.indexOfFirst { it.id == entry.id }
        if (i >= 0) {
          loanEntries[i] = entry
          onPersistLoanEntries(activeUserId)
          HapticHelper.vibrateSuccess(context)
          onAlert(strings["loan_update_success"].ifBlank { "Catatan hutang/piutang berhasil diperbarui! ✅" })
          if (activeUserId.isNotBlank()) {
            scope.launch(Dispatchers.IO) {
              SupabaseRepository.updateLoanEntry(entry)
              onRefreshInsights(activeUserId, insightsTimeframe, false)
            }
          }
        }
      },
      onDelete = { entry ->
        onRequestConfirm(strings["confirm_delete_loan"]) {
          loanEntries.removeAll { it.id == entry.id }
          onPersistLoanEntries(activeUserId)
          if (activeUserId.isNotBlank()) {
            scope.launch(Dispatchers.IO) {
              SupabaseRepository.deleteLoanEntry(entry.id)
              onRefreshInsights(activeUserId, insightsTimeframe, false)
            }
          }
        }
      },
      strings = strings,
    )

    Page.AIChat -> AiChatPage(
      messages = chatMessages,
      isLoading = aiReplyLoading,
      onClear = { chatMessages.clear() },
      onSend = { prompt ->
        val trimmed = prompt.trim()
        if (trimmed.isBlank() || aiReplyLoading) return@AiChatPage
        chatMessages.add(ChatMessage(role = "user", content = trimmed))
        onAiReplyLoadingChange(true)
        scope.launch(Dispatchers.IO) {
          try {
            val reply = onRequestAiReply(chatMessages.toList())
            withContext(Dispatchers.Main) {
              chatMessages.add(ChatMessage(role = "assistant", content = reply))
              onAiReplyLoadingChange(false)
            }
          } catch (e: Exception) {
            Log.e("AIChat", "Request failed", e)
            withContext(Dispatchers.Main) {
              onAiReplyLoadingChange(false)
              onAlert(
                if (BuildConfig.CEREBRAS_API_KEY.isBlank()) strings["ai_not_configured"]
                else strings["ai_request_failed"],
              )
            }
          }
        }
      },
      strings = strings,
    )

    Page.Calculator -> CalculatorPage(
      onRecordToExpense = { shareAmount ->
        val nowFmt = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val entry = MoneyEntry(
          type = EntryType.Expense,
          amount = shareAmount.toInt(),
          date = nowFmt,
          category = "Makanan & Minuman",
          sourceOrMethod = "Transfer",
          channelOrBank = "SeaBank",
          note = "[Split Bill] Patungan",
        )
        expenseEntries.add(entry)
        persistLocalCache()
        if (activeUserId.isNotBlank()) {
          scope.launch(Dispatchers.IO) {
            SupabaseRepository.insertMoneyEntry(activeUserId, entry)
          }
        }
        onToast("Berhasil mencatat bagian patungan Rp ${formatRupiah(shareAmount)} ke Pengeluaran!")
        onNavigateTo(Page.Expense)
      },
      onToast = onToast,
    )

    Page.Report -> ReportPage(
      incomeEntries,
      expenseEntries,
      strings = strings,
      language = currentLang,
      startYear = resolveStartYear(),
    ) { msg -> /* toast handled by parent */ }

    Page.Profile -> ProfilePage(
      user = currentUser,
      strings = strings,
      onSave = onProfileSave,
      onLogout = onProfileLogout,
    )

    Page.Settings -> SettingsPage(
      fingerprintEnabled = fingerprintEnabled,
      onFingerprintToggle = onFingerprintToggle,
      aiInsightsEnabled = aiInsightsEnabled,
      onAiInsightsToggle = onAiInsightsToggle,
      aiInsightsPrivateMode = aiInsightsPrivateMode,
      onAiInsightsPrivateModeToggle = onAiInsightsPrivateModeToggle,
      language = currentLang,
      onLanguageChange = onLanguageChange,
      strings = strings,
      onChangePassword = onChangePassword,
      canDeleteAccount = currentUser?.id?.isNotBlank() == true,
      onDeleteAccount = onDeleteAccount,
      onExportBackup = {
        val backup = FullAppBackup(
          userId = activeUserId,
          income = incomeEntries.toList(),
          expense = expenseEntries.toList(),
          transfers = transferEntries.toList(),
          dreams = dreamEntries.toList(),
          loans = loanEntries.toList(),
          categoryBudgets = CategoryBudgetManager.loadBudgets(context),
          initialBalances = InitialBalanceManager.getInitialBalances(context),
        )
        FullBackupHelper.shareBackup(context, backup)
      },
      onRestoreBackup = { backup ->
        incomeEntries.clear()
        incomeEntries.addAll(backup.income)
        expenseEntries.clear()
        expenseEntries.addAll(backup.expense)
        transferEntries.clear()
        transferEntries.addAll(backup.transfers)
        dreamEntries.clear()
        dreamEntries.addAll(backup.dreams)
        loanEntries.clear()
        loanEntries.addAll(backup.loans)
        backup.categoryBudgets.forEach { (cat, limit) ->
          CategoryBudgetManager.saveBudget(context, cat, limit)
        }
        backup.initialBalances.forEach { (acc, bal) ->
          InitialBalanceManager.setInitialBalance(context, acc, bal, userId = activeUserId, scope = scope)
        }
        onUpdateGoalMilestones()
        onPersistLoanEntries(activeUserId)
        persistLocalCache()
        HapticHelper.vibrateSuccess(context)
        if (activeUserId.isNotBlank()) {
          scope.launch(Dispatchers.IO) {
            backup.income.forEach { SupabaseRepository.insertMoneyEntry(activeUserId, it) }
            backup.expense.forEach { SupabaseRepository.insertMoneyEntry(activeUserId, it) }
            backup.transfers.forEach { SupabaseRepository.insertMoneyEntry(activeUserId, it) }
            backup.dreams.forEach { SupabaseRepository.insertDreamEntry(activeUserId, it) }
            onRefreshInsights(activeUserId, insightsTimeframe, false)
          }
        }
      },
      onResetAllData = {
        val oldEntries = (incomeEntries + expenseEntries + transferEntries).toList()
        incomeEntries.clear()
        expenseEntries.clear()
        transferEntries.clear()
        val initialBalances = InitialBalanceManager.getInitialBalances(context)
        initialBalances.keys.forEach { acc ->
          InitialBalanceManager.removeInitialBalance(context, acc, userId = activeUserId, scope = scope)
        }
        LocalDataCache.clear(context, activeUserId)
        persistLocalCache()
        onUpdateGoalMilestones()
        HapticHelper.vibrateSuccess(context)
        if (activeUserId.isNotBlank()) {
          scope.launch(Dispatchers.IO) {
            InitialBalanceManager.deleteAllInitialBalancesFromCloud(context, activeUserId)
            oldEntries.forEach { runCatching { SupabaseRepository.deleteMoneyEntry(it.id) } }
            onRefreshInsights(activeUserId, insightsTimeframe, false)
          }
        }
        onToast("Semua transaksi & saldo awal telah dihapus. Anda memulai dari 0!")
      },
      onToast = onToast,
      userId = activeUserId,
    )

    Page.Themes -> ThemesPage(currentTheme) { selected ->
      onRequestConfirm("${strings["confirm_theme_change"]} ${themeLabel(selected, strings)}?") {
        onThemeChange(selected)
      }
    }
  }
}
