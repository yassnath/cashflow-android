package com.solvix.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppCard
import androidx.compose.foundation.layout.Spacer
import com.solvix.tabungan.AppDimens
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.LocalStrings
import com.solvix.tabungan.LocalThemeName
import com.solvix.tabungan.Page
import com.solvix.tabungan.SectionTitle
import com.solvix.tabungan.pages.calculator.SplitBillSection
import com.solvix.tabungan.themePageIcon

@Composable
fun CalculatorPage(
  onRecordToExpense: (Long) -> Unit = {},
  onToast: (String) -> Unit = {},
) {
  var display by rememberSaveable { mutableStateOf("0") }
  var activeTab by rememberSaveable { mutableStateOf("calculator") }
  val strings = LocalStrings.current
  val colors = LocalAppColors.current

  Column {
    SectionTitle(
      icon = themePageVisualIcon(LocalThemeName.current, Page.Calculator),
      title = strings["calculator_title"],
      subtitle = strings["calculator_subtitle"],
    )

    // Segmented Tab Selector
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(AppDimens.radiusSm))
        .background(colors.bg2)
        .padding(4.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(AppDimens.radiusSm))
          .background(if (activeTab == "calculator") colors.accent else Color.Transparent)
          .clickable { activeTab = "calculator" }
          .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "🧮 " + strings["tab_standard_calc"].ifBlank { "Kalkulator" },
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = if (activeTab == "calculator") Color.White else colors.muted,
        )
      }

      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(AppDimens.radiusSm))
          .background(if (activeTab == "split_bill") colors.accent else Color.Transparent)
          .clickable { activeTab = "split_bill" }
          .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "👥 " + strings["tab_split_bill"].ifBlank { "Bagi Tagihan" },
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = if (activeTab == "split_bill") Color.White else colors.muted,
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (activeTab == "split_bill") {
      SplitBillSection(
        onRecordToExpense = onRecordToExpense,
        onToast = onToast,
        strings = strings,
      )
    } else {
      AppCard {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.radiusSm))
            .background(colors.bg2)
            .testTag("calculator_display")
            .padding(12.dp),
          contentAlignment = Alignment.CenterEnd,
        ) {
          Text(text = display, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.text)
        }

        val rows = listOf(
          listOf("sin", "cos", "tan", "ln", "log"),
          listOf("√", "^", "(", ")", "π"),
          listOf("7", "8", "9", "÷", "C"),
          listOf("4", "5", "6", "×", "⌫"),
          listOf("1", "2", "3", "−", "%"),
          listOf("0", ".", "e", "+", "="),
        )
        rows.forEach { row ->
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            row.forEach { key ->
              CalculatorKey(
                label = key,
                isPrimary = key == "=",
                onPress = { display = calculate(display, key) },
              )
            }
          }
        }
      }
    }
  }
}
}

@Composable
private fun RowScope.CalculatorKey(label: String, onPress: () -> Unit, isPrimary: Boolean = false) {
  val colors = LocalAppColors.current
  val isOperator = label in listOf("÷", "×", "−", "+", "^", "%")
  val isFunction = label in listOf("sin", "cos", "tan", "ln", "log", "√")
  val backgroundColor = when {
    label == "C" || label == "⌫" -> colors.danger.copy(alpha = 0.2f)
    isOperator -> colors.accent.copy(alpha = 0.2f)
    isFunction -> colors.accent2.copy(alpha = 0.18f)
    else -> colors.bg2
  }
  val backgroundModifier = if (isPrimary) {
    Modifier.background(brush = Brush.linearGradient(listOf(colors.accent, colors.accent2)))
  } else {
    Modifier.background(color = backgroundColor)
  }
  val textColor = when {
    isPrimary -> Color.White
    label == "C" || label == "⌫" -> Color(0xFFB92643)
    isOperator -> Color(0xFFA55B00)
    isFunction -> colors.accent2
    else -> colors.text
  }
  val fontSize = if (label.length > 1) 12.sp else 18.sp

  Box(
    modifier = Modifier
      .weight(1f)
      .height(48.dp)
      .clip(RoundedCornerShape(14.dp))
      .then(backgroundModifier)
      .clickable { onPress() }
      .padding(vertical = 12.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(text = label, fontSize = fontSize, fontWeight = FontWeight.SemiBold, color = textColor)
  }
}


