package com.solvix.tabungan.pages.settings

import androidx.compose.foundation.background
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
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.solvix.tabungan.AppCard
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.LocalStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreModal(
  backup: FullAppBackup,
  onDismiss: () -> Unit,
  onConfirmRestore: (FullAppBackup) -> Unit,
  strings: AppStrings = LocalStrings.current,
) {
  val colors = LocalAppColors.current
  val dateText = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(backup.exportedAt))

  BasicAlertDialog(onDismissRequest = onDismiss) {
    AppCard(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp),
      shape = RoundedCornerShape(20.dp),
    ) {
      Column(modifier = Modifier.padding(6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = strings["backup_preview_title"].ifBlank { "Pratinjau Cadangan" },
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = colors.text,
          )
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(colors.accent.copy(alpha = 0.15f))
              .clickable(onClick = onDismiss)
              .padding(horizontal = 10.dp, vertical = 4.dp),
          ) {
            Text(
              text = strings["confirm_cancel"].ifBlank { "Batal" },
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = colors.accent,
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))
        val previewDateLabel = strings["backup_preview_date"].ifBlank { "File dicadangkan pada" }
        Text(
          text = "$previewDateLabel: $dateText",
          fontSize = 12.sp,
          color = colors.muted,
        )

        Spacer(modifier = Modifier.height(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          BackupItemRow(label = strings["backup_stat_income"].ifBlank { "Pemasukan" }, count = backup.income.size, icon = "💸")
          BackupItemRow(label = strings["backup_stat_expense"].ifBlank { "Pengeluaran" }, count = backup.expense.size, icon = "🧾")
          if (backup.transfers.isNotEmpty()) {
            BackupItemRow(label = strings["backup_stat_transfer"].ifBlank { "Transfer" }, count = backup.transfers.size, icon = "🔁")
          }
          BackupItemRow(label = strings["backup_stat_dreams"].ifBlank { "Target Impian" }, count = backup.dreams.size, icon = "🌟")
          BackupItemRow(label = strings["backup_stat_loans"].ifBlank { "Catatan Hutang" }, count = backup.loans.size, icon = "💳")
          if (backup.categoryBudgets.isNotEmpty()) {
            BackupItemRow(label = strings["backup_stat_budgets"].ifBlank { "Batas Anggaran" }, count = backup.categoryBudgets.size, icon = "📊")
          }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(colors.accent, colors.accent2)))
            .clickable {
              onConfirmRestore(backup)
              onDismiss()
            },
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = strings["backup_confirm_btn"].ifBlank { "Pulihkan Seluruh Data" },
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
          )
        }
      }
    }
  }
}

@Composable
private fun BackupItemRow(label: String, count: Int, icon: String) {
  val colors = LocalAppColors.current
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .background(colors.bg2)
      .padding(horizontal = 12.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
      Text(text = icon, fontSize = 15.sp)
      Text(text = label, fontSize = 13.sp, color = colors.text)
    }
    Text(
      text = "$count data",
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      color = colors.accent,
    )
  }
}
