package com.solvix.tabungan

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HistoryPage(
  entries: List<MoneyEntry>,
  strings: AppStrings,
  onEdit: (MoneyEntry) -> Unit,
  onDelete: (MoneyEntry) -> Unit,
  onImportEntries: (List<MoneyEntry>) -> Unit = {},
  onDeleteBatch: (List<MoneyEntry>) -> Unit = { list -> list.forEach(onDelete) },
) {
  val context = LocalContext.current
  val colors = LocalAppColors.current
  val isDark = isDarkTheme(LocalThemeName.current)
  val incomeColor = if (isDark) Color(0xFF7BE27A) else Color(0xFF2E7D32)
  val expenseColor = if (isDark) Color(0xFFFF8A80) else Color(0xFFC62828)

  var isSelectionMode by rememberSaveable { mutableStateOf(false) }
  val selectedIds = remember { mutableStateListOf<String>() }

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
  var selectedType by rememberSaveable { mutableStateOf<EntryType?>(null) }
  var selectedCategory by rememberSaveable { mutableStateOf("Semua") }
  var minAmountStr by rememberSaveable { mutableStateOf("") }
  var maxAmountStr by rememberSaveable { mutableStateOf("") }
  var selectedSort by rememberSaveable { mutableStateOf(HistorySortOption.Newest) }

  var showFilterModal by rememberSaveable { mutableStateOf(false) }
  var previewReceiptUri by rememberSaveable { mutableStateOf<String?>(null) }
  var pendingImportResult by remember { mutableStateOf<HistoryImportHelper.ImportResult?>(null) }

  val csvPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
  ) { uri ->
    if (uri != null) {
      val result = HistoryImportHelper.parseCsvFromUri(context, uri)
      if (result.successEntries.isNotEmpty()) {
        pendingImportResult = result
      }
    }
  }

  val availableCategories = remember(entries) {
    listOf("Semua") + entries.map { it.category }.distinct().filter { it.isNotBlank() }
  }

  val activeFilterCount = remember(selectedType, selectedCategory, minAmountStr, maxAmountStr, fromDate, toDate, selectedSort, searchQuery) {
    var count = 0
    if (searchQuery.isNotBlank()) count++
    if (selectedType != null) count++
    if (selectedCategory != "Semua") count++
    if (minAmountStr.isNotBlank()) count++
    if (maxAmountStr.isNotBlank()) count++
    if (fromDate != defaultRange.first || toDate != defaultRange.second) count++
    if (selectedSort != HistorySortOption.Newest) count++
    count
  }

  val finalFiltered = entries.filter { entry ->
    val date = parseDate(entry.date) ?: return@filter false
    val from = parseDate(fromDate)
    val to = parseDate(toDate)
    val matchesDate = (from == null || date >= from) && (to == null || date <= to)
    val matchesType = selectedType == null || entry.type == selectedType
    val matchesCategory = selectedCategory == "Semua" || entry.category.equals(selectedCategory, ignoreCase = true)
    val minAmount = minAmountStr.toIntOrNull()
    val maxAmount = maxAmountStr.toIntOrNull()
    val matchesAmount = (minAmount == null || entry.amount >= minAmount) &&
      (maxAmount == null || entry.amount <= maxAmount)
    val matchesSearch = searchQuery.isBlank() ||
      entry.category.contains(searchQuery, ignoreCase = true) ||
      entry.note.contains(searchQuery, ignoreCase = true) ||
      entry.sourceOrMethod.contains(searchQuery, ignoreCase = true) ||
      entry.channelOrBank.contains(searchQuery, ignoreCase = true) ||
      entry.amount.toString().contains(searchQuery)

    matchesDate && matchesType && matchesCategory && matchesAmount && matchesSearch
  }

  val sorted = remember(finalFiltered, selectedSort) {
    when (selectedSort) {
      HistorySortOption.Newest -> finalFiltered.sortedByDescending { entry ->
        parseDateTimeMillis(entry.date) ?: parseCreatedAtMillis(entry.createdAt) ?: (parseDate(entry.date) ?: 0L)
      }
      HistorySortOption.Oldest -> finalFiltered.sortedBy { entry ->
        parseDateTimeMillis(entry.date) ?: parseCreatedAtMillis(entry.createdAt) ?: (parseDate(entry.date) ?: 0L)
      }
      HistorySortOption.Highest -> finalFiltered.sortedByDescending { it.amount }
      HistorySortOption.Lowest -> finalFiltered.sortedBy { it.amount }
    }
  }

  val incomeTotal = finalFiltered.filter { it.type == EntryType.Income }.sumOf { it.amount.toLong() }
  val expenseTotal = finalFiltered.filter { it.type == EntryType.Expense }.sumOf { it.amount.toLong() }
  val netBalance = incomeTotal - expenseTotal

  val groupedEntries = remember(sorted) {
    val todayStart = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val yesterdayStart = todayStart - 86_400_000L
    val idLocale = Locale.Builder().setLanguage("id").setRegion("ID").build()
    val fmt = SimpleDateFormat("EEEE, d MMMM yyyy", idLocale)

    sorted.groupBy { entry ->
      val dateMillis = parseDate(entry.date)
      if (dateMillis == null) {
        entry.date
      } else if (dateMillis >= todayStart && dateMillis < todayStart + 86_400_000L) {
        "Hari Ini"
      } else if (dateMillis >= yesterdayStart && dateMillis < todayStart) {
        "Kemarin"
      } else {
        fmt.format(Date(dateMillis))
      }
    }
  }

  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    // Header Ramping: Judul Riwayat di Kiri, Tombol Icon Filter & Ekspor di Kanan, dan Tag Filter Aktif
    HistoryTopBar(
      context = context,
      activeFilterCount = activeFilterCount,
      onOpenFilter = { showFilterModal = true },
      sortedEntries = sorted,
      fromDate = fromDate,
      toDate = toDate,
      searchQuery = searchQuery,
      selectedType = selectedType,
      selectedCategory = selectedCategory,
      minAmountStr = minAmountStr,
      maxAmountStr = maxAmountStr,
      defaultRange = defaultRange,
      onResetAll = {
        searchQuery = ""
        selectedType = null
        selectedCategory = "Semua"
        minAmountStr = ""
        maxAmountStr = ""
        fromDate = defaultRange.first
        toDate = defaultRange.second
        selectedSort = HistorySortOption.Newest
      },
      onClearSearch = { searchQuery = "" },
      onClearType = { selectedType = null },
      onClearCategory = { selectedCategory = "Semua" },
      onClearMinAmount = { minAmountStr = "" },
      onClearMaxAmount = { maxAmountStr = "" },
      onClearDateRange = {
        fromDate = defaultRange.first
        toDate = defaultRange.second
      },
      onImportCsv = { csvPickerLauncher.launch("text/*") },
      isSelectionMode = isSelectionMode,
      selectedCount = selectedIds.size,
      onToggleSelectionMode = {
        isSelectionMode = !isSelectionMode
        if (!isSelectionMode) selectedIds.clear()
      },
      onSelectAll = {
        if (selectedIds.size == sorted.size) {
          selectedIds.clear()
        } else {
          selectedIds.clear()
          selectedIds.addAll(sorted.map { it.id })
        }
      },
      onDeleteSelected = {
        val toDelete = sorted.filter { selectedIds.contains(it.id) }
        onDeleteBatch(toDelete)
        selectedIds.clear()
        isSelectionMode = false
      },
    )

    // Ringkasan Periode Hasil Filter (Ergonomis, Bersih & Ringkas)
    HistorySummaryCard(
      totalCount = sorted.size,
      incomeTotal = incomeTotal,
      expenseTotal = expenseTotal,
      netBalance = netBalance,
      incomeColor = incomeColor,
      expenseColor = expenseColor,
    )

    // Daftar Transaksi Terkelompok Tanggal
    if (sorted.isEmpty()) {
      AppCard {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
          contentAlignment = Alignment.Center,
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🔍", fontSize = 28.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = strings["history_empty_search"].ifBlank { "Tidak ada transaksi yang cocok" }, color = colors.text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = strings["history_empty_search_desc"].ifBlank { "Coba ubah kriteria filter pada tombol di atas" }, color = colors.muted, fontSize = 12.sp)
          }
        }
      }
    } else {
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        groupedEntries.forEach { (dateHeader, dayEntries) ->
          val dayIncome = dayEntries.filter { it.type == EntryType.Income }.sumOf { it.amount.toLong() }
          val dayExpense = dayEntries.filter { it.type == EntryType.Expense }.sumOf { it.amount.toLong() }

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Tanggal & Subtotal Harian
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(text = dateHeader, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.text)
              Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                if (dayIncome > 0) {
                  Text(text = "+${formatRupiah(dayIncome)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = incomeColor)
                }
                if (dayExpense > 0) {
                  Text(text = "-${formatRupiah(dayExpense)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = expenseColor)
                }
              }
            }

            // Kartu Transaksi di hari tersebut
            dayEntries.forEach { entry ->
              val isSelected = selectedIds.contains(entry.id)
              EntryCard(
                entry = entry,
                strings = strings,
                onEdit = onEdit,
                onDelete = onDelete,
                onPreviewReceipt = { previewReceiptUri = it },
                isSelectionMode = isSelectionMode,
                isSelected = isSelected,
                onToggleSelect = {
                  if (isSelected) {
                    selectedIds.remove(entry.id)
                  } else {
                    selectedIds.add(entry.id)
                  }
                },
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }

  // Modal Filter Khusus: Menampung From-To, Pencarian Teks, Kategori, Urutkan, dll.
  if (showFilterModal) {
    HistoryFilterModal(
      strings = strings,
      availableCategories = availableCategories,
      initialSearchQuery = searchQuery,
      initialFromDate = fromDate,
      initialToDate = toDate,
      initialCategory = selectedCategory,
      initialSort = selectedSort,
      initialType = selectedType,
      initialMinAmount = minAmountStr,
      initialMaxAmount = maxAmountStr,
      defaultRange = defaultRange,
      onApply = { newSearch, newFrom, newTo, newCat, newSort, newType, newMin, newMax ->
        searchQuery = newSearch
        fromDate = newFrom
        toDate = newTo
        selectedCategory = newCat
        selectedSort = newSort
        selectedType = newType
        minAmountStr = newMin
        maxAmountStr = newMax
      },
      onReset = {
        searchQuery = ""
        selectedType = null
        selectedCategory = "Semua"
        minAmountStr = ""
        maxAmountStr = ""
        fromDate = defaultRange.first
        toDate = defaultRange.second
        selectedSort = HistorySortOption.Newest
      },
      onDismiss = { showFilterModal = false },
    )
  }

  // Dialog Pratinjau Foto Bukti Transaksi
  previewReceiptUri?.let { uriStr ->
    ReceiptPreviewModal(
      receiptUri = uriStr,
      onDismiss = { previewReceiptUri = null },
    )
  }

  // Dialog Konfirmasi Impor CSV
  pendingImportResult?.let { result ->
    HistoryImportModal(
      parsedEntries = result.successEntries,
      failedCount = result.failedRowCount,
      onConfirm = {
        onImportEntries(result.successEntries)
        pendingImportResult = null
      },
      onDismiss = { pendingImportResult = null },
    )
  }
}
