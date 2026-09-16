package com.solvix.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FilterTag(label: String, onRemove: () -> Unit) {
  val colors = LocalAppColors.current
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(999.dp))
      .background(colors.accent.copy(alpha = 0.12f))
      .border(1.dp, colors.accent.copy(alpha = 0.3f), RoundedCornerShape(999.dp))
      .clickable { onRemove() }
      .padding(horizontal = 10.dp, vertical = 5.dp),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(text = label, fontSize = 11.sp, color = colors.accent, fontWeight = FontWeight.Medium)
      Text(text = "✕", fontSize = 10.sp, color = colors.accent, fontWeight = FontWeight.Bold)
    }
  }
}

@Composable
fun FilterSection(
  title: String,
  icon: String,
  content: @Composable () -> Unit,
) {
  val colors = LocalAppColors.current
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(colors.bg2.copy(alpha = 0.4f))
      .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
      .padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(text = icon, fontSize = 13.sp)
      Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = colors.text,
      )
    }
    content()
  }
}

@Composable
fun SortOptionButton(
  label: String,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  val colors = LocalAppColors.current
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(if (isSelected) colors.accent.copy(alpha = 0.15f) else colors.bg2)
      .border(
        width = 1.dp,
        color = if (isSelected) colors.accent else colors.cardBorder,
        shape = RoundedCornerShape(8.dp),
      )
      .clickable(onClick = onClick)
      .padding(vertical = 8.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) colors.accent else colors.text,
    )
  }
}
