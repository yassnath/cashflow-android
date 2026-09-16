package com.solvix.tabungan

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import kotlin.math.roundToInt

@Composable
fun DashboardFinancialHealthCard(
  totalIncome: Int,
  totalExpense: Int,
  thisMonthExpenses: List<MoneyEntry>,
  strings: AppStrings,
) {
  val colors = LocalAppColors.current
  val theme = LocalThemeName.current
  val isDark = isDarkTheme(theme)

  val incomeColor = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
  val warningColor = if (isDark) Color(0xFFFFB74D) else Color(0xFFF57C00)
  val expenseColor = if (isDark) Color(0xFFFF8A80) else Color(0xFFC62828)

  // Hitung Rasio Tabungan
  val rawSavingsRate = if (totalIncome > 0) {
    (((totalIncome - totalExpense).toFloat() / totalIncome) * 100).roundToInt()
  } else if (totalExpense > 0) {
    -100
  } else {
    0
  }
  val animatedSavingsRate = rememberAnimatedAmount(rawSavingsRate)

  // Hitung Rata-rata Pengeluaran Harian Bulan Ini
  val dayOfMonth = remember { Calendar.getInstance().get(Calendar.DAY_OF_MONTH).coerceAtLeast(1) }
  val thisMonthTotal = remember(thisMonthExpenses.size, thisMonthExpenses.sumOf { it.amount }) {
    thisMonthExpenses.sumOf { it.amount }
  }
  val dailyAverage = thisMonthTotal / dayOfMonth

  // Tentukan Level & Tips Kesehatan Finansial
  val (statusLabel, statusColor, statusTip, meterRatio) = when {
    rawSavingsRate >= 50 -> Quadruple(
      strings["dashboard_health_excellent"],
      incomeColor,
      strings["dashboard_health_tip_excellent"],
      0.95f,
    )
    rawSavingsRate >= 20 -> Quadruple(
      strings["dashboard_health_good"],
      colors.accent,
      strings["dashboard_health_tip_good"],
      0.65f,
    )
    rawSavingsRate >= 0 -> Quadruple(
      strings["dashboard_health_warning"],
      warningColor,
      strings["dashboard_health_tip_warning"],
      0.35f,
    )
    else -> Quadruple(
      strings["dashboard_health_critical"],
      expenseColor,
      strings["dashboard_health_tip_critical"],
      0.12f,
    )
  }

  val animatedMeter by animateFloatAsState(
    targetValue = meterRatio,
    animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
    label = "health_meter",
  )

  AppCard {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SectionTitle(
          icon = themeSectionVisualIcon(theme, "health", "🩺"),
          title = strings["dashboard_financial_health"],
          modifier = Modifier.weight(1f, fill = false),
          compact = true,
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Status pill
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(statusColor.copy(alpha = 0.15f))
            .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = statusLabel,
            color = statusColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
          )
        }
      }

      // Visual Score Progress Bar
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = strings["dashboard_savings_rate"],
            fontSize = 12.sp,
            color = colors.muted,
          )
          Text(
            text = "$animatedSavingsRate%",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = statusColor,
          )
        }

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(9.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(colors.bg2),
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth(animatedMeter)
              .height(9.dp)
              .clip(RoundedCornerShape(999.dp))
              .background(
                Brush.horizontalGradient(
                  listOf(statusColor.copy(alpha = 0.65f), statusColor)
                )
              ),
          )
        }
      }

      // 2 Mini Stats: Savings Rate & Daily Average Spending
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        // Daily Average Card
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(AppDimens.radiusSm))
            .background(colors.bg2.copy(alpha = 0.6f))
            .padding(10.dp),
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = strings["dashboard_daily_average"],
              fontSize = 10.sp,
              color = colors.muted,
              maxLines = 1,
            )
            AnimatedAmountText(
              amount = dailyAverage,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = colors.text,
            )
          }
        }

        // Net Flow Card
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(AppDimens.radiusSm))
            .background(colors.bg2.copy(alpha = 0.6f))
            .padding(10.dp),
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = strings["dashboard_this_month"],
              fontSize = 10.sp,
              color = colors.muted,
              maxLines = 1,
            )
            AnimatedAmountText(
              amount = totalIncome - totalExpense,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = if (totalIncome >= totalExpense) incomeColor else expenseColor,
            )
          }
        }
      }

      // Insight / Tip box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(colors.accent.copy(alpha = 0.08f))
          .border(1.dp, colors.accent.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
          .padding(horizontal = 12.dp, vertical = 8.dp),
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = "💡", fontSize = 14.sp)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = statusTip,
            fontSize = 11.sp,
            color = colors.text,
            lineHeight = 16.sp,
          )
        }
      }
    }
  }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
