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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DashboardTrendLineChart(
  incomeEntries: List<MoneyEntry>,
  expenseEntries: List<MoneyEntry>,
  currentLang: AppLanguage,
  strings: AppStrings,
) {
  val colors = LocalAppColors.current
  val theme = LocalThemeName.current
  val currentCalendarYear = remember { Calendar.getInstance().get(Calendar.YEAR) }

  // Deteksi daftar tahun yang ada pada riwayat transaksi pengguna
  val availableYears = remember(incomeEntries.size, expenseEntries.size) {
    val years = (incomeEntries + expenseEntries).mapNotNull { entry ->
      val date = parseDate(entry.date) ?: return@mapNotNull null
      Calendar.getInstance().apply { timeInMillis = date }.get(Calendar.YEAR)
    }.distinct().sortedDescending()
    if (years.isEmpty()) listOf(currentCalendarYear) else years
  }

  var selectedYear by remember(availableYears) {
    mutableIntStateOf(if (availableYears.contains(currentCalendarYear)) currentCalendarYear else availableYears.first())
  }

  // Reactive series yang selalu ter-update begitu SnapshotStateList selesai dimuat
  val monthlySeries = remember(
    incomeEntries.size,
    expenseEntries.size,
    incomeEntries.sumOf { it.amount },
    expenseEntries.sumOf { it.amount },
    selectedYear,
    currentLang,
  ) {
    buildMonthlySeriesForYear(incomeEntries, expenseEntries, currentLang, selectedYear)
  }

  val yearIncomeTotal = remember(monthlySeries) { monthlySeries.income.sum() }
  val yearExpenseTotal = remember(monthlySeries) { monthlySeries.expense.sum() }

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
          modifier = Modifier.weight(1f, fill = false),
          compact = true,
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Year Selector Chips jika terdapat tahun berbeda atau default tahun saat ini
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          availableYears.take(3).forEach { year ->
            val isSelected = year == selectedYear
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) colors.accent.copy(alpha = 0.18f) else colors.bg2)
                .border(
                  width = 1.dp,
                  color = if (isSelected) colors.accent else colors.cardBorder,
                  shape = RoundedCornerShape(8.dp),
                )
                .clickable { selectedYear = year }
                .padding(horizontal = 8.dp, vertical = 4.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = year.toString(),
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) colors.accent else colors.muted,
              )
            }
          }
        }
      }

      // Legend indicators & mini yearly summary
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFF10B981)),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${strings["dashboard_income_short"]} (${formatRupiah(yearIncomeTotal)})",
              fontSize = 10.sp,
              color = colors.muted,
            )
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(colors.danger),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${strings["dashboard_expense_short"]} (${formatRupiah(yearExpenseTotal)})",
              fontSize = 10.sp,
              color = colors.muted,
            )
          }
        }
      }

      // Animated Line Chart Canvas
      LineChart(series = monthlySeries)
    }
  }
}

/**
 * Membangun data bulanan 12 bulan secara spesifik untuk tahun tertentu
 */
private fun buildMonthlySeriesForYear(
  income: List<MoneyEntry>,
  expense: List<MoneyEntry>,
  language: AppLanguage,
  year: Int,
): ChartSeries {
  val labels = mutableListOf<String>()
  val incomeTotals = mutableListOf<Int>()
  val expenseTotals = mutableListOf<Int>()
  val locale = if (language == AppLanguage.ID) {
    Locale.Builder().setLanguage("id").setRegion("ID").build()
  } else {
    Locale.US
  }
  val fmt = SimpleDateFormat("MMM", locale)
  for (month in 0..11) {
    val cal = Calendar.getInstance().apply {
      set(Calendar.YEAR, year)
      set(Calendar.MONTH, month)
      set(Calendar.DAY_OF_MONTH, 1)
    }
    labels.add(fmt.format(cal.time))
    incomeTotals.add(income.sumOf { entry ->
      val date = parseDate(entry.date) ?: return@sumOf 0
      val dCal = Calendar.getInstance().apply { timeInMillis = date }
      if (dCal.get(Calendar.YEAR) == year && dCal.get(Calendar.MONTH) == month) entry.amount else 0
    })
    expenseTotals.add(expense.sumOf { entry ->
      val date = parseDate(entry.date) ?: return@sumOf 0
      val dCal = Calendar.getInstance().apply { timeInMillis = date }
      if (dCal.get(Calendar.YEAR) == year && dCal.get(Calendar.MONTH) == month) entry.amount else 0
    })
  }
  return ChartSeries(labels, incomeTotals, expenseTotals)
}
