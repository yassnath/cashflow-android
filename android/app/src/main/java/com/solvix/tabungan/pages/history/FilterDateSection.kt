package com.solvix.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun FilterDateSection(
  fromDate: String,
  toDate: String,
  onFromDateChange: (String) -> Unit,
  onToDateChange: (String) -> Unit,
  strings: AppStrings = LocalStrings.current,
) {
  val colors = LocalAppColors.current

  fun applyDatePreset(presetKey: String) {
    val cal = Calendar.getInstance()
    val fmt = SimpleDateFormat("dd-MM-yyyy", Locale.US)
    when (presetKey) {
      "today" -> {
        val todayStr = fmt.format(cal.time)
        onFromDateChange(todayStr)
        onToDateChange(todayStr)
      }
      "week" -> {
        val endStr = fmt.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -6)
        val startStr = fmt.format(cal.time)
        onFromDateChange(startStr)
        onToDateChange(endStr)
      }
      "month" -> {
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startStr = fmt.format(cal.time)
        cal.set(Calendar.DAY_OF_MONTH, maxDay)
        val endStr = fmt.format(cal.time)
        onFromDateChange(startStr)
        onToDateChange(endStr)
      }
      "all" -> {
        onFromDateChange("01-01-2000")
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        onToDateChange(fmt.format(cal.time))
      }
    }
  }

  val presets = listOf(
    "today" to (strings["filter_preset_today"].ifBlank { "Hari Ini" }),
    "week" to (strings["filter_preset_week"].ifBlank { "Minggu Ini" }),
    "month" to (strings["filter_preset_month"].ifBlank { "Bulan Ini" }),
    "all" to (strings["filter_preset_all"].ifBlank { "Semua" }),
  )

  FilterSection(title = strings["filter_date_range"].ifBlank { "Rentang Tanggal" }, icon = "📅") {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      // Shortcut Pills
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        presets.forEach { (key, label) ->
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(colors.bg2)
              .clickable { applyDatePreset(key) }
              .padding(horizontal = 9.dp, vertical = 5.dp),
          ) {
            Text(
              text = label,
              fontSize = 11.sp,
              color = colors.accent,
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }

      // From - To Input Box
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(modifier = Modifier.weight(1f)) {
          Column {
            Text(
              text = strings["filter_from_date"].ifBlank { "Dari Tanggal" },
              fontSize = 10.sp,
              color = colors.muted,
              fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            DateField(
              label = "",
              value = fromDate,
              onValueChange = onFromDateChange,
              placeholder = "dd-MM-yyyy",
            )
          }
        }
        Text(
          text = "→",
          fontSize = 14.sp,
          color = colors.muted,
          modifier = Modifier.padding(top = 14.dp),
        )
        Box(modifier = Modifier.weight(1f)) {
          Column {
            Text(
              text = strings["filter_to_date"].ifBlank { "Sampai Tanggal" },
              fontSize = 10.sp,
              color = colors.muted,
              fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            DateField(
              label = "",
              value = toDate,
              onValueChange = onToDateChange,
              placeholder = "dd-MM-yyyy",
            )
          }
        }
      }
    }
  }
}
