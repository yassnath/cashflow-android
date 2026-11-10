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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FilterTypeAmountSection(
  selectedType: EntryType?,
  onSelectType: (EntryType?) -> Unit,
  minAmount: String,
  onMinAmountChange: (String) -> Unit,
  maxAmount: String,
  onMaxAmountChange: (String) -> Unit,
  strings: AppStrings = LocalStrings.current,
) {
  val colors = LocalAppColors.current

  FilterSection(title = strings["filter_type_amount"].ifBlank { "Tipe & Rentang Nominal" }, icon = Icons.Rounded.Payments) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      // Tipe Segmented Control
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        listOf(
          null to (strings["filter_preset_all"].ifBlank { "Semua" }),
          EntryType.Income to (strings["entry_type_income"].ifBlank { "Pemasukan" }),
          EntryType.Expense to (strings["entry_type_expense"].ifBlank { "Pengeluaran" }),
          EntryType.Transfer to (strings["entry_type_transfer"].ifBlank { "Transfer" }),
        ).forEach { (type, label) ->
          val isSelected = selectedType == type
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) colors.accent else colors.bg2)
              .border(
                width = 1.dp,
                color = if (isSelected) colors.accent else colors.cardBorder,
                shape = RoundedCornerShape(8.dp),
              )
              .clickable { onSelectType(type) }
              .padding(vertical = 7.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = label,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) Color.White else colors.text,
            )
          }
        }
      }

      // Nominal Min - Max
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Box(modifier = Modifier.weight(1f)) {
          Column {
            Text(
              text = "${strings["filter_min_amount"].ifBlank { "Minimal" }} (${CurrencyHelper.currentCurrency.symbol})",
              fontSize = 10.sp,
              color = colors.muted,
              fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            AppTextField(
              label = "",
              value = minAmount,
              onValueChange = onMinAmountChange,
              placeholder = "0",
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
          }
        }
        Box(modifier = Modifier.weight(1f)) {
          Column {
            Text(
              text = "${strings["filter_max_amount"].ifBlank { "Maksimal" }} (${CurrencyHelper.currentCurrency.symbol})",
              fontSize = 10.sp,
              color = colors.muted,
              fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            AppTextField(
              label = "",
              value = maxAmount,
              onValueChange = onMaxAmountChange,
              placeholder = strings["filter_unlimited"].ifBlank { "Tanpa Batas" },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
          }
        }
      }
    }
  }
}
