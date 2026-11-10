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
fun LoanEntryCard(
  entry: LoanEntry,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  strings: AppStrings,
) {
  val colors = LocalAppColors.current
  val remaining = remainingLoanBalance(entry)
  val payoffMonths = simulateLoanPayoffMonths(entry)
  val balanceAfterYear = simulateLoanBalanceAfterYear(entry)
  val dueDateLabel = entry.dueDate.ifBlank { "-" }
  val noteText = entry.note.ifBlank { "-" }

  val isReceivable = entry.isReceivable
  val badgeText = if (isReceivable) "Piutang (Dipinjamkan)" else "Hutang (Pinjaman)"
  val badgeBg = if (isReceivable) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f)
  val badgeColor = if (isReceivable) Color(0xFF10B981) else Color(0xFFEF4444)
  val remainingColor = if (remaining > 0) badgeColor else colors.accent2

  AppCard(shape = RoundedCornerShape(AppDimens.radiusMd)) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(text = entry.title, fontWeight = FontWeight.Bold, color = colors.text, fontSize = 14.sp)
          Text(
            text = loanTypeLabel(entry.type, strings),
            color = colors.muted,
            fontSize = 11.sp,
          )
        }
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(badgeBg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(badgeColor),
            )
            Text(text = badgeText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = badgeColor)
          }
        }
      }

      Text(text = "${strings["loan_principal"]}: ${formatRupiah(entry.principal)}", color = colors.muted, fontSize = 12.sp)
      Text(text = "${strings["loan_paid"]}: ${formatRupiah(entry.paid)}", color = colors.muted, fontSize = 12.sp)
      Text(
        text = "${strings["loan_remaining"]}: ${formatRupiah(remaining)}",
        color = remainingColor,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
      )
      if (!isReceivable && entry.annualInterestRate > 0.0) {
        Text(
          text = "${strings["loan_interest_simulation"]}: ${entry.annualInterestRate}%",
          color = colors.muted,
          fontSize = 12.sp,
        )
      }
      Text(
        text = "${strings["loan_payoff_estimate"]}: ${loanPayoffEstimateLabel(payoffMonths, strings)}",
        color = colors.muted,
        fontSize = 12.sp,
      )
      if (!isReceivable) {
        Text(
          text = "${strings["loan_balance_after_year"]}: ${formatRupiah(balanceAfterYear)}",
          color = colors.muted,
          fontSize = 12.sp,
        )
      }
      Text(text = "${strings["loan_due_date"]}: $dueDateLabel", color = colors.muted, fontSize = 12.sp)
      Text(text = "${strings["note_label"]}: $noteText", color = colors.muted, fontSize = 12.sp)

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Box(
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.linearGradient(listOf(colors.accent, colors.accent2)))
            .clickable { onEdit() },
          contentAlignment = Alignment.Center,
        ) {
          Text(text = strings["edit"], color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
        Box(
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFD32F2F))
            .clickable { onDelete() },
          contentAlignment = Alignment.Center,
        ) {
          Text(text = strings["delete"], color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
      }
    }
  }
}
