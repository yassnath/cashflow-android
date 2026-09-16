package com.solvix.tabungan.pages.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppCard
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.LocalStrings

@Composable
fun BackupSettingsCard(
  onExportBackup: () -> Unit,
  onRestoreBackup: (FullAppBackup) -> Unit,
  onToast: (String) -> Unit,
  strings: AppStrings = LocalStrings.current,
) {
  val context = LocalContext.current
  val colors = LocalAppColors.current
  var pendingRestoreBackup by remember { mutableStateOf<FullAppBackup?>(null) }

  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
  ) { uri: Uri? ->
    if (uri != null) {
      val parsed = FullBackupHelper.readBackupFromUri(context, uri)
      if (parsed != null) {
        pendingRestoreBackup = parsed
      } else {
        onToast(strings["backup_read_failed"].ifBlank { "Gagal membaca file cadangan. Format tidak sesuai." })
      }
    }
  }

  AppCard {
    Text(
      text = strings["backup_settings_title"].ifBlank { "Cadangkan & Pulihkan Data" },
      fontWeight = FontWeight.Bold,
      fontSize = 16.sp,
      color = colors.text,
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = strings["backup_settings_subtitle"].ifBlank {
        "Simpan seluruh data keuangan (transaksi, transfer, impian, hutang, dan anggaran) ke satu file .json utuh untuk diamankan atau dipindahkan ke perangkat lain."
      },
      color = colors.muted,
      fontSize = 12.sp,
    )
    Spacer(modifier = Modifier.height(14.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      // Tombol Buat Cadangan
      Box(
        modifier = Modifier
          .weight(1f)
          .height(42.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(Brush.linearGradient(listOf(colors.accent, colors.accent2)))
          .clickable { onExportBackup() },
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "📤 " + strings["backup_create_btn"].ifBlank { "Cadangkan" },
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
        )
      }

      // Tombol Pulihkan
      Box(
        modifier = Modifier
          .weight(1f)
          .height(42.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(colors.card)
          .border(1.dp, colors.accent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
          .clickable { filePickerLauncher.launch("application/json") },
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "📥 " + strings["backup_restore_btn"].ifBlank { "Pulihkan" },
          color = colors.accent,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
        )
      }
    }
  }

  pendingRestoreBackup?.let { backup ->
    BackupRestoreModal(
      backup = backup,
      onDismiss = { pendingRestoreBackup = null },
      onConfirmRestore = { confirmed ->
        onRestoreBackup(confirmed)
        onToast(strings["backup_confirm_btn"].ifBlank { "Data berhasil dipulihkan!" })
      },
      strings = strings,
    )
  }
}
