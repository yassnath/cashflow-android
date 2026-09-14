package com.solvix.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppCard
import com.solvix.tabungan.AppDimens
import com.solvix.tabungan.AppDropdown
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.AppTextField
import com.solvix.tabungan.GradientButton
import com.solvix.tabungan.LoanEntry
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.LocalThemeName
import com.solvix.tabungan.Page
import com.solvix.tabungan.SectionTitle
import com.solvix.tabungan.UUIDString
import com.solvix.tabungan.formatRupiah
import com.solvix.tabungan.parseAmount
import com.solvix.tabungan.remainingLoanBalance
import com.solvix.tabungan.simulateLoanBalanceAfterYear
import com.solvix.tabungan.simulateLoanPayoffMonths
import com.solvix.tabungan.themePageIcon

@Composable
fun LoanTrackingPage(
  entries: List<LoanEntry>,
  onInvalid: () -> Unit,
  onSave: (LoanEntry) -> Unit,
  onUpdate: (LoanEntry) -> Unit,
  onDelete: (LoanEntry) -> Unit,
  strings: AppStrings,
) {
  var type by rememberSaveable { mutableStateOf("") }
  var title by rememberSaveable { mutableStateOf("") }
  var principal by rememberSaveable { mutableStateOf("") }
  var paid by rememberSaveable { mutableStateOf("") }
  var annualRate by rememberSaveable { mutableStateOf("") }
  var monthlyPayment by rememberSaveable { mutableStateOf("") }
  var dueDate by rememberSaveable { mutableStateOf("") }
  var note by rememberSaveable { mutableStateOf("") }
  var editingId by rememberSaveable { mutableStateOf<String?>(null) }
  var editingLinkedExpenseId by rememberSaveable { mutableStateOf<String?>(null) }

  val typeLabels = mapOf(
    "friend_debt" to strings["loan_type_friend_debt"],
    "installment" to strings["loan_type_installment"],
    "credit_card" to strings["loan_type_credit_card"],
    "paylater" to strings["loan_type_paylater"],
    "other" to strings["loan_type_other"],
  )
  val selectedTypeLabel = typeLabels[type].orEmpty()

  fun beginEdit(entry: LoanEntry) {
    editingId = entry.id
    type = entry.type
    title = entry.title
    principal = entry.principal.toString()
    paid = entry.paid.toString()
    annualRate = formatRateInput(entry.annualInterestRate)
    monthlyPayment = entry.monthlyPayment.toString()
    dueDate = entry.dueDate
    note = entry.note
    editingLinkedExpenseId = entry.linkedExpenseId
  }

  fun resetForm() {
    editingId = null
    editingLinkedExpenseId = null
    type = ""
    title = ""
    principal = ""
    paid = ""
    annualRate = ""
    monthlyPayment = ""
    dueDate = ""
    note = ""
  }

  Column {
    SectionTitle(
      icon = themePageIcon(LocalThemeName.current, Page.Loans),
      title = strings["section_loans_title"],
      subtitle = strings["section_loans_subtitle"],
    )
    AppCard {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AppDropdown(
          label = strings["loan_type"],
          placeholder = strings["placeholder_source"],
          options = typeLabels.values.toList(),
          selected = selectedTypeLabel,
          onSelected = { label ->
            type = typeLabels.entries.firstOrNull { it.value == label }?.key.orEmpty()
          },
        )
        AppTextField(
          label = strings["loan_title"],
          value = title,
          onValueChange = { title = it },
          placeholder = strings["placeholder_goal"],
        )
        AppTextField(
          label = strings["loan_principal"],
          value = principal,
          onValueChange = { principal = it },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        AppTextField(
          label = strings["loan_paid"],
          value = paid,
          onValueChange = { paid = it },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        AppTextField(
          label = strings["loan_interest_rate"],
          value = annualRate,
          onValueChange = { annualRate = it },
          placeholder = "0.0",
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        AppTextField(
          label = strings["loan_monthly_payment"],
          value = monthlyPayment,
          onValueChange = { monthlyPayment = it },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        DateField(
          label = strings["loan_due_date"],
          value = dueDate,
          onValueChange = { dueDate = it },
          placeholder = strings["placeholder_date"],
        )
        AppTextField(
          label = strings["loan_note"],
          value = note,
          onValueChange = { note = it },
          placeholder = strings["placeholder_note"],
          minLines = 2,
        )
        val titleValue = title.trim()
        val principalValue = parseAmount(principal)
        val paidValue = parseAmount(paid).coerceAtLeast(0)
        val annualRateValue = parseRate(annualRate)
        val monthlyPaymentValue = parseAmount(monthlyPayment)
        val isValid = type.isNotBlank() &&
          titleValue.isNotBlank() &&
          principalValue > 0 &&
          monthlyPaymentValue > 0 &&
          dueDate.isNotBlank()
        GradientButton(
          text = if (editingId == null) strings["save_dream"] else strings["update_dream"],
          enabled = isValid,
          onClick = {
            if (!isValid) {
              onInvalid()
              return@GradientButton
            }
            val payload = LoanEntry(
              id = editingId ?: UUIDString(),
              type = type,
              title = titleValue,
              principal = principalValue,
              paid = paidValue.coerceIn(0, principalValue),
              annualInterestRate = annualRateValue.coerceAtLeast(0.0),
              monthlyPayment = monthlyPaymentValue,
              dueDate = dueDate,
              note = note.trim(),
              linkedExpenseId = editingLinkedExpenseId,
            )
            if (editingId == null) onSave(payload) else onUpdate(payload)
            resetForm()
          },
        )
      }
    }
    if (entries.isNotEmpty()) {
      Spacer(modifier = Modifier.height(12.dp))
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        entries.forEach { entry ->
          LoanEntryCard(
            entry = entry,
            onEdit = { beginEdit(entry) },
            onDelete = { onDelete(entry) },
            strings = strings,
          )
        }
      }
    }
  }
}

@Composable
private fun LoanEntryCard(
  entry: LoanEntry,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  strings: AppStrings,
) {
  val colors = LocalAppColors.current
  val remaining = remainingLoanBalance(entry)
  val payoffMonths = simulateLoanPayoffMonths(entry)
  val balanceAfterYear = simulateLoanBalanceAfterYear(entry)
  val dueDateLabel = entry.dueDate.ifBlank { "-" }
  val noteText = entry.note.ifBlank { "-" }
  val remainingColor = if (remaining > 0) colors.danger else colors.accent2

  AppCard(shape = RoundedCornerShape(AppDimens.radiusMd)) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(text = entry.title, fontWeight = FontWeight.Bold, color = colors.text)
        Text(
          text = loanTypeLabel(entry.type, strings),
          color = colors.muted,
          fontSize = 11.sp,
        )
      }
      Text(text = "${strings["loan_principal"]}: ${formatRupiah(entry.principal)}", color = colors.muted, fontSize = 12.sp)
      Text(text = "${strings["loan_paid"]}: ${formatRupiah(entry.paid)}", color = colors.muted, fontSize = 12.sp)
      Text(
        text = "${strings["loan_remaining"]}: ${formatRupiah(remaining)}",
        color = remainingColor,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
      )
      Text(
        text = "${strings["loan_interest_simulation"]}: ${entry.annualInterestRate}%",
        color = colors.muted,
        fontSize = 12.sp,
      )
      Text(
        text = "${strings["loan_payoff_estimate"]}: ${loanPayoffEstimateLabel(payoffMonths, strings)}",
        color = colors.muted,
        fontSize = 12.sp,
      )
      Text(
        text = "${strings["loan_balance_after_year"]}: ${formatRupiah(balanceAfterYear)}",
        color = colors.muted,
        fontSize = 12.sp,
      )
      Text(text = "${strings["loan_due_date"]}: $dueDateLabel", color = colors.muted, fontSize = 12.sp)
      Text(text = "${strings["note_label"]}: $noteText", color = colors.muted, fontSize = 12.sp)
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Box(
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(colors.accent, colors.accent2)))
            .clickable { onEdit() },
          contentAlignment = Alignment.Center,
        ) {
          Text(text = strings["edit"], color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
        Box(
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFD32F2F))
            .clickable { onDelete() },
          contentAlignment = Alignment.Center,
        ) {
          Text(text = strings["delete"], color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
      }
    }
  }
}

fun parseRate(text: String): Double {
  val normalized = text.trim().replace(',', '.')
  return normalized.toDoubleOrNull() ?: 0.0
}

fun formatRateInput(value: Double): String {
  return if (value % 1.0 == 0.0) {
    value.toInt().toString()
  } else {
    value.toString()
  }
}

fun loanTypeLabel(type: String, strings: AppStrings): String {
  return when (type) {
    "friend_debt" -> strings["loan_type_friend_debt"]
    "installment" -> strings["loan_type_installment"]
    "credit_card" -> strings["loan_type_credit_card"]
    "paylater" -> strings["loan_type_paylater"]
    else -> strings["loan_type_other"]
  }
}

fun loanPayoffEstimateLabel(months: Int?, strings: AppStrings): String {
  return when {
    months == null -> strings["loan_no_estimate"]
    months == 0 -> strings["loan_paid_off"]
    else -> "$months ${strings["loan_month_unit"]}"
  }
}
