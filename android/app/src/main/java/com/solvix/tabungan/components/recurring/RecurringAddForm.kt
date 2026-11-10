package com.solvix.tabungan.components.recurring

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppCard
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.AppTextField
import com.solvix.tabungan.GhostButton
import com.solvix.tabungan.GradientButton
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.RecurringTransaction

@Composable
fun RecurringAddForm(
  strings: AppStrings,
  onCancel: () -> Unit,
  onSave: (RecurringTransaction) -> Unit,
) {
  val colors = LocalAppColors.current

  var title by remember { mutableStateOf("") }
  var amountStr by remember { mutableStateOf("") }
  var isExpense by remember { mutableStateOf(true) }
  var dueDayStr by remember { mutableStateOf("1") }
  var category by remember { mutableStateOf("") }
  var note by remember { mutableStateOf("") }

  AppCard {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Text(
        text = strings["recurring_add_title"].ifBlank { "Tambah Langganan Rutin Baru" },
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = colors.text,
      )

      AppTextField(
        label = strings["recurring_label_title"].ifBlank { "Judul Langganan" },
        value = title,
        onValueChange = { title = it },
        placeholder = strings["recurring_placeholder_title"].ifBlank { "Misal: Netflix, Listrik, Kos" },
      )

      AppTextField(
        label = strings["amount"].ifBlank { "Nominal" },
        value = amountStr,
        onValueChange = { amountStr = it },
        placeholder = "150000",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      )

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isExpense) colors.danger.copy(alpha = 0.2f) else colors.bg2)
            .clickable { isExpense = true }
            .padding(vertical = 8.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = strings["entry_type_expense"].ifBlank { "Pengeluaran" },
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isExpense) colors.danger else colors.muted,
          )
        }
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (!isExpense) colors.accent.copy(alpha = 0.2f) else colors.bg2)
            .clickable { isExpense = false }
            .padding(vertical = 8.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = strings["entry_type_income"].ifBlank { "Pemasukan" },
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (!isExpense) colors.accent else colors.muted,
          )
        }
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.weight(1f)) {
          AppTextField(
            label = strings["recurring_due_day"].ifBlank { "Tanggal Tagihan" },
            value = dueDayStr,
            onValueChange = { dueDayStr = it },
            placeholder = "1 - 31",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          )
        }
        Box(modifier = Modifier.weight(1f)) {
          AppTextField(
            label = strings["category"].ifBlank { "Kategori" },
            value = category,
            onValueChange = { category = it },
            placeholder = "Hiburan",
          )
        }
      }

      AppTextField(
        label = strings["recurring_note_optional"].ifBlank { "Catatan Tambahan (Opsional)" },
        value = note,
        onValueChange = { note = it },
        placeholder = strings["note_optional"].ifBlank { "Catatan tambahan..." },
      )

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        GhostButton(
          text = strings["confirm_cancel"].ifBlank { "Batal" },
          modifier = Modifier.weight(1f),
          onClick = onCancel,
        )
        GradientButton(
          text = strings["save"].ifBlank { "Simpan" },
          modifier = Modifier.weight(1f),
          enabled = title.isNotBlank() && (amountStr.toIntOrNull() ?: 0) > 0,
          onClick = {
            val newObj = RecurringTransaction(
              title = title.trim(),
              amount = amountStr.toIntOrNull() ?: 0,
              type = if (isExpense) "Expense" else "Income",
              category = category.trim().ifBlank { "Rutin" },
              dueDay = (dueDayStr.toIntOrNull() ?: 1).coerceIn(1, 31),
              note = note.trim(),
            )
            onSave(newObj)
          },
        )
      }
    }
  }
}
