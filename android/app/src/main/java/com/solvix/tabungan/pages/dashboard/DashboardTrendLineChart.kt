package com.solvix.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardTrendLineChart(
  incomeEntries: List<MoneyEntry>,
  expenseEntries: List<MoneyEntry>,
  currentLang: AppLanguage,
  strings: AppStrings,
) {
  val colors = LocalAppColors.current
  val theme = LocalThemeName.current

  AppCard {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SectionTitle(
          icon = themeSectionVisualIcon(theme, "trend", "📈"),
          title = strings["dashboard_monthly_trend"],
        )
        // Legend indicators
        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(colors.accent2),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = strings["dashboard_income_short"], fontSize = 11.sp, color = colors.muted)
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(colors.danger),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = strings["dashboard_expense_short"], fontSize = 11.sp, color = colors.muted)
          }
        }
      }

      val monthlySeries = remember(incomeEntries, expenseEntries, currentLang) {
        buildMonthlySeries(incomeEntries, expenseEntries, currentLang)
      }

      LineChart(series = monthlySeries)
    }
  }
}
