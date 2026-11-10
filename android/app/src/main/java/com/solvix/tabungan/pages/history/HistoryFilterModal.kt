package com.solvix.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun HistoryFilterModal(
  strings: AppStrings,
  availableCategories: List<String>,
  initialSearchQuery: String,
  initialFromDate: String,
  initialToDate: String,
  initialCategory: String,
  initialSort: HistorySortOption,
  initialType: EntryType?,
  initialMinAmount: String,
  initialMaxAmount: String,
  defaultRange: Pair<String, String>,
  onApply: (
    searchQuery: String,
    fromDate: String,
    toDate: String,
    category: String,
    sort: HistorySortOption,
    type: EntryType?,
    minAmount: String,
    maxAmount: String,
  ) -> Unit,
  onReset: () -> Unit,
  onDismiss: () -> Unit,
) {
  val colors = LocalAppColors.current

  var tempSearchQuery by remember { mutableStateOf(initialSearchQuery) }
  var tempFromDate by remember { mutableStateOf(initialFromDate) }
  var tempToDate by remember { mutableStateOf(initialToDate) }
  var tempCategory by remember { mutableStateOf(initialCategory) }
  var tempSort by remember { mutableStateOf(initialSort) }
  var tempType by remember { mutableStateOf(initialType) }
  var tempMinAmount by remember { mutableStateOf(initialMinAmount) }
  var tempMaxAmount by remember { mutableStateOf(initialMaxAmount) }

  val configuration = LocalConfiguration.current
  val maxModalHeight = (configuration.screenHeightDp * 0.88f).dp
  val isSmallScreen = configuration.screenWidthDp < 360

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(if (isSmallScreen) 0.96f else 0.92f)
        .widthIn(max = 520.dp)
        .imePadding()
        .heightIn(max = maxModalHeight)
        .clip(RoundedCornerShape(20.dp))
        .background(colors.card)
        .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
        .padding(18.dp),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // 1. Header Pop-up
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = strings["history_filter_modal_title"].ifBlank { "Filter & Cari Riwayat" },
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = colors.text,
            )
            Text(
              text = strings["history_filter_modal_subtitle"].ifBlank { "Sesuaikan filter pencarian transaksi Anda" },
              fontSize = 11.sp,
              color = colors.muted,
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(imageVector = Icons.Rounded.Close, contentDescription = strings["close"], tint = colors.muted)
          }
        }

        // Garis Pembatas Halus Atas
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colors.cardBorder),
        )

        // 2. Scrollable Body
        Column(
          modifier = Modifier
            .weight(1f, fill = false)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          // Seksi 1: Kata Kunci Pencarian
          FilterSection(title = strings["history_filter_text_search"].ifBlank { "Pencarian Teks" }, icon = Icons.Rounded.Search) {
            AppTextField(
              label = "",
              value = tempSearchQuery,
              onValueChange = { tempSearchQuery = it },
              placeholder = strings["history_filter_text_placeholder"].ifBlank { "Cari catatan, kategori, metode, atau bank..." },
            )
          }

          // Seksi 2: Rentang Tanggal
          FilterDateSection(
            fromDate = tempFromDate,
            toDate = tempToDate,
            onFromDateChange = { tempFromDate = it },
            onToDateChange = { tempToDate = it },
            strings = strings,
          )

          // Seksi 3: Kategori Transaksi
          FilterCategorySection(
            availableCategories = availableCategories,
            selectedCategory = tempCategory,
            onSelectCategory = { tempCategory = it },
            strings = strings,
          )

          // Seksi 4: Pengurutan (Sorting)
          FilterSortSection(
            selectedSort = tempSort,
            onSelectSort = { tempSort = it },
            strings = strings,
          )

          // Seksi 5: Tipe & Rentang Nominal
          FilterTypeAmountSection(
            selectedType = tempType,
            onSelectType = { tempType = it },
            minAmount = tempMinAmount,
            onMinAmountChange = { tempMinAmount = it },
            maxAmount = tempMaxAmount,
            onMaxAmountChange = { tempMaxAmount = it },
            strings = strings,
          )
        }

        // Garis Pembatas Halus Bawah
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colors.cardBorder),
        )

        // 3. Tombol Aksi Bawah: Reset & Cari / Terapkan
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(colors.bg2)
              .border(1.dp, colors.cardBorder, RoundedCornerShape(10.dp))
              .clickable {
                tempSearchQuery = ""
                tempFromDate = defaultRange.first
                tempToDate = defaultRange.second
                tempCategory = "Semua"
                tempSort = HistorySortOption.Newest
                tempType = null
                tempMinAmount = ""
                tempMaxAmount = ""
                onReset()
                onDismiss()
              },
            contentAlignment = Alignment.Center,
          ) {
            Text(text = strings["filter_reset"].ifBlank { "Reset" }, color = colors.muted, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }

          Box(
            modifier = Modifier
              .weight(1.8f)
              .height(44.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(Brush.linearGradient(listOf(colors.accent, colors.accent2)))
              .clickable {
                onApply(
                  tempSearchQuery,
                  tempFromDate,
                  tempToDate,
                  tempCategory,
                  tempSort,
                  tempType,
                  tempMinAmount,
                  tempMaxAmount,
                )
                onDismiss()
              },
            contentAlignment = Alignment.Center,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(imageVector = Icons.Rounded.Search, contentDescription = strings["search"], tint = Color.White, modifier = Modifier.size(16.dp))
              Text(text = strings["filter_apply"].ifBlank { "Cari / Terapkan" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          }
        }
      }
    }
  }
}
