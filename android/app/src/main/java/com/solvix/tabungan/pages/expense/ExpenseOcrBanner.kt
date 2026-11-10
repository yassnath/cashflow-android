package com.solvix.tabungan.pages.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.solvix.tabungan.AppDimens
import com.solvix.tabungan.LocalAppColors

@Composable
fun ExpenseOcrBanner(
  isScanning: Boolean,
  title: String,
  onClick: () -> Unit,
) {
  val colors = LocalAppColors.current

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(AppDimens.radiusSm))
      .background(colors.accent.copy(alpha = 0.12f))
      .border(1.dp, colors.accent.copy(alpha = 0.3f), RoundedCornerShape(AppDimens.radiusSm))
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 10.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.weight(1f),
      ) {
        Text(text = "📸", fontSize = 18.sp)
        Column {
          Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = colors.text,
          )
          Text(
            text = if (isScanning) "Sedang memindai teks struk..." else "Pilih foto struk untuk otomatis mengisi nominal & tanggal",
            fontSize = 11.sp,
            color = colors.muted,
          )
        }
      }
      Spacer(modifier = Modifier.padding(horizontal = 2.dp))
      Text(
        text = if (isScanning) "⏳ ..." else "Scan ➔",
        color = colors.accent,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
      )
    }
  }
}
