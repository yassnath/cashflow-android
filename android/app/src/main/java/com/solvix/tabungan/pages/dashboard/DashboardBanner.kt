package com.solvix.tabungan

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun DashboardBanner(
  displayName: String,
  todayFormatted: String,
  netBalance: Int,
  totalIncome: Int,
  totalExpense: Int,
  strings: AppStrings,
  locale: Locale,
) {
  val colors = LocalAppColors.current
  val theme = LocalThemeName.current
  val isDark = isDarkTheme(theme)

  val incomeColor = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
  val expenseColor = if (isDark) Color(0xFFFF8A80) else Color(0xFFC62828)

  val animatedBalance = remember { Animatable(0f) }
  LaunchedEffect(netBalance) {
    animatedBalance.animateTo(
      targetValue = netBalance.toFloat(),
      animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
    )
  }

  AppCard {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
      // User Greeting & Date
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "${strings["dashboard_greeting"]}, $displayName! 👋",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = colors.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = todayFormatted,
            fontSize = 12.sp,
            color = colors.muted,
            modifier = Modifier.padding(top = 2.dp),
          )
        }

        // Health Status Badge
        val isHealthy = netBalance >= 0
        val badgeBg = if (isHealthy) incomeColor.copy(alpha = 0.15f) else expenseColor.copy(alpha = 0.15f)
        val badgeColor = if (isHealthy) incomeColor else expenseColor
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(badgeBg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        ) {
          Text(
            text = if (isHealthy) "✓ " + strings["dashboard_net_positive"] else "! " + strings["dashboard_net_negative"],
            color = badgeColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
          )
        }
      }

      // Net Balance Visual Card
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(AppDimens.radiusMd))
          .background(
            Brush.linearGradient(
              colors = listOf(
                colors.accent.copy(alpha = 0.22f),
                colors.accent2.copy(alpha = 0.12f),
              )
            )
          )
          .border(
            width = 1.dp,
            color = colors.accent.copy(alpha = 0.35f),
            shape = RoundedCornerShape(AppDimens.radiusMd),
          )
          .padding(16.dp),
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = strings["dashboard_balance"].uppercase(locale),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = colors.muted,
            letterSpacing = 1.sp,
          )
          val currentAnimatedValue = animatedBalance.value.roundToInt()
          val formattedBalance = if (currentAnimatedValue < 0) {
            "- " + formatRupiah(abs(currentAnimatedValue))
          } else {
            formatRupiah(currentAnimatedValue)
          }
          Text(
            text = formattedBalance,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.text,
          )

          Spacer(modifier = Modifier.height(4.dp))

          // Income & Expense Quick Summary Pill
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            // Income mini-card
            Row(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.card.copy(alpha = 0.85f))
                .padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(text = "↓", fontSize = 16.sp, color = incomeColor, fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.width(6.dp))
              Column {
                Text(
                  text = strings["dashboard_income_short"],
                  fontSize = 10.sp,
                  color = colors.muted,
                )
                Text(
                  text = formatRupiah(totalIncome),
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = incomeColor,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }

            // Expense mini-card
            Row(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.card.copy(alpha = 0.85f))
                .padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(text = "↑", fontSize = 16.sp, color = expenseColor, fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.width(6.dp))
              Column {
                Text(
                  text = strings["dashboard_expense_short"],
                  fontSize = 10.sp,
                  color = colors.muted,
                )
                Text(
                  text = formatRupiah(totalExpense),
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
    }
  }
}
