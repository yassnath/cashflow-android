package com.solvix.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FilterCategorySection(
  availableCategories: List<String>,
  selectedCategory: String,
  onSelectCategory: (String) -> Unit,
  strings: AppStrings = LocalStrings.current,
) {
  val colors = LocalAppColors.current

  FilterSection(title = strings["filter_category_section"].ifBlank { "Kategori Transaksi" }, icon = Icons.Rounded.Category) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      availableCategories.forEach { cat ->
        val isSelected = cat == selectedCategory
        val displayLabel = if (cat == "Semua") strings["filter_preset_all"].ifBlank { "Semua" } else cat
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (isSelected) colors.accent else colors.bg2)
            .border(
              width = 1.dp,
              color = if (isSelected) colors.accent else colors.cardBorder,
              shape = RoundedCornerShape(999.dp),
            )
            .clickable { onSelectCategory(cat) }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
          Text(
            text = displayLabel,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else colors.text,
          )
        }
      }
    }
  }
}
