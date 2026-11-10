package com.solvix.tabungan.pages.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppCard
import com.solvix.tabungan.AppDimens
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.AppTextField
import com.solvix.tabungan.GhostButton
import com.solvix.tabungan.GradientButton
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.LocalStrings
import com.solvix.tabungan.formatRupiah
import kotlin.math.roundToLong

@Composable
fun SplitBillSection(
  onRecordToExpense: (Long) -> Unit,
  onToast: (String) -> Unit = {},
  strings: AppStrings = LocalStrings.current,
) {
  val context = LocalContext.current
  val colors = LocalAppColors.current

  var billAmountStr by remember { mutableStateOf("") }
  var taxPercent by remember { mutableDoubleStateOf(11.0) } // Default PPN 11%
  var peopleCount by remember { mutableIntStateOf(2) }
  var tipAmountStr by remember { mutableStateOf("") }

  val subtotal = billAmountStr.filter { it.isDigit() }.toLongOrNull() ?: 0L
  val taxAmount = ((subtotal * (taxPercent / 100.0))).roundToLong()
  val tip = tipAmountStr.filter { it.isDigit() }.toLongOrNull() ?: 0L
  val grandTotal = subtotal + taxAmount + tip
  val perPerson = if (peopleCount > 0) (grandTotal + peopleCount - 1) / peopleCount else 0L

  val quickTaxOptions = listOf(0.0, 10.0, 11.0, 12.0, 15.0)

  AppCard {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
      // Judul & Icon
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(colors.accent.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Rounded.Group,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(18.dp),
          )
        }
        Column {
          Text(
            text = strings["split_bill_title"].ifBlank { "Bagi Tagihan" },
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = colors.text,
          )
          Text(
            text = strings["split_bill_subtitle"].ifBlank { "Hitung patungan makan/nongkrong bersama teman secara adil" },
            fontSize = 11.sp,
            color = colors.muted,
          )
        }
      }

      // Input Total Tagihan
      AppTextField(
        label = strings["split_bill_amount_label"].ifBlank { "Total Tagihan" },
        value = billAmountStr,
        onValueChange = { input -> billAmountStr = input.filter { it.isDigit() } },
        placeholder = "Contoh: 150000",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      )

      // Pajak & Layanan (Tax & Service)
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = strings["split_bill_tax_label"].ifBlank { "Pajak / Layanan" },
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = colors.muted,
          )
          Text(
            text = "${taxPercent.toInt()}% (${formatRupiah(taxAmount)})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = colors.accent,
          )
        }

        // Chips Persentase Pajak
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          quickTaxOptions.forEach { pct ->
            val isSelected = taxPercent == pct
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) colors.accent else colors.bg2)
                .border(1.dp, if (isSelected) colors.accent else colors.cardBorder, RoundedCornerShape(16.dp))
                .clickable { taxPercent = pct }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
              Text(
                text = "${pct.toInt()}%",
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else colors.text,
              )
            }
          }
        }
      }

      // Jumlah Orang (Counter)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = strings["split_bill_people_label"].ifBlank { "Jumlah Orang Patungan" },
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = colors.text,
          )
          Text(
            text = "$peopleCount orang",
            fontSize = 11.sp,
            color = colors.muted,
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(colors.bg2)
              .border(1.dp, colors.cardBorder, CircleShape)
              .clickable { if (peopleCount > 1) peopleCount-- },
            contentAlignment = Alignment.Center,
          ) {
            Icon(imageVector = Icons.Rounded.Remove, contentDescription = "Kurang", tint = colors.text, modifier = Modifier.size(16.dp))
          }

          Text(
            text = peopleCount.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = colors.text,
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.Center,
          )

          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(colors.accent.copy(alpha = 0.15f))
              .clickable { if (peopleCount < 50) peopleCount++ },
            contentAlignment = Alignment.Center,
          ) {
            Icon(imageVector = Icons.Rounded.Add, contentDescription = "Tambah", tint = colors.accent, modifier = Modifier.size(16.dp))
          }
        }
      }

      // Tip Opsional
      AppTextField(
        label = strings["split_bill_tip_label"].ifBlank { "Tip Sukarela (Opsional)" },
        value = tipAmountStr,
        onValueChange = { input -> tipAmountStr = input.filter { it.isDigit() } },
        placeholder = "Contoh: 10000",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      )

      Spacer(modifier = Modifier.height(2.dp))

      // Hasil Rincian Pembagian
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(AppDimens.radiusMd))
          .background(colors.accent.copy(alpha = 0.12f))
          .border(1.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(AppDimens.radiusMd))
          .padding(14.dp),
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(text = strings["split_bill_amount_label"].ifBlank { "Total Akhir Tagihan" }, fontSize = 12.sp, color = colors.muted)
            Text(text = formatRupiah(grandTotal), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.text)
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(text = strings["split_bill_people_label"].ifBlank { "Jumlah Orang" }, fontSize = 12.sp, color = colors.muted)
            Text(text = "$peopleCount ${if (strings["split_bill_person_unit"].isBlank()) "Orang" else strings["split_bill_person_unit"]}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(1.dp)
              .background(colors.cardBorder)
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = strings["split_bill_per_person"].ifBlank { "Nominal Per Orang" },
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.accent,
              )
              Text(
                text = strings["split_bill_per_person_desc"].ifBlank { "Yang harus dibayar masing-masing" },
                fontSize = 10.sp,
                color = colors.muted,
              )
            }
            Text(
              text = formatRupiah(perPerson),
              fontSize = 18.sp,
              fontWeight = FontWeight.ExtraBold,
              color = colors.accent,
            )
          }
        }
      }

      // Tombol Aksi: Salin ke WhatsApp & Catat ke Pengeluaran
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        GhostButton(
          text = strings["split_bill_copy"].ifBlank { "📋 Salin Info" },
          modifier = Modifier.weight(1f),
          onClick = {
            val textToCopy = """
              🧾 *Rincian Split Bill Tabunganku*
              Subtotal: ${formatRupiah(subtotal)}
              Pajak (${taxPercent.toInt()}%): ${formatRupiah(taxAmount)}
              Tip: ${formatRupiah(tip)}
              *Total: ${formatRupiah(grandTotal)}*
              Dibagi: $peopleCount orang
              👉 *Per Orang: ${formatRupiah(perPerson)}*
            """.trimIndent()
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Split Bill", textToCopy))
            onToast(strings["split_bill_copy_success"].ifBlank { "Rincian patungan berhasil disalin!" })
          },
        )

        GradientButton(
          text = strings["split_bill_record"].ifBlank { "📥 Catat Bagian Saya" },
          modifier = Modifier.weight(1f),
          enabled = perPerson > 0L,
          onClick = {
            onRecordToExpense(perPerson)
          },
        )
      }
    }
  }
}
