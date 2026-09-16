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
import com.solvix.tabungan.components.AddCategoryDialog
import com.solvix.tabungan.components.RecurringSubscriptionsModal

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
  var showAddCategoryDialog by remember { mutableStateOf(false) }

  var customBankInput by rememberSaveable { mutableStateOf("") }
  var customAccountsList by remember { mutableStateOf(CustomAccountManager.getCustomAccounts(context)) }

  val receiptLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
  ) { uri ->
    if (uri != null) {
      receiptUri = ReceiptStorageHelper.saveReceiptToInternalStorage(context, uri)
    }
  }

  LaunchedEffect(editEntry?.id) {
    val entry = editEntry
    if (entry != null && entry.type == EntryType.Expense) {
      editingId = entry.id
      amount = entry.amount.toString()
      date = entry.date
      category = entry.category
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
    AppCard {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AppTextField(
          label = strings["label_amount"],
          value = amount,
          onValueChange = { amount = it },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        DateField(strings["label_date"], value = date, onValueChange = { date = it }, placeholder = strings["placeholder_date"])
        AppTextField(strings["label_category"], value = category, onValueChange = { category = it }, placeholder = strings["placeholder_category_expense"])

        // Quick Category Chips & "+ Kategori Baru"
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          // Tombol Tambah Kategori Kustom
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(999.dp))
              .background(colors.accent.copy(alpha = 0.12f))
              .border(1.dp, colors.accent.copy(alpha = 0.4f), RoundedCornerShape(999.dp))
              .clickable { showAddCategoryDialog = true }
              .padding(horizontal = 10.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(text = strings["category_add_button"].ifBlank { "+ Kategori" }, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.accent)
          }

          categoriesList.forEach { cat ->
            val isSelected = cat.equals(category, ignoreCase = true)
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(if (isSelected) colors.accent else colors.bg2)
                .clickable { category = cat }
                .padding(horizontal = 10.dp, vertical = 5.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = cat,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else colors.text,
              )
            }
          }
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
            label = if (language == AppLanguage.ID) "Nama Bank / Dompet Baru (Manual)" else "New Bank / Wallet Name (Manual)",
            value = customBankInput,
            onValueChange = { customBankInput = it },
            placeholder = if (language == AppLanguage.ID) "Contoh: Bibit, Indodax, Bank Jatim" else "e.g. Bibit, Indodax",
          )
        }

        // Catatan dengan Smart Auto-Categorization
        AppTextField(
          label = strings["label_note"],
          value = note,
          onValueChange = { newNote ->
            note = newNote
            if (category.isBlank() || categoriesList.any { it.equals(category, ignoreCase = true) }) {
              val detected = SmartCategoryHelper.detectCategory(newNote, EntryType.Expense)
              if (detected != null) {
                category = detected
              }
            }
          },
          placeholder = strings["placeholder_note"],
          minLines = 2,
        )

        // Lampiran Foto Struk / Bukti Transfer (100% Opsional)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(text = strings["receipt_attachment_title"].ifBlank { "Lampiran Bukti / Struk (Opsional)" }, fontSize = 12.sp, color = colors.muted, fontWeight = FontWeight.SemiBold)
          if (receiptUri.isBlank()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(colors.bg2)
                .border(1.dp, colors.cardBorder, RoundedCornerShape(10.dp))
                .clickable { receiptLauncher.launch("image/*") }
                .padding(vertical = 12.dp),
              contentAlignment = Alignment.Center,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "📷", fontSize = 14.sp)
                Text(text = strings["receipt_pick_expense"].ifBlank { "+ Pilih Foto Struk / Bukti Belanja (Opsional)" }, fontSize = 12.sp, color = colors.accent, fontWeight = FontWeight.SemiBold)
              }
            }
          } else {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(colors.accent.copy(alpha = 0.1f))
                .border(1.dp, colors.accent.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "📎", fontSize = 14.sp)
                Text(text = strings["receipt_attached"].ifBlank { "Foto Bukti Terlampir" }, fontSize = 12.sp, color = colors.text, fontWeight = FontWeight.Bold)
              }
              Box(
                modifier = Modifier
                  .clip(CircleShape)
                  .background(colors.danger.copy(alpha = 0.15f))
                  .clickable { receiptUri = "" }
                  .padding(horizontal = 8.dp, vertical = 4.dp),
              ) {
                Text(text = "${strings["delete"]} ✕", fontSize = 11.sp, color = colors.danger, fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        val isBankValid = bank.isNotBlank() && (bank != otherLabel || customBankInput.isNotBlank())
        val isFormValid = parseAmount(amount) > 0 &&
          date.isNotBlank() &&
          category.isNotBlank() &&
          method.isNotBlank() &&
          isBankValid

        GradientButton(
          text = if (editingId == null) strings["save_expense"] else strings["update_expense"],
          enabled = isFormValid,
          onClick = {
            if (!isFormValid) return@GradientButton
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
              category = category,
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

            // Peringatan Batas Anggaran Kategori
            val budgetCheck = BudgetAlertHelper.checkBudgetStatus(
              context = context,
              category = category,
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

  if (showAddCategoryDialog) {
    AddCategoryDialog(
      onDismiss = { showAddCategoryDialog = false },
      placeholder = strings["category_placeholder_expense"].ifBlank { "Misal: Hobi, Kucing, Donasi" },
      strings = strings,
      onConfirm = { newCat ->
        CategoryManager.addExpenseCategory(context, newCat)
        categoriesList = CategoryManager.getExpenseCategories(context)
        category = newCat
        showAddCategoryDialog = false
      },
    )
  }

  if (showRecurringModal) {
    RecurringSubscriptionsModal(
      onDismiss = { showRecurringModal = false },
      onEntriesGenerated = onEntriesGenerated,
    )
  }
}
