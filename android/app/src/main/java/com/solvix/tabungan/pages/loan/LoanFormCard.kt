package com.solvix.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoanFormCard(
  isReceivable: Boolean,
  onIsReceivableChange: (Boolean) -> Unit,
  type: String,
  onTypeChange: (String) -> Unit,
  title: String,
  onTitleChange: (String) -> Unit,
  principal: String,
  onPrincipalChange: (String) -> Unit,
  paid: String,
  onPaidChange: (String) -> Unit,
  annualRate: String,
  onAnnualRateChange: (String) -> Unit,
  monthlyPayment: String,
  onMonthlyPaymentChange: (String) -> Unit,
  dueDate: String,
  onDueDateChange: (String) -> Unit,
  note: String,
  onNoteChange: (String) -> Unit,
  isEditing: Boolean,
  onCancelEdit: () -> Unit,
  onSubmit: () -> Unit,
  strings: AppStrings,
) {
  val colors = LocalAppColors.current

  val typeLabels = mapOf(
    "friend_debt" to strings["loan_type_friend_debt"],
    "installment" to strings["loan_type_installment"],
    "credit_card" to strings["loan_type_credit_card"],
    "paylater" to strings["loan_type_paylater"],
    "other" to strings["loan_type_other"],
  )
  val selectedTypeLabel = typeLabels[type].orEmpty()

  val titleValue = title.trim()
  val principalValue = parseAmount(principal)
  val monthlyPaymentValue = parseAmount(monthlyPayment)
  val isValid = type.isNotBlank() &&
    titleValue.isNotBlank() &&
    principalValue > 0 &&
    monthlyPaymentValue > 0 &&
    dueDate.isNotBlank()

  AppCard {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      // Toggle Segmented Hutang vs Piutang
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        listOf(
          false to (strings["loan_my_debt"].ifBlank { "🔴 Hutang Saya" }),
          true to (strings["loan_my_receivable"].ifBlank { "🟢 Piutang Saya" }),
        ).forEach { (receivable, label) ->
          val isSelected = isReceivable == receivable
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (isSelected) colors.accent else colors.bg2)
              .border(1.dp, if (isSelected) colors.accent else colors.cardBorder, RoundedCornerShape(10.dp))
              .clickable { onIsReceivableChange(receivable) }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = label,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) Color.White else colors.text,
            )
          }
        }
      }

      AppDropdown(
        label = if (isReceivable) (strings["loan_category_receivable"].ifBlank { "Kategori Piutang" }) else strings["loan_type"],
        placeholder = strings["placeholder_source"],
        options = typeLabels.values.toList(),
        selected = selectedTypeLabel,
        onSelected = { label ->
          onTypeChange(typeLabels.entries.firstOrNull { it.value == label }?.key.orEmpty())
        },
      )
      AppTextField(
        label = if (isReceivable) (strings["loan_borrower_name"].ifBlank { "Nama Peminjam / Keterangan" }) else strings["loan_title"],
        value = title,
        onValueChange = onTitleChange,
        placeholder = if (isReceivable) (strings["loan_borrower_placeholder"].ifBlank { "Contoh: Pinjaman Budi" }) else strings["placeholder_goal"],
      )
      AppTextField(
        label = if (isReceivable) (strings["loan_principal_receivable"].ifBlank { "Total Uang Dipinjamkan (Rp)" }) else strings["loan_principal"],
        value = principal,
        onValueChange = onPrincipalChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      )
      AppTextField(
        label = if (isReceivable) (strings["loan_paid_receivable"].ifBlank { "Sudah Dikembalikan (Rp)" }) else strings["loan_paid"],
        value = paid,
        onValueChange = onPaidChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      )
      if (!isReceivable) {
        AppTextField(
          label = strings["loan_interest_rate"],
          value = annualRate,
          onValueChange = onAnnualRateChange,
          placeholder = "0.0",
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
      }
      AppTextField(
        label = if (isReceivable) (strings["loan_monthly_receivable"].ifBlank { "Cicilan Pengembalian per Bulan (Rp)" }) else strings["loan_monthly_payment"],
        value = monthlyPayment,
        onValueChange = onMonthlyPaymentChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      )
      DateField(
        label = if (isReceivable) (strings["loan_due_date_receivable"].ifBlank { "Target Tanggal Lunas" }) else strings["loan_due_date"],
        value = dueDate,
        onValueChange = onDueDateChange,
        placeholder = strings["placeholder_date"],
      )
      AppTextField(
        label = strings["loan_note"],
        value = note,
        onValueChange = onNoteChange,
        placeholder = strings["placeholder_note"],
        minLines = 2,
      )

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        if (isEditing) {
          Box(
            modifier = Modifier
              .weight(0.8f)
              .clip(RoundedCornerShape(12.dp))
              .background(colors.bg2)
              .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
              .clickable { onCancelEdit() }
              .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(text = strings["cancel"].ifBlank { "Batal" }, color = colors.muted, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
        Box(modifier = Modifier.weight(1.5f)) {
          GradientButton(
            text = if (!isEditing) {
              if (isReceivable) strings["loan_save_receivable"].ifBlank { "Simpan Catatan Piutang" } else strings["loan_save_debt"].ifBlank { "Simpan Catatan Hutang" }
            } else {
              strings["loan_update_record"].ifBlank { "Perbarui Catatan" }
            },
            enabled = isValid,
            onClick = onSubmit,
          )
        }
      }
    }
  }
}
