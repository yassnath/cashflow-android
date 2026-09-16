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
import androidx.compose.foundation.layout.width
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
import java.util.Calendar

private data class UpcomingItem(
  val id: String,
  val title: String,
  val amount: Int,
  val rawDate: String,
  val daysRemaining: Int,
  val isLoan: Boolean,
)

@Composable
fun DashboardUpcomingBills(
  loanEntries: List<LoanEntry>,
  dreamEntries: List<DreamEntry>,
  strings: AppStrings,
  onNavigateTo: (Page) -> Unit,
) {
  val colors = LocalAppColors.current
  val theme = LocalThemeName.current
  val isDark = isDarkTheme(theme)

  val warningColor = if (isDark) Color(0xFFFFB74D) else Color(0xFFF57C00)
  val dangerColor = if (isDark) Color(0xFFFF8A80) else Color(0xFFC62828)
  val safeColor = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)

  val nowStartOfDay = remember {
    val cal = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    cal.timeInMillis
  }

  val upcomingItems = remember(loanEntries, dreamEntries) {
    val items = mutableListOf<UpcomingItem>()

    // Tagihan hutang/pinjaman yang belum lunas dan ada tanggal jatuh tempo
    loanEntries.forEach { loan ->
      val remaining = (loan.principal - loan.paid).coerceAtLeast(0)
      if (remaining > 0 && loan.dueDate.isNotBlank()) {
        val dueMillis = parseDate(loan.dueDate) ?: parseDateTimeMillis(loan.dueDate)
        if (dueMillis != null) {
          val days = ((dueMillis - nowStartOfDay) / 86_400_000L).toInt()
          // Tampilkan yang sudah lewat atau dalam 60 hari ke depan
          if (days <= 60) {
            items.add(
              UpcomingItem(
                id = loan.id,
                title = loan.title,
                amount = remaining,
                rawDate = loan.dueDate,
                daysRemaining = days,
                isLoan = true,
              )
            )
          }
        }
      }
    }

    // Target impian yang belum tercapai dan ada deadline
    dreamEntries.forEach { dream ->
      val remaining = (dream.target - dream.current).coerceAtLeast(0)
      if (remaining > 0 && dream.deadline.isNotBlank()) {
        val deadlineMillis = parseDate(dream.deadline) ?: parseDateTimeMillis(dream.deadline)
        if (deadlineMillis != null) {
          val days = ((deadlineMillis - nowStartOfDay) / 86_400_000L).toInt()
          if (days in -15..60) {
            items.add(
              UpcomingItem(
                id = dream.id,
                title = "🎯 ${dream.title}",
                amount = remaining,
                rawDate = dream.deadline,
                daysRemaining = days,
                isLoan = false,
              )
            )
          }
        }
      }
    }

    items.sortedBy { it.daysRemaining }.take(3)
  }

  // Hanya tampil jika ada tagihan atau pengingat
  if (upcomingItems.isEmpty()) return

  AppCard {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SectionTitle(
          icon = themeSectionVisualIcon(theme, "debt", "⏰"),
          title = strings["dashboard_upcoming_reminders"],
          modifier = Modifier.weight(1f, fill = false),
          compact = true,
        )
      }

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        upcomingItems.forEach { item ->
          val (badgeText, badgeColor) = when {
            item.daysRemaining < 0 -> Pair(
              "${strings["dashboard_overdue"]} (${-item.daysRemaining}h)",
              dangerColor,
            )
            item.daysRemaining == 0 -> Pair(
              strings["dashboard_due_today"],
              warningColor,
            )
            item.daysRemaining <= 7 -> Pair(
              "${item.daysRemaining} ${strings["dashboard_days_left"]}",
              warningColor,
            )
            else -> Pair(
              "${item.daysRemaining} ${strings["dashboard_days_left"]}",
              colors.accent,
            )
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(AppDimens.radiusSm))
              .background(colors.bg2.copy(alpha = 0.6f))
              .clickable {
                if (item.isLoan) onNavigateTo(Page.Loans) else onNavigateTo(Page.Dreams)
              }
              .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = item.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = item.rawDate,
                fontSize = 11.sp,
                color = colors.muted,
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
              AnimatedAmountText(
                amount = item.amount,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (item.isLoan) dangerColor else safeColor,
              )
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(999.dp))
                  .background(badgeColor.copy(alpha = 0.12f))
                  .padding(horizontal = 8.dp, vertical = 2.dp),
              ) {
                Text(
                  text = badgeText,
                  color = badgeColor,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          }
        }
      }
    }
  }
}
