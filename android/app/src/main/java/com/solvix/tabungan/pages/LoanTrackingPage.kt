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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoanTrackingPage(
  entries: List<LoanEntry>,
  onInvalid: () -> Unit,
  onSave: (LoanEntry) -> Unit,
  onUpdate: (LoanEntry) -> Unit,
  onDelete: (LoanEntry) -> Unit,
  strings: AppStrings,
) {
  val colors = LocalAppColors.current

  var selectedTab by rememberSaveable { mutableStateOf<LoanTab?>(null) } // null = Semua
  var isReceivable by rememberSaveable { mutableStateOf(false) }
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

  fun beginEdit(entry: LoanEntry) {
    editingId = entry.id
    isReceivable = entry.isReceivable
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
    isReceivable = false
    type = ""
    title = ""
    principal = ""
    paid = ""
    annualRate = ""
    monthlyPayment = ""
    dueDate = ""
    note = ""
  }

  val filteredEntries = when (selectedTab) {
    LoanTab.Debt -> entries.filter { !it.isReceivable }
    LoanTab.Receivable -> entries.filter { it.isReceivable }
    null -> entries
  }

  Column(modifier = Modifier.imePadding()) {
    SectionTitle(
      icon = themePageVisualIcon(LocalThemeName.current, Page.Loans),
      title = "Hutang & Piutang",
      subtitle = "Pantau pinjaman yang harus dibayar dan uang yang dipinjamkan",
    )

    // Ringkasan Total Hutang & Piutang
    LoanSummaryCard(entries = entries)

    Spacer(modifier = Modifier.height(14.dp))

    // Form Input / Edit
    LoanFormCard(
      isReceivable = isReceivable,
      onIsReceivableChange = { isReceivable = it },
      type = type,
      onTypeChange = { type = it },
      title = title,
      onTitleChange = { title = it },
      principal = principal,
      onPrincipalChange = { principal = it },
      paid = paid,
      onPaidChange = { paid = it },
      annualRate = annualRate,
      onAnnualRateChange = { annualRate = it },
      monthlyPayment = monthlyPayment,
      onMonthlyPaymentChange = { monthlyPayment = it },
      dueDate = dueDate,
      onDueDateChange = { dueDate = it },
      note = note,
      onNoteChange = { note = it },
      isEditing = editingId != null,
      onCancelEdit = { resetForm() },
      onSubmit = {
        val titleValue = title.trim()
        val principalValue = parseAmount(principal)
        val paidValue = parseAmount(paid).coerceAtLeast(0)
        val annualRateValue = parseRate(annualRate)
        val monthlyPaymentValue = parseAmount(monthlyPayment)
        if (type.isBlank() || titleValue.isBlank() || principalValue <= 0 || monthlyPaymentValue <= 0 || dueDate.isBlank()) {
          onInvalid()
          return@LoanFormCard
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
          isReceivable = isReceivable,
        )
        if (editingId == null) onSave(payload) else onUpdate(payload)
        resetForm()
      },
      strings = strings,
    )

    if (entries.isNotEmpty()) {
      Spacer(modifier = Modifier.height(16.dp))

      // Tab Filter Pills
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        listOf(
          null to "Semua (${entries.size})",
          LoanTab.Debt to "Hutang (${entries.count { !it.isReceivable }})",
          LoanTab.Receivable to "Piutang (${entries.count { it.isReceivable }})",
        ).forEach { (tab, label) ->
          val isSelected = selectedTab == tab
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) colors.accent else colors.bg2)
              .border(1.dp, if (isSelected) colors.accent else colors.cardBorder, RoundedCornerShape(8.dp))
              .clickable { selectedTab = tab }
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

      Spacer(modifier = Modifier.height(10.dp))

      // Daftar Kartu
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        filteredEntries.forEach { entry ->
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
