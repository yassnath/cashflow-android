package com.solvix.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppCard
import com.solvix.tabungan.AppDimens
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.AppTextField
import com.solvix.tabungan.ChipButton
import com.solvix.tabungan.DateField
import com.solvix.tabungan.EntryType
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.LocalThemeName
import com.solvix.tabungan.MoneyEntry
import com.solvix.tabungan.Page
import com.solvix.tabungan.SectionTitle
import com.solvix.tabungan.StatRow
import com.solvix.tabungan.formatCreatedAt
import com.solvix.tabungan.formatRupiah
import com.solvix.tabungan.isDarkTheme
import com.solvix.tabungan.parseCreatedAtMillis
import com.solvix.tabungan.parseDate
import com.solvix.tabungan.parseDateTimeMillis
import com.solvix.tabungan.themePageIcon
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HistoryPage(
  entries: List<MoneyEntry>,
  strings: AppStrings,
  onEdit: (MoneyEntry) -> Unit,
  onDelete: (MoneyEntry) -> Unit,
) {
  val colors = LocalAppColors.current
  val isDark = isDarkTheme(LocalThemeName.current)
  val incomeColor = if (isDark) Color(0xFF7BE27A) else Color(0xFF2E7D32)
  val expenseColor = if (isDark) Color(0xFFFF8A80) else Color(0xFFC62828)

  val defaultRange = remember {
    val now = Calendar.getInstance()
    val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.US)
    val start = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }.time
    val end = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, now.getActualMaximum(Calendar.DAY_OF_MONTH)) }.time
    formatter.format(start) to formatter.format(end)
  }

  var fromDate by rememberSaveable { mutableStateOf(defaultRange.first) }
  var toDate by rememberSaveable { mutableStateOf(defaultRange.second) }
  var searchQuery by rememberSaveable { mutableStateOf("") }
  var selectedCategory by rememberSaveable { mutableStateOf("Semua") }

  val dateFiltered = entries.filter { entry ->
    val date = parseDate(entry.date) ?: return@filter false
    val from = parseDate(fromDate)
    val to = parseDate(toDate)
    (from == null || date >= from) && (to == null || date <= to)
  }

  val availableCategories = remember(entries) {
    listOf("Semua") + entries.map { it.category }.distinct().filter { it.isNotBlank() }
  }

  val finalFiltered = dateFiltered.filter { entry ->
    val matchesSearch = searchQuery.isBlank() ||
      entry.category.contains(searchQuery, ignoreCase = true) ||
      entry.note.contains(searchQuery, ignoreCase = true) ||
      entry.sourceOrMethod.contains(searchQuery, ignoreCase = true) ||
      entry.channelOrBank.contains(searchQuery, ignoreCase = true) ||
      entry.amount.toString().contains(searchQuery)
    val matchesCategory = selectedCategory == "Semua" || entry.category.equals(selectedCategory, ignoreCase = true)
    matchesSearch && matchesCategory
  }

  val sorted = finalFiltered.sortedByDescending { entry ->
    parseDateTimeMillis(entry.date)
      ?: parseCreatedAtMillis(entry.createdAt)
      ?: (parseDate(entry.date) ?: 0L)
  }

  val incomeTotal = dateFiltered.filter { it.type == EntryType.Income }.sumOf { it.amount }
  val expenseTotal = dateFiltered.filter { it.type == EntryType.Expense }.sumOf { it.amount }

  Column {
    SectionTitle(icon = themePageVisualIcon(LocalThemeName.current, Page.History), title = strings["section_history_title"], subtitle = strings["section_history_subtitle"])

    AppCard {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Column(modifier = Modifier.weight(1f)) {
            Text(text = strings["from"], fontSize = 11.sp, color = colors.text)
            Spacer(modifier = Modifier.height(4.dp))
            DateField(label = "", value = fromDate, onValueChange = { fromDate = it }, placeholder = strings["placeholder_date"])
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(text = strings["to"], fontSize = 11.sp, color = colors.text)
            Spacer(modifier = Modifier.height(4.dp))
            DateField(label = "", value = toDate, onValueChange = { toDate = it }, placeholder = strings["placeholder_date"])
          }
        }
        AppTextField(
          label = "Cari Riwayat",
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = "Cari kategori, catatan, atau bank...",
        )
        if (availableCategories.size > 1) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            availableCategories.forEach { cat ->
              val isSelected = cat == selectedCategory
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(999.dp))
                  .background(if (isSelected) colors.accent else colors.bg2)
                  .clickable { selectedCategory = cat }
                  .padding(horizontal = 12.dp, vertical = 6.dp),
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
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))
    AppCard {
      Text(text = strings["summary_period"], fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.text)
      Spacer(modifier = Modifier.height(10.dp))
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        StatRow(icon = "⬆️", label = strings["summary_income"], value = formatRupiah(incomeTotal), accentColor = incomeColor)
        StatRow(icon = "⬇️", label = strings["summary_expense"], value = formatRupiah(expenseTotal), accentColor = expenseColor)
        StatRow(icon = "✨", label = strings["summary_balance"], value = formatRupiah(incomeTotal - expenseTotal))
      }
    }

    EntryList(
      title = strings["section_history_title"],
      entries = sorted,
      strings = strings,
      onEdit = onEdit,
      onDelete = onDelete,
    )
    if (finalFiltered.isEmpty()) {
      Spacer(modifier = Modifier.height(10.dp))
      AppCard {
        Text(text = strings["no_transactions"], color = colors.muted, fontSize = 12.sp)
      }
    }
  }
}

