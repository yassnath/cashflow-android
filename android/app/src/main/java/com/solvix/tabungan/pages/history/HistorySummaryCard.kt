package com.solvix.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

@Composable
fun HistorySummaryCard(
  totalCount: Int,
  incomeTotal: Long,
  expenseTotal: Long,
  netBalance: Long,
  incomeColor: Color,
  expenseColor: Color,
  strings: AppStrings = LocalStrings.current,
) {
  val colors = LocalAppColors.current

  AppCard {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column {
        Text(
          text = strings["history_filter_summary"].ifBlank { "Ringkasan Hasil Filter" },
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = colors.text,
        )
        Text(
          text = "$totalCount " + (strings["history_transactions_found"].ifBlank { "transaksi ditemukan" }),
          fontSize = 11.sp,
          color = colors.muted,
        )
      }
      Text(
        text = formatRupiah(netBalance),
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = if (netBalance >= 0) incomeColor else expenseColor,
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(10.dp))
          .background(incomeColor.copy(alpha = 0.1f))
          .padding(8.dp),
      ) {
        Column {
          Text(text = strings["entry_type_income"].ifBlank { "Pemasukan" }, fontSize = 11.sp, color = colors.muted)
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "+${formatRupiah(incomeTotal)}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = incomeColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(10.dp))
          .background(expenseColor.copy(alpha = 0.1f))
          .padding(8.dp),
      ) {
        Column {
          Text(text = strings["entry_type_expense"].ifBlank { "Pengeluaran" }, fontSize = 11.sp, color = colors.muted)
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "-${formatRupiah(expenseTotal)}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = expenseColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
  }
}
