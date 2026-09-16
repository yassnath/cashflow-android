package com.solvix.tabungan

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import kotlin.math.roundToInt

enum class ExpenseTimeframe {
  ThisWeek,
  ThisMonth,
  ThisYear,
  All,
}

@Composable
fun DashboardTopExpenseBars(
  thisMonthExpenses: List<MoneyEntry>,
  allExpenseEntries: List<MoneyEntry>,
  strings: AppStrings,
) {
  val colors = LocalAppColors.current
  val theme = LocalThemeName.current

  var selectedTimeframe by remember { mutableStateOf(ExpenseTimeframe.ThisMonth) }
  var dropdownExpanded by remember { mutableStateOf(false) }

  val targetExpenses = remember(selectedTimeframe, allExpenseEntries, thisMonthExpenses) {
    val now = Calendar.getInstance()
    val currentYear = now.get(Calendar.YEAR)
    val currentMonth = now.get(Calendar.MONTH)
    val currentWeek = now.get(Calendar.WEEK_OF_YEAR)

    when (selectedTimeframe) {
      ExpenseTimeframe.ThisWeek -> {
        allExpenseEntries.filter { entry ->
          val millis = parseDate(entry.date) ?: return@filter false
          val cal = Calendar.getInstance().apply { timeInMillis = millis }
          cal.get(Calendar.YEAR) == currentYear && cal.get(Calendar.WEEK_OF_YEAR) == currentWeek
        }
      }
      ExpenseTimeframe.ThisMonth -> {
        if (thisMonthExpenses.isNotEmpty()) {
          thisMonthExpenses
        } else {
          allExpenseEntries.filter { entry ->
            val millis = parseDate(entry.date) ?: return@filter false
            val cal = Calendar.getInstance().apply { timeInMillis = millis }
            cal.get(Calendar.YEAR) == currentYear && cal.get(Calendar.MONTH) == currentMonth
          }
        }
      }
      ExpenseTimeframe.ThisYear -> {
        allExpenseEntries.filter { entry ->
          val millis = parseDate(entry.date) ?: return@filter false
          val cal = Calendar.getInstance().apply { timeInMillis = millis }
          cal.get(Calendar.YEAR) == currentYear
        }
      }
      ExpenseTimeframe.All -> allExpenseEntries
    }
  }

  val timeframeLabel = when (selectedTimeframe) {
    ExpenseTimeframe.ThisWeek -> strings["timeframe_this_week"]
    ExpenseTimeframe.ThisMonth -> strings["timeframe_this_month"]
    ExpenseTimeframe.ThisYear -> strings["timeframe_this_year"]
    ExpenseTimeframe.All -> strings["timeframe_all"]
  }

  AppCard {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SectionTitle(
          icon = themeSectionVisualIcon(theme, "bar", "🏷️"),
          title = strings["dashboard_top_expenses"],
          modifier = Modifier.weight(1f, fill = false),
          compact = true,
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Timeframe Dropdown Trigger Chip
        Box {
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(colors.accent.copy(alpha = 0.12f))
              .clickable { dropdownExpanded = true }
              .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
          ) {
            Text(
              text = timeframeLabel,
              fontSize = 11.sp,
              color = colors.accent,
              fontWeight = FontWeight.SemiBold,
            )
            Icon(
              imageVector = Icons.Rounded.KeyboardArrowDown,
              contentDescription = null,
              tint = colors.accent,
              modifier = Modifier.size(16.dp),
            )
          }

          DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false },
            modifier = Modifier
              .background(colors.card)
              .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp)),
          ) {
            ExpenseTimeframe.values().forEach { tf ->
              val isSelected = tf == selectedTimeframe
              val itemLabel = when (tf) {
                ExpenseTimeframe.ThisWeek -> strings["timeframe_this_week"]
                ExpenseTimeframe.ThisMonth -> strings["timeframe_this_month"]
                ExpenseTimeframe.ThisYear -> strings["timeframe_this_year"]
                ExpenseTimeframe.All -> strings["timeframe_all"]
              }
              DropdownMenuItem(
                text = {
                  Text(
                    text = itemLabel,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) colors.accent else colors.text,
                  )
                },
                onClick = {
                  selectedTimeframe = tf
                  dropdownExpanded = false
                },
              )
            }
          }
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
                  AnimatedAmountText(
                    amount = entry.value,
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
