package com.solvix.tabungan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FilterSortSection(
  selectedSort: HistorySortOption,
  onSelectSort: (HistorySortOption) -> Unit,
  strings: AppStrings = LocalStrings.current,
) {
  FilterSection(title = strings["filter_sort_by"].ifBlank { "Urutkan Berdasarkan" }, icon = "↕️") {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        SortOptionButton(
          label = strings["filter_sort_newest"].ifBlank { "Terbaru" },
          isSelected = selectedSort == HistorySortOption.Newest,
          modifier = Modifier.weight(1f),
          onClick = { onSelectSort(HistorySortOption.Newest) },
        )
        SortOptionButton(
          label = strings["filter_sort_oldest"].ifBlank { "Terlama" },
          isSelected = selectedSort == HistorySortOption.Oldest,
          modifier = Modifier.weight(1f),
          onClick = { onSelectSort(HistorySortOption.Oldest) },
        )
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        SortOptionButton(
          label = strings["filter_sort_highest"].ifBlank { "Nominal Terbesar" },
          isSelected = selectedSort == HistorySortOption.Highest,
          modifier = Modifier.weight(1f),
          onClick = { onSelectSort(HistorySortOption.Highest) },
        )
        SortOptionButton(
          label = strings["filter_sort_lowest"].ifBlank { "Nominal Terkecil" },
          isSelected = selectedSort == HistorySortOption.Lowest,
          modifier = Modifier.weight(1f),
          onClick = { onSelectSort(HistorySortOption.Lowest) },
        )
      }
    }
  }
}
