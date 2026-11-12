package com.solvix.tabungan

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HistoryTopBar(
  context: Context,
  activeFilterCount: Int,
  onOpenFilter: () -> Unit,
  sortedEntries: List<MoneyEntry>,
  fromDate: String,
  toDate: String,
  searchQuery: String,
  selectedType: EntryType?,
  selectedCategory: String,
  minAmountStr: String,
  maxAmountStr: String,
  defaultRange: Pair<String, String>,
  onResetAll: () -> Unit,
  onClearSearch: () -> Unit,
  onClearType: () -> Unit,
  onClearCategory: () -> Unit,
  onClearMinAmount: () -> Unit,
  onClearMaxAmount: () -> Unit,
  onClearDateRange: () -> Unit,
  onImportCsv: () -> Unit = {},
  isSelectionMode: Boolean = false,
  selectedCount: Int = 0,
  onToggleSelectionMode: () -> Unit = {},
  onSelectAll: () -> Unit = {},
  onDeleteSelected: () -> Unit = {},
  strings: AppStrings = LocalStrings.current,
) {
  val colors = LocalAppColors.current
  var showExportMenu by remember { mutableStateOf(false) }

  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    if (isSelectionMode) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(colors.accent.copy(alpha = 0.12f))
          .border(1.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(colors.bg2)
              .clickable { onToggleSelectionMode() },
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Rounded.Close,
              contentDescription = "Batal",
              tint = colors.text,
              modifier = Modifier.size(18.dp),
            )
          }
          Text(
            text = "$selectedCount dipilih",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = colors.text,
          )
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(colors.bg2)
              .clickable { onSelectAll() }
              .heightIn(min = 44.dp)
              .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = "Pilih Semua",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = colors.accent,
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (selectedCount > 0) colors.danger else colors.danger.copy(alpha = 0.3f))
              .clickable(enabled = selectedCount > 0) { onDeleteSelected() }
              .heightIn(min = 44.dp)
              .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
          ) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = null,
                tint = colors.onDanger,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = "Hapus",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onDanger,
              )
            }
          }
        }
      }
    } else {
      // Header Bar: Title di Kiri, Tombol Aksi (Filter, Ekspor, Checklist) di Kanan
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SectionTitle(
          icon = themePageVisualIcon(LocalThemeName.current, Page.History),
          title = strings["page_history"].ifBlank { "Riwayat" },
          subtitle = strings["section_history_subtitle"].ifBlank { "Lacak seluruh arus transaksi" },
          modifier = Modifier.weight(1f, fill = false),
          compact = true,
        )

        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          // Tombol Checklist Mode Seleksi
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(colors.bg2)
              .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
              .clickable { onToggleSelectionMode() },
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Rounded.Checklist,
              contentDescription = "Pilih Banyak",
              tint = colors.accent,
              modifier = Modifier.size(20.dp),
            )
          }

          // Tombol Icon Filter Pop-up (hanya icon, dengan badge jika ada filter aktif)
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(if (activeFilterCount > 0) colors.accent.copy(alpha = 0.15f) else colors.bg2)
              .border(
                width = 1.dp,
                color = if (activeFilterCount > 0) colors.accent else colors.cardBorder,
                shape = RoundedCornerShape(12.dp),
              )
              .clickable { onOpenFilter() },
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Rounded.FilterList,
              contentDescription = "Filter",
              tint = if (activeFilterCount > 0) colors.accent else colors.text,
              modifier = Modifier.size(20.dp),
            )
            if (activeFilterCount > 0) {
              Box(
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .padding(top = 4.dp, end = 4.dp)
                  .size(16.dp)
                  .clip(CircleShape)
                  .background(colors.accent),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = activeFilterCount.toString(),
                  color = colors.onGradient,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          }

          // Tombol Ekspor Dropdown
          Box {
            Box(
              modifier = Modifier
                .heightIn(min = 44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.bg2)
                .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                .clickable { showExportMenu = true }
                .padding(horizontal = 10.dp),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Rounded.FileDownload,
                contentDescription = "Ekspor",
                tint = colors.accent,
                modifier = Modifier.size(20.dp),
              )
            }

            DropdownMenu(
              expanded = showExportMenu,
              onDismissRequest = { showExportMenu = false },
              modifier = Modifier.background(colors.card),
            ) {
              DropdownMenuItem(
                text = { Text("📄 Ekspor Dokumen PDF", fontSize = 13.sp, color = colors.text) },
                onClick = {
                  showExportMenu = false
                  val uri = HistoryExportHelper.exportToPdf(
                    context = context,
                    entries = sortedEntries,
                    periodText = "$fromDate s/d $toDate",
                  )
                  HistoryExportHelper.shareExportedFile(context, uri, "application/pdf", "CashFlow Laporan PDF")
                },
              )
              DropdownMenuItem(
                text = { Text("📊 Ekspor Excel / CSV", fontSize = 13.sp, color = colors.text) },
                onClick = {
                  showExportMenu = false
                  val uri = HistoryExportHelper.exportToCsv(
                    context = context,
                    entries = sortedEntries,
                    filterTitle = "Riwayat_${fromDate}_${toDate}",
                  )
                  HistoryExportHelper.shareExportedFile(context, uri, "text/csv", "CashFlow Laporan CSV")
                },
              )
              DropdownMenuItem(
                text = { Text("📥 Impor dari File CSV", fontSize = 13.sp, color = colors.text) },
                onClick = {
                  showExportMenu = false
                  onImportCsv()
                },
              )
            }
          }
        }
      }
    }

    // Active Filter Chips Horizontal (jika ada filter yang sedang aktif)
    if (activeFilterCount > 0) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Reset Chip
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(colors.danger.copy(alpha = 0.15f))
            .clickable { onResetAll() }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        ) {
          Text(text = strings["filter_reset_all"].ifBlank { "Reset Semua ✕" }, fontSize = 11.sp, color = colors.danger, fontWeight = FontWeight.Bold)
        }

        if (searchQuery.isNotBlank()) {
          val searchPrefix = strings["search"].ifBlank { "Cari" }
          FilterTag(label = "$searchPrefix: \"$searchQuery\"") { onClearSearch() }
        }
        selectedType?.let { type ->
          val label = when (type) {
            EntryType.Income -> strings["entry_type_income"].ifBlank { "Pemasukan" }
            EntryType.Expense -> strings["entry_type_expense"].ifBlank { "Pengeluaran" }
            EntryType.Transfer -> strings["entry_type_transfer"].ifBlank { "Transfer" }
          }
          FilterTag(label = label) {
            onClearType()
          }
        }
        if (selectedCategory != "Semua") {
          val catPrefix = strings["category"].ifBlank { "Kategori" }
          FilterTag(label = "$catPrefix: $selectedCategory") { onClearCategory() }
        }
        if (minAmountStr.isNotBlank()) {
          FilterTag(label = "Min: ${CurrencyHelper.currentCurrency.symbol} $minAmountStr") { onClearMinAmount() }
        }
        if (maxAmountStr.isNotBlank()) {
          FilterTag(label = "Maks: ${CurrencyHelper.currentCurrency.symbol} $maxAmountStr") { onClearMaxAmount() }
        }
        if (fromDate != defaultRange.first || toDate != defaultRange.second) {
          FilterTag(label = "$fromDate - $toDate") {
            onClearDateRange()
          }
        }
      }
    }
  }
}
