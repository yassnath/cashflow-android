package com.solvix.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EntryCard(
  entry: MoneyEntry,
  strings: AppStrings,
  onEdit: (MoneyEntry) -> Unit,
  onDelete: (MoneyEntry) -> Unit,
  onPreviewReceipt: (String) -> Unit = {},
) {
  val colors = LocalAppColors.current
  val isIncome = entry.type == EntryType.Income
  val isTransfer = entry.type == EntryType.Transfer
  val icon = if (isTransfer) "🔁" else if (isIncome) "⬆️" else "⬇️"
  val amountPrefix = if (isTransfer) "⇄ " else if (isIncome) "+ " else "- "
  val isDark = isDarkTheme(LocalThemeName.current)
  val amountColor = if (isTransfer) {
    if (isDark) Color(0xFF80D8FF) else Color(0xFF0288D1)
  } else if (isIncome) {
    if (isDark) Color(0xFF7BE27A) else Color(0xFF2E7D32)
  } else {
    if (isDark) Color(0xFFFF8A80) else Color(0xFFC62828)
  }
  val badgeBg = if (isTransfer) {
    Color(0xFF0288D1).copy(alpha = 0.2f)
  } else if (isIncome) {
    colors.accent.copy(alpha = 0.2f)
  } else {
    colors.danger.copy(alpha = 0.2f)
  }

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

      // Lampiran Struk Bukti (Jika Ada)
      if (entry.receiptUri.isNotBlank()) {
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.accent.copy(alpha = 0.12f))
            .clickable { onPreviewReceipt(entry.receiptUri) }
            .padding(horizontal = 10.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(text = "📎", fontSize = 11.sp)
          Text(
            text = "Lihat Foto Bukti Transfer / Struk",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = colors.accent,
          )
        }
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
