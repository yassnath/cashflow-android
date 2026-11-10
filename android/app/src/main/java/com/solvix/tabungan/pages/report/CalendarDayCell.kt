package com.solvix.tabungan.pages.report

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
fun CalendarDayCell(
  dayNum: Int,
  isSelected: Boolean,
  isToday: Boolean,
  hasIncome: Boolean,
  hasExpense: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = LocalAppColors.current

  Box(
    modifier = modifier
      .aspectRatio(1f)
      .clip(RoundedCornerShape(8.dp))
      .background(
        when {
          isSelected -> colors.accent.copy(alpha = 0.22f)
          isToday -> colors.accent2.copy(alpha = 0.12f)
          else -> colors.bg2.copy(alpha = 0.4f)
        }
      )
      .border(
        width = if (isSelected) 1.5.dp else if (isToday) 1.dp else 0.5.dp,
        color = when {
          isSelected -> colors.accent
          isToday -> colors.accent2
          else -> colors.cardBorder.copy(alpha = 0.5f)
        },
        shape = RoundedCornerShape(8.dp),
      )
      .clickable(onClick = onClick)
      .padding(2.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Text(
        text = dayNum.toString(),
        fontSize = 12.sp,
        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
        color = if (isSelected) colors.accent else colors.text,
      )

      // Dot Indikator Pemasukan / Pengeluaran
      Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.height(5.dp),
      ) {
        if (hasIncome) {
          Box(
            modifier = Modifier
              .size(4.dp)
              .clip(CircleShape)
              .background(Color(0xFF2EC4B6))
          )
        }
        if (hasExpense) {
          Box(
            modifier = Modifier
              .size(4.dp)
              .clip(CircleShape)
              .background(Color(0xFFFF4D6D))
          )
        }
      }
    }
  }
}
