package com.solvix.tabungan.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.solvix.tabungan.AppCard
import com.solvix.tabungan.AppPinSecurityManager
import com.solvix.tabungan.DangerButton
import com.solvix.tabungan.GradientButton
import com.solvix.tabungan.GhostButton
import com.solvix.tabungan.LocalAppColors

/**
 * Modal Popup Pengaturan Keamanan PIN 6-Digit yang ergonomis, bersih, dan informatif.
 * Menghindari penempatan tombol yang berdesakan di kartu pengaturan, serta memberikan
 * feedback visual yang jelas bagi anak muda maupun orang tua.
 */
@Composable
fun PinSecuritySettingsModal(
  onDismiss: () -> Unit,
  onOpenPinSetup: () -> Unit,
  onPinRemoved: () -> Unit,
  userId: String,
) {
  val context = LocalContext.current
  val colors = LocalAppColors.current
  val isPinActive = remember(AppPinSecurityManager.versionState.intValue) {
    AppPinSecurityManager.isPinSet(context)
  }
  var showRemoveConfirm by remember { mutableStateOf(false) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .widthIn(max = 480.dp)
        .padding(vertical = 20.dp),
      contentAlignment = Alignment.Center,
    ) {
      AppCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          // Top Bar: Judul & Tombol Tutup
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(colors.accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Rounded.Security,
                  contentDescription = null,
                  tint = colors.accent,
                  modifier = Modifier.size(20.dp),
                )
              }
              Text(
                text = "Kunci PIN 6-Digit",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = colors.text,
              )
            }

            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(colors.bg2)
                .clickable { onDismiss() },
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Tutup",
                tint = colors.muted,
                modifier = Modifier.size(18.dp),
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Hero Icon & Status Banner
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(
                if (isPinActive) {
                  Brush.linearGradient(listOf(Color(0xFF2EC4B6), Color(0xFF209E91)))
                } else {
                  Brush.linearGradient(listOf(colors.accent, colors.accent2))
                }
              ),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = if (isPinActive) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(32.dp),
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Badge Status
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(
                if (isPinActive) Color(0xFF2EC4B6).copy(alpha = 0.15f)
                else colors.muted.copy(alpha = 0.15f)
              )
              .border(
                1.dp,
                if (isPinActive) Color(0xFF2EC4B6).copy(alpha = 0.4f)
                else colors.muted.copy(alpha = 0.3f),
                RoundedCornerShape(20.dp),
              )
              .padding(horizontal = 14.dp, vertical = 6.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = if (isPinActive) Icons.Rounded.CheckCircle else Icons.Rounded.Key,
                contentDescription = null,
                tint = if (isPinActive) Color(0xFF2EC4B6) else colors.muted,
                modifier = Modifier.size(14.dp),
              )
              Text(
                text = if (isPinActive) "PIN AKTIF & TERLINDUNGI" else "PIN BELUM DIAKTIFKAN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPinActive) Color(0xFF2EC4B6) else colors.muted,
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Deskripsi Edukatif
          Text(
            text = if (isPinActive) {
              "Aplikasi CashFlow Anda aman terkunci. PIN diperlukan saat aplikasi dibuka atau setelah waktu kunci otomatis tercapai."
            } else {
              "Lindungi privasi transaksi, buku rekening, dan catatan keuangan Anda dari akses orang lain dengan kode PIN 6-angka."
            },
            fontSize = 13.sp,
            color = colors.text.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 8.dp),
          )

          Spacer(modifier = Modifier.height(20.dp))

          // Action Buttons
          if (isPinActive) {
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              GradientButton(
                text = "Ubah PIN 6-Digit",
                onClick = {
                  onDismiss()
                  onOpenPinSetup()
                },
              )

              DangerButton(
                text = "Hapus / Nonaktifkan PIN",
                fillMaxWidth = true,
                onClick = { showRemoveConfirm = true },
              )

              GhostButton(
                text = "Tutup",
                fillMaxWidth = true,
                onClick = onDismiss,
              )
            }
          } else {
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              GradientButton(
                text = "Atur PIN 6-Digit Sekarang",
                onClick = {
                  onDismiss()
                  onOpenPinSetup()
                },
              )

              GhostButton(
                text = "Nanti Saja",
                fillMaxWidth = true,
                onClick = onDismiss,
              )
            }
          }
        }
      }
    }
  }

  // Dialog Konfirmasi Hapus PIN
  if (showRemoveConfirm) {
    Dialog(
      onDismissRequest = { showRemoveConfirm = false },
      properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(0.88f)
          .padding(16.dp),
        contentAlignment = Alignment.Center,
      ) {
        AppCard(shape = RoundedCornerShape(20.dp)) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
              text = "⚠️",
              fontSize = 32.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Hapus Kunci PIN?",
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp,
              color = colors.text,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Setelah dihapus, aplikasi tidak akan meminta PIN saat dibuka. Anda dapat mengaktifkannya kembali kapan saja.",
              fontSize = 12.sp,
              color = colors.muted,
              textAlign = TextAlign.Center,
              lineHeight = 16.sp,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(12.dp))
                  .background(colors.bg2)
                  .clickable { showRemoveConfirm = false }
                  .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
              ) {
                Text(text = "Batal", color = colors.text, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
              }

              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(12.dp))
                  .background(colors.danger)
                  .clickable {
                    showRemoveConfirm = false
                    AppPinSecurityManager.removePin(context, userId)
                    onPinRemoved()
                    onDismiss()
                  }
                  .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
              ) {
                Text(text = "Ya, Hapus", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }
            }
          }
        }
      }
    }
  }
}

