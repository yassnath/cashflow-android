package com.solvix.tabungan

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoanSummaryCard(
  entries: List<LoanEntry>,
  strings: AppStrings = LocalStrings.current,
) {
  val colors = LocalAppColors.current
  val totalDebtRemaining = entries.filter { !it.isReceivable }.sumOf { remainingLoanBalance(it) }
  val totalReceivableRemaining = entries.filter { it.isReceivable }.sumOf { remainingLoanBalance(it) }

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    // Sisa Hutang Saya
    Box(
      modifier = Modifier
        .weight(1f)
        .clip(RoundedCornerShape(14.dp))
        .background(colors.card)
        .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp))
        .padding(14.dp),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = strings["loan_my_debt"].ifBlank { "🔴 Hutang Saya" }, fontSize = 11.sp, color = colors.muted, fontWeight = FontWeight.SemiBold)
        Text(
          text = formatRupiah(totalDebtRemaining),
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = if (totalDebtRemaining > 0) Color(0xFFEF4444) else colors.accent2,
        )
        Text(
          text = strings["loan_must_pay"].ifBlank { "Wajib dibayar" },
          fontSize = 10.sp,
          color = colors.muted,
        )
      }
    }

    // Sisa Piutang Saya
    Box(
      modifier = Modifier
        .weight(1f)
        .clip(RoundedCornerShape(14.dp))
        .background(colors.card)
        .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp))
        .padding(14.dp),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = strings["loan_my_receivable"].ifBlank { "🟢 Piutang Saya" }, fontSize = 11.sp, color = colors.muted, fontWeight = FontWeight.SemiBold)
        Text(
          text = formatRupiah(totalReceivableRemaining),
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = if (totalReceivableRemaining > 0) Color(0xFF10B981) else colors.text,
        )
        Text(
          text = strings["loan_to_collect"].ifBlank { "Akan ditagih" },
          fontSize = 10.sp,
          color = colors.muted,
        )
      }
    }
  }
}
