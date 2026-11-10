package com.solvix.tabungan.components

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.LocalAppColors

@Composable
fun CategoryChipsRow(
  categories: List<String>,
  selectedCategory: String,
  onSelectCategory: (String) -> Unit,
  onAddCategoryClick: () -> Unit,
  addButtonLabel: String = "+ Kategori",
) {
  val colors = LocalAppColors.current

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
        .clickable(onClick = onAddCategoryClick)
        .padding(horizontal = 10.dp, vertical = 5.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = addButtonLabel,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = colors.accent,
      )
    }

    categories.forEach { cat ->
      val isSelected = cat.equals(selectedCategory, ignoreCase = true)
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(999.dp))
          .background(if (isSelected) colors.accent else colors.bg2)
          .clickable { onSelectCategory(cat) }
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
}
