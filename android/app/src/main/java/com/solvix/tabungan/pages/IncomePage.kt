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
import com.solvix.tabungan.components.AccountAutoResearchPreview
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.solvix.tabungan.components.ReceiptAttachmentField

@Composable
fun IncomePage(
  onSave: (MoneyEntry) -> Unit,
  onUpdate: (MoneyEntry) -> Unit,
  editEntry: MoneyEntry?,
  onEditConsumed: () -> Unit,
  strings: AppStrings,
) {
  val context = LocalContext.current
  val colors = LocalAppColors.current
  val language = LocalLanguage.current
  var amount by rememberSaveable { mutableStateOf("") }
  var date by rememberSaveable { mutableStateOf("") }
  var category by rememberSaveable { mutableStateOf("") }
  var source by rememberSaveable { mutableStateOf("") }
  var channel by rememberSaveable { mutableStateOf("") }
  var note by rememberSaveable { mutableStateOf("") }
  var receiptUri by rememberSaveable { mutableStateOf("") }
  var editingId by rememberSaveable { mutableStateOf<String?>(null) }
  var editingTime by rememberSaveable { mutableStateOf<String?>(null) }
  var editingCreatedAt by rememberSaveable { mutableStateOf<String?>(null) }

  var categoriesList by remember { mutableStateOf(CategoryManager.getIncomeCategories(context)) }
  var customCategoryInput by rememberSaveable { mutableStateOf("") }

  var customSourceInput by rememberSaveable { mutableStateOf("") }
  var customChannelInput by rememberSaveable { mutableStateOf("") }
  var customAccountsList by remember { mutableStateOf(CustomAccountManager.getCustomAccounts(context)) }
  var customSourcesList by remember { mutableStateOf(CustomAccountManager.getCustomSources(context)) }

  val receiptLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
  ) { uri ->
    if (uri != null) {
      receiptUri = ReceiptStorageHelper.saveReceiptToInternalStorage(context, uri)
    }
  }

  LaunchedEffect(editEntry?.id) {
    val entry = editEntry
    if (entry != null && entry.type == EntryType.Income) {
      editingId = entry.id
      amount = entry.amount.toString()
      date = entry.date
      category = entry.category
      if (entry.category.isNotBlank() && !categoriesList.contains(entry.category)) {
        CategoryManager.addIncomeCategory(context, entry.category)
        categoriesList = CategoryManager.getIncomeCategories(context)
      }
      source = entry.sourceOrMethod
      channel = entry.channelOrBank
      note = entry.note
      receiptUri = entry.receiptUri
      editingTime = extractTime(entry.date)
      editingCreatedAt = entry.createdAt.ifBlank { null }
      onEditConsumed()
    }
  }

  Column(modifier = Modifier.imePadding()) {
    SectionTitle(
      icon = themePageVisualIcon(LocalThemeName.current, Page.Income),
      title = strings["section_income_title"],
      subtitle = strings["section_income_subtitle"],
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

        val otherCategoryLabel = if (language == AppLanguage.ID) "Lainnya" else "Other"
        val categoryOptions = remember(categoriesList, otherCategoryLabel) {
          (categoriesList.filter { it != otherCategoryLabel } + listOf(otherCategoryLabel)).distinct()
        }

        AppDropdown(
          label = strings["label_category"].ifBlank { if (language == AppLanguage.ID) "Kategori Pemasukan" else "Income Category" },
          placeholder = strings["placeholder_category_income"].ifBlank { if (language == AppLanguage.ID) "Pilih Kategori" else "Select Category" },
          options = categoryOptions,
          selected = category,
          onSelected = { sel -> category = sel },
        )

        if (category == otherCategoryLabel) {
          AppTextField(
            label = if (language == AppLanguage.ID) "Nama Kategori Baru" else "New Category Name",
            value = customCategoryInput,
            onValueChange = { customCategoryInput = it },
            placeholder = if (language == AppLanguage.ID) "Contoh: Gaji, Bonus, Penjualan" else "e.g. Salary, Bonus, Sales",
          )
        }

        val otherLabel = if (language == AppLanguage.ID) "Lainnya" else "Other"
        val baseSources = optionList(language, OptionList.IncomeSources)
        val sourceOptions = remember(baseSources, customSourcesList) {
          (baseSources.filter { it != otherLabel } + customSourcesList + listOf(otherLabel)).distinct()
        }

        val baseChannels = optionList(language, OptionList.IncomeChannels)
        val channelOptions = remember(baseChannels, customAccountsList) {
          (baseChannels.filter { it != otherLabel } + customAccountsList + listOf(otherLabel)).distinct()
        }

        AppDropdown(
          label = strings["label_source"],
          placeholder = strings["placeholder_source"],
          options = sourceOptions,
          selected = source,
          onSelected = { source = it },
        )
        if (source == otherLabel) {
          AppTextField(
            label = if (language == AppLanguage.ID) "Nama Sumber Baru" else "New Source Name",
            value = customSourceInput,
            onValueChange = { customSourceInput = it },
            placeholder = if (language == AppLanguage.ID) "Contoh: Dividen, Royalti" else "e.g. Dividend, Royalty",
          )
        }

        AppDropdown(
          label = strings["label_channel"],
          placeholder = strings["placeholder_channel"],
          options = channelOptions,
          selected = channel,
          onSelected = { channel = it },
        )
        if (channel == otherLabel) {
          AppTextField(
            label = if (language == AppLanguage.ID) "Nama Akun / Bank Baru" else "New Account / Bank Name",
            value = customChannelInput,
            onValueChange = { customChannelInput = it },
            placeholder = if (language == AppLanguage.ID) "Contoh: Bank Neo, Solana, LinkAja" else "e.g. Bank Neo, Solana",
          )
          AccountAutoResearchPreview(
            query = customChannelInput,
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
              val detected = SmartCategoryHelper.detectCategory(newNote, EntryType.Income)
              if (detected != null) {
                if (!categoriesList.contains(detected)) {
                  CategoryManager.addIncomeCategory(context, detected)
                  categoriesList = CategoryManager.getIncomeCategories(context)
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
          pickLabel = strings["receipt_pick_income"].ifBlank { "+ Foto Bukti Transfer (Opsional)" },
          attachmentTitle = strings["receipt_attachment_title"].ifBlank { "Lampiran Bukti / Struk" },
          attachedLabel = strings["receipt_attached"].ifBlank { "Foto Bukti Terlampir" },
          deleteLabel = strings["delete"],
        )

        val isCategoryValid = category.isNotBlank() && (category != otherCategoryLabel || customCategoryInput.isNotBlank())
        val isSourceValid = source.isNotBlank() && (source != otherLabel || customSourceInput.isNotBlank())
        val isChannelValid = channel.isNotBlank() && (channel != otherLabel || customChannelInput.isNotBlank())
        val isFormValid = parseAmount(amount) > 0 &&
          date.isNotBlank() &&
          isCategoryValid &&
          isSourceValid &&
          isChannelValid

        GradientButton(
          text = if (editingId == null) strings["save_income"] else strings["update_income"],
          enabled = isFormValid,
          onClick = {
            if (!isFormValid) return@GradientButton
            val finalCategory = if (category == otherCategoryLabel && customCategoryInput.isNotBlank()) {
              val trimmed = customCategoryInput.trim()
              CategoryManager.addIncomeCategory(context, trimmed)
              categoriesList = CategoryManager.getIncomeCategories(context)
              trimmed
            } else {
              category
            }

            val finalSource = if (source == otherLabel && customSourceInput.isNotBlank()) {
              val trimmed = customSourceInput.trim()
              CustomAccountManager.addCustomSource(context, trimmed)
              customSourcesList = CustomAccountManager.getCustomSources(context)
              trimmed
            } else {
              source
            }

            val finalChannel = if (channel == otherLabel && customChannelInput.isNotBlank()) {
              val trimmed = customChannelInput.trim()
              CustomAccountManager.addCustomAccount(context, trimmed)
              customAccountsList = CustomAccountManager.getCustomAccounts(context)
              trimmed
            } else {
              channel
            }

            val timeFallback = editingTime ?: nowJakartaTime()
            val normalizedDate = ensureDateHasTime(date, timeFallback)
            val createdAt = editingCreatedAt ?: nowJakartaText()
            val entry = MoneyEntry(
              id = editingId ?: UUIDString(),
              type = EntryType.Income,
              amount = parseAmount(amount),
              date = normalizedDate,
              category = finalCategory,
              note = note,
              sourceOrMethod = finalSource,
              channelOrBank = finalChannel,
              createdAt = createdAt,
              receiptUri = receiptUri,
            )
            if (editingId == null) {
              onSave(entry)
            } else {
              onUpdate(entry)
            }
            amount = ""
            date = ""
            category = ""
            customCategoryInput = ""
            source = ""
            channel = ""
            customSourceInput = ""
            customChannelInput = ""
            note = ""
            receiptUri = ""
            editingId = null
            editingTime = null
            editingCreatedAt = null
          },
        )
      }
    }
  }
}
