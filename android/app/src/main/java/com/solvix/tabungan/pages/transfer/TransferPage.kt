package com.solvix.tabungan.pages.transfer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.CustomAccountManager
import com.solvix.tabungan.EntryType
import com.solvix.tabungan.HapticHelper
import com.solvix.tabungan.LocalThemeName
import com.solvix.tabungan.MoneyEntry
import com.solvix.tabungan.Page
import com.solvix.tabungan.SectionTitle
import com.solvix.tabungan.UUIDString
import com.solvix.tabungan.ensureDateHasTime
import com.solvix.tabungan.formatRupiah
import com.solvix.tabungan.nowJakartaText
import com.solvix.tabungan.nowJakartaTime
import com.solvix.tabungan.parseAmount
import com.solvix.tabungan.themePageVisualIcon

@Composable
fun TransferPage(
  onSave: (MoneyEntry) -> Unit,
  onUpdate: (MoneyEntry) -> Unit,
  editEntry: MoneyEntry?,
  onEditConsumed: () -> Unit,
  strings: AppStrings,
) {
  val context = LocalContext.current
  var fromAccount by rememberSaveable { mutableStateOf("SeaBank") }
  var toAccount by rememberSaveable { mutableStateOf("GoPay") }
  var customFromAccount by rememberSaveable { mutableStateOf("") }
  var customToAccount by rememberSaveable { mutableStateOf("") }
  var amount by rememberSaveable { mutableStateOf("") }
  var adminFee by rememberSaveable { mutableStateOf("") }
  var date by rememberSaveable { mutableStateOf("") }
  var note by rememberSaveable { mutableStateOf("") }
  var editingId by rememberSaveable { mutableStateOf<String?>(null) }
  var editingCreatedAt by rememberSaveable { mutableStateOf<String?>(null) }

  var customAccountsList by remember { mutableStateOf(CustomAccountManager.getCustomAccounts(context)) }

  val defaultAccounts = listOf(
    "SeaBank", "Crypto", "Saham", "BCA", "Mandiri", "BRI", "BNI", "BSI", "Jago", "Jenius",
    "GoPay", "OVO", "DANA", "ShopeePay", "Dompet Tunai",
  )

  val accountOptions = remember(defaultAccounts, customAccountsList) {
    (defaultAccounts.filter { it != "Lainnya" } + customAccountsList + listOf("Lainnya")).distinct()
  }

  LaunchedEffect(editEntry?.id) {
    val entry = editEntry
    if (entry != null && entry.type == EntryType.Transfer) {
      editingId = entry.id
      fromAccount = entry.sourceOrMethod
      toAccount = entry.channelOrBank
      amount = entry.amount.toString()
      date = entry.date
      note = entry.note
      editingCreatedAt = entry.createdAt
      onEditConsumed()
    }
  }

  Column(modifier = Modifier.imePadding()) {
    SectionTitle(
      icon = themePageVisualIcon(LocalThemeName.current, Page.Transfer),
      title = strings["transfer_header_title"].ifBlank { "Transfer Antar Akun" },
      subtitle = strings["transfer_header_subtitle"].ifBlank { "Pindah dana antar rekening dan e-wallet tanpa mengubah total kekayaan" },
    )

    Spacer(modifier = Modifier.height(10.dp))

    TransferFormCard(
      fromAccount = fromAccount,
      onFromAccountChange = { fromAccount = it },
      toAccount = toAccount,
      onToAccountChange = { toAccount = it },
      amount = amount,
      onAmountChange = { amount = it },
      adminFee = adminFee,
      onAdminFeeChange = { adminFee = it },
      date = date,
      onDateChange = { date = it },
      note = note,
      onNoteChange = { note = it },
      accountOptions = accountOptions,
      isEditing = editingId != null,
      onCancelEdit = {
        editingId = null
        amount = ""
        adminFee = ""
        note = ""
        customFromAccount = ""
        customToAccount = ""
      },
      onSubmit = {
        val amountValue = parseAmount(amount)
        if (amountValue <= 0) return@TransferFormCard

        val finalFrom = if (fromAccount == "Lainnya" && customFromAccount.isNotBlank()) {
          val trimmed = customFromAccount.trim()
          CustomAccountManager.addCustomAccount(context, trimmed)
          customAccountsList = CustomAccountManager.getCustomAccounts(context)
          trimmed
        } else {
          fromAccount
        }

        val finalTo = if (toAccount == "Lainnya" && customToAccount.isNotBlank()) {
          val trimmed = customToAccount.trim()
          CustomAccountManager.addCustomAccount(context, trimmed)
          customAccountsList = CustomAccountManager.getCustomAccounts(context)
          trimmed
        } else {
          toAccount
        }

        val feeValue = parseAmount(adminFee)
        val noteContent = when {
          note.isNotBlank() && feeValue > 0 -> "$note (Biaya admin: Rp ${formatRupiah(feeValue)})"
          feeValue > 0 -> "Biaya admin transfer: Rp ${formatRupiah(feeValue)}"
          else -> note
        }

        val entry = MoneyEntry(
          id = editingId ?: UUIDString(),
          type = EntryType.Transfer,
          amount = amountValue,
          date = ensureDateHasTime(date, nowJakartaTime()),
          category = "Transfer Saldo",
          note = noteContent,
          sourceOrMethod = finalFrom,
          channelOrBank = finalTo,
          createdAt = editingCreatedAt ?: nowJakartaText(),
        )

        if (editingId == null) {
          onSave(entry)
        } else {
          onUpdate(entry)
        }
        HapticHelper.vibrateSuccess(context)

        amount = ""
        adminFee = ""
        note = ""
        customFromAccount = ""
        customToAccount = ""
        editingId = null
        editingCreatedAt = null
      },
      strings = strings,
      customFromAccount = customFromAccount,
      onCustomFromAccountChange = { customFromAccount = it },
      customToAccount = customToAccount,
      onCustomToAccountChange = { customToAccount = it },
    )
  }
}
