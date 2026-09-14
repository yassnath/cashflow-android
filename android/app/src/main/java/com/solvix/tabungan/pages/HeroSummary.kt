package com.solvix.tabungan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun UUIDString(): String = java.util.UUID.randomUUID().toString()

@Composable
fun HeroSummary(
  range: SummaryRange,
  onRangeChange: (SummaryRange) -> Unit,
  incomeTotal: Int,
  expenseTotal: Int,
  strings: AppStrings,
) {
  val colors = LocalAppColors.current
  val isDark = isDarkTheme(LocalThemeName.current)
  val incomeColor = if (isDark) Color(0xFF7BE27A) else Color(0xFF2E7D32)
  val expenseColor = if (isDark) Color(0xFFFF8A80) else Color(0xFFC62828)
  val options = SummaryRange.values().toList()
  AppCard {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "${strings["summary_title"]} ${summaryRangeLabel(range, strings)}",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = colors.text,
      )
      AppDropdown(
        label = "",
        placeholder = summaryRangeLabel(range, strings),
        options = options.map { summaryRangeLabel(it, strings) },
        selected = summaryRangeLabel(range, strings),
        onSelected = { label ->
          val selected = options.firstOrNull { summaryRangeLabel(it, strings) == label }
          if (selected != null) {
            onRangeChange(selected)
          }
        },
        modifier = Modifier
          .widthIn(max = 130.dp)
          .weight(0.45f, fill = false),
      )
    }
    Spacer(modifier = Modifier.height(10.dp))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      StatRow(icon = "⬆️", label = strings["summary_income"], value = formatRupiah(incomeTotal), accentColor = incomeColor)
      StatRow(icon = "⬇️", label = strings["summary_expense"], value = formatRupiah(expenseTotal), accentColor = expenseColor)
      StatRow(icon = "✨", label = strings["summary_balance"], value = formatRupiah(incomeTotal - expenseTotal))
    }
  }
}
