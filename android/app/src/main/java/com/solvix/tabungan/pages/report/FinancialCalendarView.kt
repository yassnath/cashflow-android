package com.solvix.tabungan.pages.report

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppCard
import com.solvix.tabungan.AppDimens
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.EntryType
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.LocalStrings
import com.solvix.tabungan.MoneyEntry
import com.solvix.tabungan.formatRupiah
import com.solvix.tabungan.parseDate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun FinancialCalendarView(
  income: List<MoneyEntry>,
  expense: List<MoneyEntry>,
  strings: AppStrings = LocalStrings.current,
) {
  val colors = LocalAppColors.current

  var calendarMonth by remember {
    val cal = Calendar.getInstance()
    mutableStateOf(cal)
  }
  var selectedDay by remember {
    val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    mutableIntStateOf(today)
  }

  val currentYear = calendarMonth.get(Calendar.YEAR)
  val currentMonth = calendarMonth.get(Calendar.MONTH) // 0-indexed

  val idLocale = Locale.Builder().setLanguage("id").setRegion("ID").build()
  val monthYearTitle = remember(calendarMonth) {
    val fmt = SimpleDateFormat("MMMM yyyy", idLocale)
    fmt.format(calendarMonth.time)
  }

  // 1. Filter entri yang terjadi di bulan ini
  val entriesThisMonth = remember(income, expense, currentYear, currentMonth) {
    val all = (income + expense)
    all.filter { entry ->
      val d = parseDate(entry.date) ?: return@filter false
      val c = Calendar.getInstance().apply { timeInMillis = d }
      c.get(Calendar.YEAR) == currentYear && c.get(Calendar.MONTH) == currentMonth
    }
  }

  val monthIncomeTotal = entriesThisMonth.filter { it.type == EntryType.Income }.sumOf { it.amount.toLong() }
  val monthExpenseTotal = entriesThisMonth.filter { it.type == EntryType.Expense }.sumOf { it.amount.toLong() }
  val monthNet = monthIncomeTotal - monthExpenseTotal

  // 2. Map transaksi per hari (Day of Month -> List<MoneyEntry>)
  val entriesByDay = remember(entriesThisMonth) {
    entriesThisMonth.groupBy { entry ->
      val d = parseDate(entry.date) ?: 0L
      val c = Calendar.getInstance().apply { timeInMillis = d }
      c.get(Calendar.DAY_OF_MONTH)
    }
  }

  // 3. Kalender Grid Setup
  val daysInMonth = remember(calendarMonth) {
    val c = calendarMonth.clone() as Calendar
    c.set(Calendar.DAY_OF_MONTH, 1)
    c.getActualMaximum(Calendar.DAY_OF_MONTH)
  }

  val firstDayOfWeek = remember(calendarMonth) {
    val c = calendarMonth.clone() as Calendar
    c.set(Calendar.DAY_OF_MONTH, 1)
    // Calendar.SUNDAY = 1, MONDAY = 2 ...
    // Ubah agar Senin = 0, Minggu = 6
    val dow = c.get(Calendar.DAY_OF_WEEK)
    (dow + 5) % 7
  }

  val dayLabels = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

  val todayCal = Calendar.getInstance()
  val isCurrentMonthToday = todayCal.get(Calendar.YEAR) == currentYear && todayCal.get(Calendar.MONTH) == currentMonth
  val todayDateNum = todayCal.get(Calendar.DAY_OF_MONTH)

  AppCard {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      // Header Navigasi Bulan: < Bulan Tahun >
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(colors.bg2)
              .clickable {
                val next = calendarMonth.clone() as Calendar
                next.add(Calendar.MONTH, -1)
                calendarMonth = next
                selectedDay = 1
              },
            contentAlignment = Alignment.Center,
          ) {
            Icon(Icons.Rounded.ChevronLeft, contentDescription = "Bulan Lalu", tint = colors.text)
          }

          Text(
            text = monthYearTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = colors.text,
          )

          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(colors.bg2)
              .clickable {
                val next = calendarMonth.clone() as Calendar
                next.add(Calendar.MONTH, 1)
                calendarMonth = next
                selectedDay = 1
              },
            contentAlignment = Alignment.Center,
          ) {
            Icon(Icons.Rounded.ChevronRight, contentDescription = "Bulan Depan", tint = colors.text)
          }
        }

        if (!isCurrentMonthToday) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(colors.accent.copy(alpha = 0.12f))
              .clickable {
                calendarMonth = Calendar.getInstance()
                selectedDay = todayDateNum
              }
              .padding(horizontal = 10.dp, vertical = 5.dp),
          ) {
            Text(text = "Hari Ini", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.accent)
          }
        }
      }

      // Ringkasan Arus Kas Bulan Ini
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(AppDimens.radiusSm))
          .background(colors.bg2.copy(alpha = 0.7f))
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(text = strings["dashboard_income_short"].ifBlank { "Masuk" }, fontSize = 10.sp, color = colors.muted)
          Text(text = formatRupiah(monthIncomeTotal), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2EC4B6))
        }
        Column {
          Text(text = strings["dashboard_expense_short"].ifBlank { "Keluar" }, fontSize = 10.sp, color = colors.muted)
          Text(text = formatRupiah(monthExpenseTotal), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF4D6D))
        }
        Column(horizontalAlignment = Alignment.End) {
          Text(text = strings["summary_balance"].ifBlank { "Selisih Bersih" }, fontSize = 10.sp, color = colors.muted)
          Text(
            text = formatRupiah(monthNet),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (monthNet >= 0) colors.text else Color(0xFFFF4D6D),
          )
        }
      }

      // Label Hari (Sen - Min)
      Row(modifier = Modifier.fillMaxWidth()) {
        dayLabels.forEach { label ->
          Text(
            text = label,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.muted,
          )
        }
      }

      // Grid Tanggal
      val totalSlots = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7
      val rows = totalSlots / 7

      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (r in 0 until rows) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            for (c in 0 until 7) {
              val slotIndex = (r * 7) + c
              val dayNum = slotIndex - firstDayOfWeek + 1

              if (dayNum in 1..daysInMonth) {
                val isSelected = dayNum == selectedDay
                val isToday = isCurrentMonthToday && dayNum == todayDateNum
                val dayEntries = entriesByDay[dayNum].orEmpty()
                val hasIncome = dayEntries.any { it.type == EntryType.Income }
                val hasExpense = dayEntries.any { it.type == EntryType.Expense }

                CalendarDayCell(
                  dayNum = dayNum,
                  isSelected = isSelected,
                  isToday = isToday,
                  hasIncome = hasIncome,
                  hasExpense = hasExpense,
                  onClick = { selectedDay = dayNum },
                  modifier = Modifier.weight(1f),
                )
              } else {
                Spacer(modifier = Modifier.weight(1f))
              }
            }
          }
        }
      }

      // Detail Transaksi Hari yang Dipilih
      CalendarDayDetails(
        selectedDay = selectedDay,
        monthYearTitle = monthYearTitle,
        selectedDayEntries = entriesByDay[selectedDay].orEmpty(),
      )
    }
  }
}
