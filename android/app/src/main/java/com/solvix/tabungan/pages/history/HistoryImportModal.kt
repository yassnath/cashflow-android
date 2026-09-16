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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun HistoryImportModal(
  parsedEntries: List<MoneyEntry>,
  failedCount: Int,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
  strings: AppStrings = LocalStrings.current,
) {
  val colors = LocalAppColors.current
  val incomeCount = parsedEntries.count { it.type == EntryType.Income }
  val expenseCount = parsedEntries.count { it.type == EntryType.Expense }
  val totalIncome = parsedEntries.filter { it.type == EntryType.Income }.sumOf { it.amount }
  val totalExpense = parsedEntries.filter { it.type == EntryType.Expense }.sumOf { it.amount }

  Dialog(onDismissRequest = onDismiss) {
    Box(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .clip(RoundedCornerShape(20.dp))
        .background(colors.card)
        .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
        .padding(18.dp),
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = strings["history_import_title"].ifBlank { "📥 Konfirmasi Impor Riwayat" },
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = colors.text,
        )
        Text(
          text = try {
            String.format(strings["history_import_subtitle"].ifBlank { "Ditemukan %d transaksi yang siap ditambahkan ke riwayat Anda." }, parsedEntries.size)
          } catch (_: Exception) {
            "Ditemukan ${parsedEntries.size} transaksi yang siap ditambahkan ke riwayat Anda."
          },
          fontSize = 12.sp,
          color = colors.muted,
        )

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.bg2)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(text = "${strings["entry_type_income"]} ($incomeCount):", fontSize = 11.sp, color = colors.muted)
              Text(text = "+${formatRupiah(totalIncome)}", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(text = "${strings["entry_type_expense"]} ($expenseCount):", fontSize = 11.sp, color = colors.muted)
              Text(text = "-${formatRupiah(totalExpense)}", fontSize = 11.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
            }
            if (failedCount > 0) {
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = try {
                  String.format(strings["history_import_failed_note"].ifBlank { "⚠️ %d baris dilewati karena format tidak valid." }, failedCount)
                } catch (_: Exception) {
                  "⚠️ $failedCount baris dilewati karena format tidak valid."
                },
                fontSize = 10.sp,
                color = colors.muted,
              )
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .height(42.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(colors.bg2)
              .border(1.dp, colors.cardBorder, RoundedCornerShape(10.dp))
              .clickable { onDismiss() },
            contentAlignment = Alignment.Center,
          ) {
            Text(text = strings["confirm_cancel"].ifBlank { "Batal" }, color = colors.muted, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }

          Box(
            modifier = Modifier
              .weight(1.5f)
              .height(42.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(Brush.linearGradient(listOf(colors.accent, colors.accent2)))
              .clickable {
                onConfirm()
                onDismiss()
              },
            contentAlignment = Alignment.Center,
          ) {
            Text(text = strings["history_import_confirm"].ifBlank { "Impor Sekarang" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }
    }
  }
}