@Composable
fun EntryList(
  title: String,
  entries: List<MoneyEntry>,
  strings: AppStrings,
  onEdit: (MoneyEntry) -> Unit,
  onDelete: (MoneyEntry) -> Unit,
) {
  if (entries.isEmpty()) return
  val colors = LocalAppColors.current
  Spacer(modifier = Modifier.height(12.dp))
  Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.text)
  Spacer(modifier = Modifier.height(8.dp))
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    entries.forEach { entry ->
      EntryCard(entry = entry, strings = strings, onEdit = onEdit, onDelete = onDelete)
    }
  }
}

@Composable
fun EntryCard(
  entry: MoneyEntry,
  strings: AppStrings,
  onEdit: (MoneyEntry) -> Unit,
  onDelete: (MoneyEntry) -> Unit,
) {
  val colors = LocalAppColors.current
  val isIncome = entry.type == EntryType.Income
  val icon = if (isIncome) "⬆️" else "⬇️"
  val amountPrefix = if (isIncome) "+ " else "- "
  val isDark = isDarkTheme(LocalThemeName.current)
  val amountColor = if (isIncome) {
    if (isDark) Color(0xFF7BE27A) else Color(0xFF2E7D32)
  } else {
    if (isDark) Color(0xFFFF8A80) else Color(0xFFC62828)
  }
  val badgeBg = if (isIncome) colors.accent.copy(alpha = 0.2f) else colors.danger.copy(alpha = 0.2f)

  AppCard(shape = RoundedCornerShape(AppDimens.radiusMd)) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(badgeBg),
            contentAlignment = Alignment.Center,
          ) {
            Text(text = icon, fontSize = 12.sp)
          }
          Text(text = entry.category, fontWeight = FontWeight.Bold, color = colors.text)
        }
        Text(text = "$amountPrefix${formatRupiah(entry.amount)}", fontWeight = FontWeight.Bold, color = amountColor)
      }
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = "${entry.sourceOrMethod} • ${entry.channelOrBank}", color = colors.muted, fontSize = 12.sp)
        Text(text = entry.date, color = colors.muted, fontSize = 12.sp)
      }
      if (entry.createdAt.isNotBlank()) {
        Text(text = "Dibuat: ${formatCreatedAt(entry.createdAt)}", color = colors.muted, fontSize = 11.sp)
      }
      if (entry.note.isNotBlank()) {
        Text(text = entry.note, color = colors.muted, fontSize = 12.sp)
      }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Box(
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.linearGradient(listOf(colors.accent, colors.accent2)))
            .clickable { onEdit(entry) },
          contentAlignment = Alignment.Center,
        ) {
          Text(text = strings["edit"], color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Box(
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.linearGradient(listOf(colors.danger, colors.danger.copy(alpha = 0.85f))))
            .clickable { onDelete(entry) },
          contentAlignment = Alignment.Center,
        ) {
          Text(text = strings["delete"], color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
      }
    }
  }
}
