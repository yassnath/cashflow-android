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
  var showAddCategoryDialog by remember { mutableStateOf(false) }

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
        AppTextField(strings["label_category"], value = category, onValueChange = { category = it }, placeholder = strings["placeholder_category_income"])

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
            label = if (language == AppLanguage.ID) "Nama Sumber Baru (Manual)" else "New Source Name (Manual)",
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
            label = if (language == AppLanguage.ID) "Nama Bank / Dompet Baru (Manual)" else "New Bank / Wallet Name (Manual)",
            value = customChannelInput,
            onValueChange = { customChannelInput = it },
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
              val detected = SmartCategoryHelper.detectCategory(newNote, EntryType.Income)
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
                Text(text = strings["receipt_pick_income"].ifBlank { "+ Pilih Foto Bukti Transfer (Opsional)" }, fontSize = 12.sp, color = colors.accent, fontWeight = FontWeight.SemiBold)
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

        val isSourceValid = source.isNotBlank() && (source != otherLabel || customSourceInput.isNotBlank())
        val isChannelValid = channel.isNotBlank() && (channel != otherLabel || customChannelInput.isNotBlank())
        val isFormValid = parseAmount(amount) > 0 &&
          date.isNotBlank() &&
          category.isNotBlank() &&
          isSourceValid &&
          isChannelValid

        GradientButton(
          text = if (editingId == null) strings["save_income"] else strings["update_income"],
          enabled = isFormValid,
          onClick = {
            if (!isFormValid) return@GradientButton
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
              category = category,
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

  if (showAddCategoryDialog) {
    AddCategoryDialog(
      onDismiss = { showAddCategoryDialog = false },
      placeholder = strings["category_placeholder_income"].ifBlank { "Misal: Bonus, Dividen, Freelance" },
      strings = strings,
      onConfirm = { newCat ->
        CategoryManager.addIncomeCategory(context, newCat)
        categoriesList = CategoryManager.getIncomeCategories(context)
        category = newCat
        showAddCategoryDialog = false
      },
    )
  }
}
