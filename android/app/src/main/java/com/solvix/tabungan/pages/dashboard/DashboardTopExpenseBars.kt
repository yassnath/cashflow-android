package com.solvix.tabungan

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun DashboardTopExpenseBars(
  thisMonthExpenses: List<MoneyEntry>,
  allExpenseEntries: List<MoneyEntry>,
  strings: AppStrings,
) {
  val colors = LocalAppColors.current
  val targetExpenses = if (thisMonthExpenses.isNotEmpty()) thisMonthExpenses else allExpenseEntries
  val isCurrentMonthData = thisMonthExpenses.isNotEmpty()

  AppCard {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SectionTitle(
          icon = "🏷️",
          title = strings["dashboard_top_expenses"],
        )
        if (isCurrentMonthData) {
          Text(
            text = strings["dashboard_this_month"],
            fontSize = 11.sp,
            color = colors.accent,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }

      if (targetExpenses.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(text = strings["dashboard_no_data"], color = colors.muted, fontSize = 13.sp)
        }
      } else {
        val totalExpensesScope = targetExpenses.sumOf { it.amount }.coerceAtLeast(1)
        val topCategories = remember(targetExpenses) {
          targetExpenses
            .groupBy { it.category }
            .mapValues { (_, entries) -> entries.sumOf { it.amount } }
            .entries
            .sortedByDescending { it.value }
            .take(5)
        }

        val maxCategoryValue = (topCategories.firstOrNull()?.value ?: 1).coerceAtLeast(1)

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          topCategories.forEachIndexed { index, entry ->
            val percentage = ((entry.value.toFloat() / totalExpensesScope) * 100).roundToInt()
            val targetRatio = (entry.value.toFloat() / maxCategoryValue).coerceIn(0.05f, 1f)
            val animatedProgress by animateFloatAsState(
              targetValue = targetRatio,
              animationSpec = tween(durationMillis = 600 + index * 100, easing = FastOutSlowInEasing),
              label = "bar_progress_$index",
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = entry.key,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Medium,
                  color = colors.text,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f, fill = false),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "$percentage%  ",
                    fontSize = 11.sp,
                    color = colors.muted,
                  )
                  Text(
                    text = formatRupiah(entry.value),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text,
                  )
                }
              }

              // Bar Track & Fill
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(8.dp)
                  .clip(RoundedCornerShape(999.dp))
                  .background(colors.bg2),
              ) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                      Brush.horizontalGradient(
                        listOf(
                          colors.accent,
                          colors.accent2,
                        )
                      )
                    ),
                )
              }
            }
          }
        }
      }
    }
  }
}
