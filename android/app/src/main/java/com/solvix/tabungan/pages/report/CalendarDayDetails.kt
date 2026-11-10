package com.solvix.tabungan.pages.report

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppDimens
import com.solvix.tabungan.EntryType
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.MoneyEntry
import com.solvix.tabungan.formatRupiah

@Composable
fun CalendarDayDetails(
  selectedDay: Int,
  monthYearTitle: String,
  selectedDayEntries: List<MoneyEntry>,
) {
  val colors = LocalAppColors.current
  val selDayIncome = selectedDayEntries.filter { it.type == EntryType.Income }.sumOf { it.amount.toLong() }
  val selDayExpense = selectedDayEntries.filter { it.type == EntryType.Expense }.sumOf { it.amount.toLong() }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(AppDimens.radiusSm))
      .background(colors.bg2.copy(alpha = 0.6f))
      .border(1.dp, colors.cardBorder, RoundedCornerShape(AppDimens.radiusSm))
      .padding(12.dp),
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "📅 Tanggal $selectedDay $monthYearTitle",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = colors.text,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          if (selDayIncome > 0L) {
            Text(
              text = "+${formatRupiah(selDayIncome)}",
              fontSize = 11.sp,
              color = Color(0xFF2EC4B6),
              fontWeight = FontWeight.Bold,
            )
          }
          if (selDayExpense > 0L) {
            Text(
              text = "-${formatRupiah(selDayExpense)}",
              fontSize = 11.sp,
              color = Color(0xFFFF4D6D),
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }

      if (selectedDayEntries.isEmpty()) {
        Text(
          text = "Tidak ada transaksi tercatat di tanggal ini.",
          fontSize = 11.sp,
          color = colors.muted,
          modifier = Modifier.padding(vertical = 4.dp),
        )
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          selectedDayEntries.forEach { item ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(colors.card)
                .padding(horizontal = 10.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = item.category,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 12.sp,
                  color = colors.text,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                val detail = listOf(item.channelOrBank, item.note).filter { it.isNotBlank() }.joinToString(" • ")
                if (detail.isNotBlank()) {
                  Text(
                    text = detail,
                    fontSize = 10.sp,
                    color = colors.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
              }
              Text(
                text = "${if (item.type == EntryType.Income) "+" else "-"}${formatRupiah(item.amount.toLong())}",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (item.type == EntryType.Income) Color(0xFF2EC4B6) else Color(0xFFFF4D6D),
              )
            }
          }
        }
      }
    }
  }
}
