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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardLoanSummary(
  loanEntries: List<LoanEntry>,
  strings: AppStrings,
  onNavigateTo: (Page) -> Unit,
) {
  val colors = LocalAppColors.current
  val theme = LocalThemeName.current
  val isDark = isDarkTheme(theme)

  val incomeColor = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
  val expenseColor = if (isDark) Color(0xFFFF8A80) else Color(0xFFC62828)

  val totalRemainingDebt = loanEntries.sumOf { remainingLoanBalance(it) }
  val activeLoansCount = loanEntries.count { remainingLoanBalance(it) > 0 }

  AppCard {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SectionTitle(
          icon = themePageVisualIcon(theme, Page.Loans),
          title = strings["dashboard_active_loans"],
        )
        ChipButton(
          text = strings["dashboard_see_all"],
          onClick = { onNavigateTo(Page.Loans) },
        )
      }

      if (loanEntries.isEmpty() || activeLoansCount == 0) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.radiusSm))
            .background(incomeColor.copy(alpha = 0.1f))
            .padding(14.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "✨ " + strings["dashboard_no_loans"],
            color = incomeColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
          )
        }
      } else {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.radiusSm))
            .background(expenseColor.copy(alpha = 0.08f))
            .border(
              width = 1.dp,
              color = expenseColor.copy(alpha = 0.25f),
              shape = RoundedCornerShape(AppDimens.radiusSm),
            )
            .padding(14.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                text = "$activeLoansCount ${strings["dashboard_active_loans"]}",
                fontSize = 12.sp,
                color = colors.muted,
              )
              Text(
                text = formatRupiah(totalRemainingDebt),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = expenseColor,
              )
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(colors.card)
                .clickable { onNavigateTo(Page.Loans) }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
              Text(
                text = "→",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = colors.text,
              )
            }
          }
        }
      }
    }
  }
}
