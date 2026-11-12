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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

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

  val configuration = LocalConfiguration.current
  val isSmallScreen = configuration.screenWidthDp < 360

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(if (isSmallScreen) 0.98f else 0.92f)
        .widthIn(max = 480.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(colors.card)
        .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
        .padding(if (isSmallScreen) 14.dp else 18.dp),
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(colors.accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Rounded.FileDownload,
              contentDescription = null,
              tint = colors.accent,
              modifier = Modifier.size(20.dp),
            )
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = strings["history_import_title"].ifBlank { "Konfirmasi Impor Riwayat" }.replace("📥 ", ""),
              fontSize = if (isSmallScreen) 14.sp else 16.sp,
              fontWeight = FontWeight.Bold,
              color = colors.text,
            )
            Text(
              text = try {
                String.format(strings["history_import_subtitle"].ifBlank { "Ditemukan %d transaksi yang siap ditambahkan." }, parsedEntries.size)
              } catch (_: Exception) {
                "Ditemukan ${parsedEntries.size} transaksi yang siap ditambahkan."
              },
              fontSize = 11.sp,
              color = colors.muted,
            )
          }
        }

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
              .heightIn(min = 44.dp)
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
              .heightIn(min = 44.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(Brush.linearGradient(listOf(colors.accent, colors.accent2)))
              .clickable {
                onConfirm()
                onDismiss()
              },
            contentAlignment = Alignment.Center,
          ) {
            Text(text = strings["history_import_confirm"].ifBlank { "Impor Sekarang" }, color = colors.onGradient, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }
    }
  }
}
