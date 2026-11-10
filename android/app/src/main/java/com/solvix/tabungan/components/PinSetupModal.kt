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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.solvix.tabungan.HapticHelper
import com.solvix.tabungan.LocalAppColors
import kotlinx.coroutines.delay

/**
 * Dialog Keypad Pembuatan & Konfirmasi PIN 6-Digit yang ramah pengguna dari anak muda hingga lansia:
 * - Ukuran tombol angka besar (touch target 54-60dp)
 * - Teks angka kontras tinggi (22sp)
 * - Dot indikator 6 digit beranimasi & berwarna jelas
 * - Validasi 2 langkah otomatis dengan haptic feedback
 */
@Composable
fun PinSetupModal(
  onDismiss: () -> Unit,
  onPinSaved: (String) -> Unit,
) {
  val colors = LocalAppColors.current
  val context = LocalContext.current

  var step by remember { mutableIntStateOf(1) } // 1: Buat, 2: Konfirmasi
  var firstPin by remember { mutableStateOf("") }
  var confirmPin by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf("") }

  val currentPin = if (step == 1) firstPin else confirmPin

  LaunchedEffect(firstPin) {
    if (firstPin.length == 6) {
      delay(200)
      step = 2
      errorMessage = ""
    }
  }

  LaunchedEffect(confirmPin) {
    if (confirmPin.length == 6) {
      if (confirmPin == firstPin) {
        HapticHelper.vibrateSuccess(context)
        onPinSaved(confirmPin)
        onDismiss()
      } else {
        HapticHelper.vibrateClick(context)
        errorMessage = "PIN tidak cocok. Silakan ulangi konfirmasi."
        delay(700)
        confirmPin = ""
      }
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .widthIn(max = 480.dp)
        .imePadding()
        .padding(vertical = 16.dp),
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
          // Top Header: Step Indicator & Close Button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(colors.accent.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
              Text(
                text = if (step == 1) "Langkah 1/2: Buat PIN" else "Langkah 2/2: Konfirmasi",
                fontSize = 11.sp,
                color = colors.accent,
                fontWeight = FontWeight.Bold,
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

          Spacer(modifier = Modifier.height(14.dp))

          // Lock Icon Badge
          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(Brush.linearGradient(listOf(colors.accent, colors.accent2))),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = if (step == 1) Icons.Rounded.Lock else Icons.Rounded.Shield,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(28.dp),
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = if (step == 1) "Buat PIN 6-Digit Baru" else "Konfirmasi PIN Anda",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colors.text,
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = if (step == 1) {
              "Masukkan 6 angka yang mudah diingat untuk mengunci aplikasi"
            } else {
              "Masukkan kembali 6 angka yang sama untuk verifikasi"
            },
            fontSize = 12.sp,
            color = colors.muted,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            modifier = Modifier.padding(horizontal = 12.dp),
          )

          Spacer(modifier = Modifier.height(18.dp))

          // 6 PIN Dots Indicator (Ergonomis & Kontras Tinggi)
          Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            repeat(6) { index ->
              val isFilled = index < currentPin.length
              val isError = errorMessage.isNotBlank()
              Box(
                modifier = Modifier
                  .size(16.dp)
                  .clip(CircleShape)
                  .background(
                    when {
                      isError -> colors.danger
                      isFilled -> colors.accent
                      else -> colors.bg2
                    }
                  )
                  .border(
                    width = 1.5.dp,
                    color = when {
                      isError -> colors.danger
                      isFilled -> colors.accent
                      else -> colors.muted.copy(alpha = 0.4f)
                    },
                    shape = CircleShape,
                  )
              )
            }
          }

          // Error Message or Spacer
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(28.dp),
            contentAlignment = Alignment.Center,
          ) {
            if (errorMessage.isNotEmpty()) {
              Text(
                text = errorMessage,
                fontSize = 12.sp,
                color = colors.danger,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
              )
            }
          }

          // Keypad Numerik Ergonomis (Besar, Nyaman untuk Anak Muda & Orang Tua)
          val keypadRows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("RESET", "0", "DEL"),
          )

          Column(
            modifier = Modifier.fillMaxWidth(0.92f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            keypadRows.forEach { rowKeys ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                rowKeys.forEach { key ->
                  val isSpecial = key == "RESET" || key == "DEL"
                  Box(
                    modifier = Modifier
                      .size(56.dp)
                      .clip(CircleShape)
                      .background(
                        if (isSpecial) colors.bg2.copy(alpha = 0.5f)
                        else colors.bg2
                      )
                      .clickable {
                        HapticHelper.vibrateClick(context)
                        when (key) {
                          "DEL" -> {
                            if (step == 1 && firstPin.isNotEmpty()) {
                              firstPin = firstPin.dropLast(1)
                              errorMessage = ""
                            } else if (step == 2 && confirmPin.isNotEmpty()) {
                              confirmPin = confirmPin.dropLast(1)
                              errorMessage = ""
                            }
                          }
                          "RESET" -> {
                            step = 1
                            firstPin = ""
                            confirmPin = ""
                            errorMessage = ""
                          }
                          else -> {
                            if (step == 1 && firstPin.length < 6) {
                              firstPin += key
                              errorMessage = ""
                            } else if (step == 2 && confirmPin.length < 6) {
                              confirmPin += key
                              errorMessage = ""
                            }
                          }
                        }
                      },
                    contentAlignment = Alignment.Center,
                  ) {
                    when (key) {
                      "DEL" -> Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Backspace,
                        contentDescription = "Hapus",
                        tint = colors.text,
                        modifier = Modifier.size(22.dp),
                      )
                      "RESET" -> Text(
                        text = "Ulang",
                        fontSize = 12.sp,
                        color = colors.muted,
                        fontWeight = FontWeight.Bold,
                      )
                      else -> Text(
                        text = key,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text,
                      )
                    }
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
        }
      }
    }
  }
}
