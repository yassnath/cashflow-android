package com.solvix.tabungan

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardRecentTransactions(
  incomeEntries: List<MoneyEntry>,
  expenseEntries: List<MoneyEntry>,
  strings: AppStrings,
  onNavigateTo: (Page) -> Unit,
  transferEntries: List<MoneyEntry> = emptyList(),
) {
  val colors = LocalAppColors.current
  val theme = LocalThemeName.current
  val isDark = isDarkTheme(theme)

  val incomeColor = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
  val expenseColor = if (isDark) Color(0xFFFF8A80) else Color(0xFFC62828)
  val transferColor = if (isDark) Color(0xFF80D8FF) else Color(0xFF0288D1)

  val recentTransactions = remember(incomeEntries.size, expenseEntries.size, transferEntries.size) {
    (incomeEntries + expenseEntries + transferEntries)
      .sortedByDescending { entry ->
        parseDateTimeMillis(entry.date) ?: parseDate(entry.date) ?: 0L
      }
      .take(4)
  }

  AppCard {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SectionTitle(
          icon = themeSectionVisualIcon(theme, "history", "🕒"),
          title = strings["dashboard_recent_transactions"],
          modifier = Modifier.weight(1f, fill = false),
          compact = true,
        )
        Spacer(modifier = Modifier.width(6.dp))
        ChipButton(
          text = strings["dashboard_see_all"],
          onClick = { onNavigateTo(Page.History) },
        )
      }

      if (recentTransactions.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(text = strings["dashboard_no_data"], color = colors.muted, fontSize = 13.sp)
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          recentTransactions.forEach { entry ->
            val isIncome = entry.type == EntryType.Income
            val isTransfer = entry.type == EntryType.Transfer
            val itemColor = if (isTransfer) transferColor else if (isIncome) incomeColor else expenseColor
            val badgeBg = itemColor.copy(alpha = 0.12f)
            val iconSymbol = if (isTransfer) "⇄" else if (isIncome) "↓" else "↑"

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppDimens.radiusSm))
                .background(colors.bg2.copy(alpha = 0.6f))
                .clickable { onNavigateTo(Page.History) }
                .padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              // Icon Badge
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(badgeBg)
                  .border(1.dp, itemColor.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = iconSymbol,
                  color = itemColor,
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 15.sp,
                )
              }

              Spacer(modifier = Modifier.width(10.dp))

              // Title & Category / Date
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = entry.category.ifBlank {
                    if (isTransfer) "Transfer" else if (isIncome) strings["page_income"] else strings["page_expense"]
                  },
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 13.sp,
                  color = colors.text,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                val subText = if (entry.note.isNotBlank()) {
                  "${entry.date} • ${entry.note}"
                } else if (entry.sourceOrMethod.isNotBlank()) {
                  "${entry.date} • ${entry.sourceOrMethod}"
                } else {
                  entry.date
                }
                Text(
                  text = subText,
                  fontSize = 11.sp,
                  color = colors.muted,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }

              Spacer(modifier = Modifier.width(8.dp))

              // Animated Amount
              AnimatedAmountText(
                amount = entry.amount,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = itemColor,
                showPlusSign = isIncome,
                showMinusSign = !isIncome && !isTransfer,
              )
            }
          }
        }
      }
    }
  }
}
