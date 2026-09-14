package com.solvix.tabungan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.solvix.tabungan.AppCard
import com.solvix.tabungan.AppDropdown
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.AppTextField
import com.solvix.tabungan.EntryType
import com.solvix.tabungan.GradientButton
import com.solvix.tabungan.LocalLanguage
import com.solvix.tabungan.LocalThemeName
import com.solvix.tabungan.MoneyEntry
import com.solvix.tabungan.OptionList
import com.solvix.tabungan.Page
import com.solvix.tabungan.SectionTitle
import com.solvix.tabungan.UUIDString
import com.solvix.tabungan.ensureDateHasTime
import com.solvix.tabungan.extractTime
import com.solvix.tabungan.nowJakartaText
import com.solvix.tabungan.nowJakartaTime
import com.solvix.tabungan.optionList
import com.solvix.tabungan.parseAmount
import com.solvix.tabungan.themePageIcon

@Composable
fun IncomePage(
  onSave: (MoneyEntry) -> Unit,
  onUpdate: (MoneyEntry) -> Unit,
  editEntry: MoneyEntry?,
  onEditConsumed: () -> Unit,
  strings: AppStrings,
) {
  val language = LocalLanguage.current
  var amount by rememberSaveable { mutableStateOf("") }
  var date by rememberSaveable { mutableStateOf("") }
  var category by rememberSaveable { mutableStateOf("") }
  var source by rememberSaveable { mutableStateOf("") }
  var channel by rememberSaveable { mutableStateOf("") }
  var note by rememberSaveable { mutableStateOf("") }
  var editingId by rememberSaveable { mutableStateOf<String?>(null) }
  var editingTime by rememberSaveable { mutableStateOf<String?>(null) }
  var editingCreatedAt by rememberSaveable { mutableStateOf<String?>(null) }

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
      editingTime = extractTime(entry.date)
      editingCreatedAt = entry.createdAt.ifBlank { null }
      onEditConsumed()
    }
  }

  Column {
    SectionTitle(icon = themePageVisualIcon(LocalThemeName.current, Page.Income), title = strings["section_income_title"], subtitle = strings["section_income_subtitle"])
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
        AppDropdown(
          label = strings["label_source"],
          placeholder = strings["placeholder_source"],
          options = optionList(language, OptionList.IncomeSources),
          selected = source,
          onSelected = { source = it },
        )
        AppDropdown(
          label = strings["label_channel"],
          placeholder = strings["placeholder_channel"],
          options = optionList(language, OptionList.IncomeChannels),
          selected = channel,
          onSelected = { channel = it },
        )
        AppTextField(strings["label_note"], value = note, onValueChange = { note = it }, placeholder = strings["placeholder_note"], minLines = 2)
        val isFormValid = parseAmount(amount) > 0 &&
          date.isNotBlank() &&
          category.isNotBlank() &&
          source.isNotBlank() &&
          channel.isNotBlank()
        GradientButton(
          text = if (editingId == null) strings["save_income"] else strings["update_income"],
          enabled = isFormValid,
          onClick = {
            if (!isFormValid) return@GradientButton
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
              sourceOrMethod = source,
              channelOrBank = channel,
              createdAt = createdAt,
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
            note = ""
            editingId = null
            editingTime = null
            editingCreatedAt = null
          },
        )
      }
    }
  }
}
