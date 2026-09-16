package com.solvix.tabungan.pages.transfer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppCard
import com.solvix.tabungan.AppDropdown
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.AppTextField
import com.solvix.tabungan.DateField
import com.solvix.tabungan.GradientButton
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.parseAmount

@Composable
fun TransferFormCard(
  fromAccount: String,
  onFromAccountChange: (String) -> Unit,
  toAccount: String,
  onToAccountChange: (String) -> Unit,
  amount: String,
  onAmountChange: (String) -> Unit,
  adminFee: String,
  onAdminFeeChange: (String) -> Unit,
  date: String,
  onDateChange: (String) -> Unit,
  note: String,
  onNoteChange: (String) -> Unit,
  accountOptions: List<String>,
  isEditing: Boolean,
  onCancelEdit: () -> Unit,
  onSubmit: () -> Unit,
  strings: AppStrings,
  customFromAccount: String = "",
  onCustomFromAccountChange: (String) -> Unit = {},
  customToAccount: String = "",
  onCustomToAccountChange: (String) -> Unit = {},
) {
  val colors = LocalAppColors.current
  val amountValue = parseAmount(amount)
  val isFromValid = fromAccount.isNotBlank() && (fromAccount != "Lainnya" || customFromAccount.isNotBlank())
  val isToValid = toAccount.isNotBlank() && (toAccount != "Lainnya" || customToAccount.isNotBlank())
  val isValid = isFromValid &&
    isToValid &&
    amountValue > 0 &&
    date.isNotBlank()

  AppCard {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text(
        text = "🔁 " + strings["transfer_card_title"].ifBlank { "Pindah Dana Antar Rekening / Dompet" },
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = colors.text,
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          AppDropdown(
            label = strings["transfer_from_label"].ifBlank { "Dari Akun / Dompet" },
            placeholder = strings["transfer_from_placeholder"].ifBlank { "Pilih Asal" },
            options = accountOptions,
            selected = fromAccount,
            onSelected = onFromAccountChange,
          )
        }
        Text(text = "➔", fontSize = 16.sp, color = colors.accent, modifier = Modifier.padding(top = 18.dp))
        Column(modifier = Modifier.weight(1f)) {
          AppDropdown(
            label = strings["transfer_to_label"].ifBlank { "Ke Akun / Dompet" },
            placeholder = strings["transfer_to_placeholder"].ifBlank { "Pilih Tujuan" },
            options = accountOptions,
            selected = toAccount,
            onSelected = onToAccountChange,
          )
        }
      }

      if (fromAccount == "Lainnya") {
        AppTextField(
          label = strings["transfer_manual_from_label"].ifBlank { "Nama Rekening Asal Baru (Manual)" },
          value = customFromAccount,
          onValueChange = onCustomFromAccountChange,
          placeholder = strings["transfer_manual_placeholder"].ifBlank { "Contoh: Bibit, Indodax, Bank Jatim" },
        )
      }

      if (toAccount == "Lainnya") {
        AppTextField(
          label = strings["transfer_manual_to_label"].ifBlank { "Nama Rekening Tujuan Baru (Manual)" },
          value = customToAccount,
          onValueChange = onCustomToAccountChange,
          placeholder = strings["transfer_manual_placeholder"].ifBlank { "Contoh: Bibit, Indodax, Bank Jatim" },
        )
      }

      AppTextField(
        label = strings["transfer_amount_label"].ifBlank { "Nominal Transfer" },
        value = amount,
        onValueChange = onAmountChange,
        placeholder = "0",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      )

      AppTextField(
        label = strings["transfer_admin_fee_label"].ifBlank { "Biaya Admin (Opsional)" },
        value = adminFee,
        onValueChange = onAdminFeeChange,
        placeholder = "0",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      )

      DateField(
        label = strings["transfer_date_label"].ifBlank { "Tanggal Transfer" },
        value = date,
        onValueChange = onDateChange,
        placeholder = "dd-MM-yyyy",
      )

      AppTextField(
        label = strings["transfer_note_label"].ifBlank { "Catatan (Opsional)" },
        value = note,
        onValueChange = onNoteChange,
        placeholder = strings["transfer_note_placeholder"].ifBlank { "Contoh: Top-up saldo GoPay dari BCA" },
      )

      Spacer(modifier = Modifier.height(4.dp))

      GradientButton(
        text = if (isEditing) {
          strings["transfer_update_btn"].ifBlank { "Perbarui Transfer" }
        } else {
          strings["transfer_save_btn"].ifBlank { "Simpan Transaksi Transfer" }
        },
        enabled = isValid,
        onClick = onSubmit,
      )
    }
  }
}
