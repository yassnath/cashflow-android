package com.solvix.tabungan

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.solvix.tabungan.components.AccountAutoResearchPreview
import com.solvix.tabungan.components.ReceiptAttachmentField
import com.solvix.tabungan.components.RecurringSubscriptionsModal
import com.solvix.tabungan.pages.expense.ExpenseOcrBanner
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpensePage(
  onSave: (MoneyEntry) -> Unit,
  onUpdate: (MoneyEntry) -> Unit,
  editEntry: MoneyEntry?,
  onEditConsumed: () -> Unit,
  strings: AppStrings,
  existingExpenses: List<MoneyEntry> = emptyList(),
  onBudgetWarning: (String) -> Unit = {},
  onEntriesGenerated: (List<MoneyEntry>) -> Unit = {},
) {
  val context = LocalContext.current
  val colors = LocalAppColors.current
  val language = LocalLanguage.current
  var amount by rememberSaveable { mutableStateOf("") }
  var date by rememberSaveable { mutableStateOf("") }
  var category by rememberSaveable { mutableStateOf("") }
  var method by rememberSaveable { mutableStateOf("") }
  var bank by rememberSaveable { mutableStateOf("") }
  var note by rememberSaveable { mutableStateOf("") }
  var receiptUri by rememberSaveable { mutableStateOf("") }
  var editingId by rememberSaveable { mutableStateOf<String?>(null) }
  var editingTime by rememberSaveable { mutableStateOf<String?>(null) }
  var editingCreatedAt by rememberSaveable { mutableStateOf<String?>(null) }

  var showRecurringModal by remember { mutableStateOf(false) }
  var categoriesList by remember { mutableStateOf(CategoryManager.getExpenseCategories(context)) }
  var customCategoryInput by rememberSaveable { mutableStateOf("") }

  var customBankInput by rememberSaveable { mutableStateOf("") }
  var customAccountsList by remember { mutableStateOf(CustomAccountManager.getCustomAccounts(context)) }

  var isScanningOcr by remember { mutableStateOf(false) }

  val receiptLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
  ) { uri ->
    if (uri != null) {
      receiptUri = ReceiptStorageHelper.saveReceiptToInternalStorage(context, uri)
    }
  }

  val ocrLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
  ) { uri ->
    if (uri != null) {
      isScanningOcr = true
      val saved = ReceiptStorageHelper.saveReceiptToInternalStorage(context, uri)
      receiptUri = saved
      ReceiptOcrHelper.processReceiptImage(context, uri) { result ->
        isScanningOcr = false
        if (result.amount != null) {
          amount = result.amount.toString()
        }
        if (!result.date.isNullOrBlank()) {
          val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
          date = "${result.date} $currentTime"
        }
        if (!result.merchant.isNullOrBlank() && note.isBlank()) {
          note = result.merchant
        }
      }
    }
  }

  LaunchedEffect(editEntry?.id) {
    val entry = editEntry
    if (entry != null && entry.type == EntryType.Expense) {
      editingId = entry.id
      amount = entry.amount.toString()
      date = entry.date
      category = entry.category
      if (entry.category.isNotBlank() && !categoriesList.contains(entry.category)) {
        CategoryManager.addExpenseCategory(context, entry.category)
        categoriesList = CategoryManager.getExpenseCategories(context)
      }
      method = entry.sourceOrMethod
      bank = entry.channelOrBank
      note = entry.note
      receiptUri = entry.receiptUri
      editingTime = extractTime(entry.date)
      editingCreatedAt = entry.createdAt.ifBlank { null }
      onEditConsumed()
    }
  }

  Column(modifier = Modifier.imePadding()) {
    SectionTitle(
      icon = themePageVisualIcon(LocalThemeName.current, Page.Expense),
      title = strings["section_expense_title"],
      subtitle = strings["section_expense_subtitle"],
    )

    // Tombol Scan Struk Otomatis (OCR)
    ExpenseOcrBanner(
      isScanning = isScanningOcr,
      title = strings["ocr_scan_title"].ifBlank { "Scan Struk (OCR)" },
      onClick = { ocrLauncher.launch("image/*") },
    )

    Spacer(modifier = Modifier.height(10.dp))

    AppCard {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AppTextField(
          label = strings["label_amount"],
          value = amount,
          onValueChange = { amount = it },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        DateField(strings["label_date"], value = date, onValueChange = { date = it }, placeholder = strings["placeholder_date"])

        val otherCategoryLabel = if (language == AppLanguage.ID) "Lainnya" else "Other"
        val categoryOptions = remember(categoriesList, otherCategoryLabel) {
          (categoriesList.filter { it != otherCategoryLabel } + listOf(otherCategoryLabel)).distinct()
        }

        AppDropdown(
          label = strings["label_category"].ifBlank { if (language == AppLanguage.ID) "Kategori Pengeluaran" else "Expense Category" },
          placeholder = strings["placeholder_category_expense"].ifBlank { if (language == AppLanguage.ID) "Pilih Kategori" else "Select Category" },
          options = categoryOptions,
          selected = category,
          onSelected = { sel -> category = sel },
        )

        if (category == otherCategoryLabel) {
          AppTextField(
            label = if (language == AppLanguage.ID) "Nama Kategori Baru" else "New Category Name",
            value = customCategoryInput,
            onValueChange = { customCategoryInput = it },
            placeholder = if (language == AppLanguage.ID) "Contoh: Makanan, Belanja, Tagihan" else "e.g. Food, Shopping, Bills",
          )
        }

        AppDropdown(
          label = strings["label_method"],
          placeholder = strings["placeholder_method"],
          options = optionList(language, OptionList.ExpenseMethods),
          selected = method,
          onSelected = { method = it },
        )
        val otherLabel = if (language == AppLanguage.ID) "Lainnya" else "Other"
        val baseBanks = optionList(language, OptionList.ExpenseBanks)
        val bankOptions = remember(baseBanks, customAccountsList) {
          (baseBanks.filter { it != otherLabel } + customAccountsList + listOf(otherLabel)).distinct()
        }

        AppDropdown(
          label = strings["label_bank"],
          placeholder = strings["placeholder_bank"],
          options = bankOptions,
          selected = bank,
          onSelected = { bank = it },
        )
        if (bank == otherLabel) {
          AppTextField(
            label = if (language == AppLanguage.ID) "Nama Akun / Bank Baru" else "New Account / Bank Name",
            value = customBankInput,
            onValueChange = { customBankInput = it },
            placeholder = if (language == AppLanguage.ID) "Contoh: Bank Neo, Solana, LinkAja" else "e.g. Bank Neo, Solana",
          )
          AccountAutoResearchPreview(
            query = customBankInput,
            modifier = Modifier.padding(top = 2.dp),
          )
        }

        // Catatan dengan Smart Auto-Categorization
        AppTextField(
          label = strings["label_note"],
          value = note,
          onValueChange = { newNote ->
            note = newNote
            if (category.isBlank() || category == otherCategoryLabel) {
              val detected = SmartCategoryHelper.detectCategory(newNote, EntryType.Expense)
              if (detected != null) {
                if (!categoriesList.contains(detected)) {
                  CategoryManager.addExpenseCategory(context, detected)
                  categoriesList = CategoryManager.getExpenseCategories(context)
                }
                category = detected
              }
            }
          },
          placeholder = strings["placeholder_note"],
          minLines = 2,
        )

        // Lampiran Foto Struk / Bukti Transfer (100% Opsional)
        ReceiptAttachmentField(
          receiptUri = receiptUri,
          onPickReceipt = { receiptLauncher.launch("image/*") },
          onRemoveReceipt = { receiptUri = "" },
          pickLabel = strings["receipt_pick_expense"].ifBlank { "+ Foto Struk / Bukti (Opsional)" },
          attachmentTitle = strings["receipt_attachment_title"].ifBlank { "Lampiran Bukti / Struk" },
          attachedLabel = strings["receipt_attached"].ifBlank { "Foto Bukti Terlampir" },
          deleteLabel = strings["delete"],
        )

        val isCategoryValid = category.isNotBlank() && (category != otherCategoryLabel || customCategoryInput.isNotBlank())
        val isBankValid = bank.isNotBlank() && (bank != otherLabel || customBankInput.isNotBlank())
        val isFormValid = parseAmount(amount) > 0 &&
          date.isNotBlank() &&
          isCategoryValid &&
          method.isNotBlank() &&
          isBankValid

        GradientButton(
          text = if (editingId == null) strings["save_expense"] else strings["update_expense"],
          enabled = isFormValid,
          onClick = {
            if (!isFormValid) return@GradientButton
            val finalCategory = if (category == otherCategoryLabel && customCategoryInput.isNotBlank()) {
              val trimmed = customCategoryInput.trim()
              CategoryManager.addExpenseCategory(context, trimmed)
              categoriesList = CategoryManager.getExpenseCategories(context)
              trimmed
            } else {
              category
            }

            val finalBank = if (bank == otherLabel && customBankInput.isNotBlank()) {
              val trimmed = customBankInput.trim()
              CustomAccountManager.addCustomAccount(context, trimmed)
              customAccountsList = CustomAccountManager.getCustomAccounts(context)
              trimmed
            } else {
              bank
            }

            val timeFallback = editingTime ?: nowJakartaTime()
            val normalizedDate = ensureDateHasTime(date, timeFallback)
            val createdAt = editingCreatedAt ?: nowJakartaText()
            val entry = MoneyEntry(
              id = editingId ?: UUIDString(),
              type = EntryType.Expense,
              amount = parseAmount(amount),
              date = normalizedDate,
              category = finalCategory,
              note = note,
              sourceOrMethod = method,
              channelOrBank = finalBank,
              createdAt = createdAt,
              receiptUri = receiptUri,
            )
            if (editingId == null) {
              onSave(entry)
            } else {
              onUpdate(entry)
            }
            customBankInput = ""
            customCategoryInput = ""

            // Peringatan Batas Anggaran Kategori
            val budgetCheck = BudgetAlertHelper.checkBudgetStatus(
              context = context,
              category = finalCategory,
              newAmount = entry.amount,
              existingExpenses = existingExpenses,
              editingEntryId = editingId,
            )
            if (budgetCheck != null && budgetCheck.isNearLimit && budgetCheck.message.isNotBlank()) {
              onBudgetWarning(budgetCheck.message)
            }

            amount = ""
            date = ""
            category = ""
            method = ""
            bank = ""
            note = ""
            receiptUri = ""
            editingId = null
            editingTime = null
            editingCreatedAt = null
          },
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Kelola Langganan Rutin
    AppCard {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(text = strings["recurring_card_title"].ifBlank { "Langganan & Tagihan Rutin" }, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.text)
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = strings["recurring_card_subtitle"].ifBlank {
              "Kelola tagihan berulang (Netflix, Spotify, Kos, Listrik, dll) agar otomatis tercatat saat tempo."
            },
            fontSize = 12.sp,
            color = colors.muted,
          )
        }
        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.accent.copy(alpha = 0.12f))
            .clickable { showRecurringModal = true }
            .padding(horizontal = 12.dp, vertical = 8.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(text = strings["recurring_card_action"].ifBlank { "⚙️ Kelola" }, color = colors.accent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
      }
    }
  }


  if (showRecurringModal) {
    RecurringSubscriptionsModal(
      onDismiss = { showRecurringModal = false },
      onEntriesGenerated = onEntriesGenerated,
    )
  }
}
