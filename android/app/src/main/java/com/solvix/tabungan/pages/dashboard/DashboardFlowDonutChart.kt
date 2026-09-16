package com.solvix.tabungan

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun DashboardFlowDonutChart(
  totalIncome: Int,
  totalExpense: Int,
  strings: AppStrings,
) {
  val colors = LocalAppColors.current
  val theme = LocalThemeName.current
  val isDark = isDarkTheme(theme)

  val incomeColor = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
  val expenseColor = if (isDark) Color(0xFFFF8A80) else Color(0xFFC62828)

  AppCard {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      SectionTitle(
        icon = themeSectionVisualIcon(theme, "pie", "📊"),
        title = strings["dashboard_income_vs_expense"],
        compact = true,
      )

      if (totalIncome == 0 && totalExpense == 0) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(text = strings["dashboard_no_data"], color = colors.muted, fontSize = 13.sp)
        }
      } else {
        val slices = listOf(
          DonutSlice(label = strings["page_income"], value = totalIncome, color = incomeColor),
          DonutSlice(label = strings["page_expense"], value = totalExpense, color = expenseColor),
        )
        val totalFlow = (totalIncome + totalExpense).coerceAtLeast(1)
        val targetIncomePercentage = ((totalIncome.toFloat() / totalFlow) * 100).roundToInt()
        val animatedIncomePercentage = rememberAnimatedAmount(targetIncomePercentage)
        val animatedExpensePercentage = (100 - animatedIncomePercentage).coerceAtLeast(0)

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          DonutChart(
            slices = slices,
            modifier = Modifier.size(130.dp),
            strokeWidth = 16.dp,
            centerLabel = "$animatedIncomePercentage%",
            centerSubLabel = strings["dashboard_income_short"],
          )

          Column(
            modifier = Modifier
              .weight(1f)
              .padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            // Income row
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Box(
                modifier = Modifier
                  .size(12.dp)
                  .clip(CircleShape)
                  .background(incomeColor),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(text = strings["page_income"], fontSize = 12.sp, color = colors.muted)
                AnimatedAmountText(
                  amount = totalIncome,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = colors.text,
                )
              }
              Text(
                text = "$animatedIncomePercentage%",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = incomeColor,
              )
            }

            // Expense row
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Box(
                modifier = Modifier
                  .size(12.dp)
                  .clip(CircleShape)
                  .background(expenseColor),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(text = strings["page_expense"], fontSize = 12.sp, color = colors.muted)
                AnimatedAmountText(
                  amount = totalExpense,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = colors.text,
                )
              }
              Text(
                text = "$animatedExpensePercentage%",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = expenseColor,
              )
            }
          }
        }
      }
    }
  }
}
