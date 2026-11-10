package com.solvix.tabungan.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.solvix.tabungan.AppPinSecurityManager
import com.solvix.tabungan.HapticHelper
import com.solvix.tabungan.LocalAppColors
import kotlinx.coroutines.delay

/**
 * Layar kunci keamanan aplikasi otomatis dengan perlindungan PIN 6-digit & Biometrik.
 * Mendukung autentikasi hybrid: PIN numerik cadangan dan pemindai sidik jari.
 */
@Composable
fun AppLockScreen(
  isLocked: Boolean,
  canUseBiometrics: Boolean = true,
  onUnlockRequest: () -> Unit,
  onUnlockSuccess: () -> Unit,
  onLogoutRequest: () -> Unit = {},
) {
  val colors = LocalAppColors.current
  val context = LocalContext.current
  val hasPin = remember(isLocked, AppPinSecurityManager.versionState.intValue) {
    AppPinSecurityManager.isPinSet(context)
  }

  var enteredPin by remember { mutableStateOf("") }
  var isError by remember { mutableStateOf(false) }
  var showForgotPinDialog by remember { mutableStateOf(false) }

  // Reset PIN input saat status kunci berubah
  LaunchedEffect(isLocked) {
    if (isLocked) {
      enteredPin = ""
      isError = false
      if (canUseBiometrics) {
        onUnlockRequest()
      }
    }
  }

  // Verifikasi otomatis begitu 6 digit terisi
  LaunchedEffect(enteredPin) {
    if (enteredPin.length == 6) {
      if (hasPin) {
        val ok = AppPinSecurityManager.verifyPin(context, enteredPin)
        if (ok) {
          HapticHelper.vibrateSuccess(context)
          isError = false
          enteredPin = ""
          onUnlockSuccess()
        } else {
          HapticHelper.vibrateClick(context)
          isError = true
          delay(600)
          enteredPin = ""
        }
      } else {
        // Jika belum set PIN tapi lock kepicu, izinkan unlock setelah 6 digit atau biometrik
        onUnlockSuccess()
      }
    }
  }

  AnimatedVisibility(
    visible = isLocked,
    enter = fadeIn(),
    exit = fadeOut(),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(colors.bg)
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          enabled = true,
          onClick = {},
        ),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
      ) {
        // Ikon gembok terenkripsi
        Box(
          modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(colors.accent, colors.accent2))),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Rounded.Lock,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(32.dp),
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = if (hasPin) "Masukkan PIN Keamanan" else "Aplikasi Terkunci",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = colors.text,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = if (hasPin) {
            "Masukkan 6 digit PIN untuk membuka data finansial Anda"
          } else {
            "Gunakan sensor sidik jari perangkat untuk membuka aplikasi"
          },
          fontSize = 12.sp,
          color = colors.muted,
          textAlign = TextAlign.Center,
          lineHeight = 16.sp,
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (hasPin) {
          // Indikator 6 Dot PIN
          Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            repeat(6) { index ->
              val isFilled = index < enteredPin.length
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
                    color = if (isError) colors.danger else if (isFilled) colors.accent else colors.muted.copy(alpha = 0.35f),
                    shape = CircleShape,
                  )
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Teks feedback kesalahan
          Text(
            text = if (isError) "PIN salah, silakan coba lagi" else " ",
            fontSize = 12.sp,
            color = colors.danger,
            fontWeight = FontWeight.Medium,
          )

          Spacer(modifier = Modifier.height(18.dp))

          // Keypad Numerik Ergonomis (1-9, Biometrik, 0, Backspace)
          val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("BIO", "0", "DEL"),
          )

          Column(
            modifier = Modifier
              .fillMaxWidth(0.85f)
              .widthIn(max = 360.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            keys.forEach { rowKeys ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                rowKeys.forEach { key ->
                  Box(
                    modifier = Modifier
                      .size(62.dp)
                      .clip(CircleShape)
                      .background(
                        when (key) {
                          "BIO", "DEL" -> Color.Transparent
                          else -> colors.bg2.copy(alpha = 0.7f)
                        }
                      )
                      .border(
                        width = 1.dp,
                        color = when (key) {
                          "BIO", "DEL" -> Color.Transparent
                          else -> colors.text.copy(alpha = 0.08f)
                        },
                        shape = CircleShape,
                      )
                      .clickable {
                        HapticHelper.vibrateClick(context)
                        when (key) {
                          "DEL" -> {
                            if (enteredPin.isNotEmpty()) {
                              enteredPin = enteredPin.dropLast(1)
                              isError = false
                            }
                          }
                          "BIO" -> {
                            if (canUseBiometrics) {
                              onUnlockRequest()
                            }
                          }
                          else -> {
                            if (enteredPin.length < 6) {
                              enteredPin += key
                              isError = false
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
                        modifier = Modifier.size(24.dp),
                      )
                      "BIO" -> {
                        if (canUseBiometrics) {
                          Icon(
                            imageVector = Icons.Rounded.Fingerprint,
                            contentDescription = "Sidik Jari",
                            tint = colors.accent,
                            modifier = Modifier.size(30.dp),
                          )
                        }
                      }
                      else -> Text(
                        text = key,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text,
                      )
                    }
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Tombol Bantuan / Lupa PIN
          Text(
            text = "Lupa PIN Keamanan?",
            fontSize = 12.sp,
            color = colors.muted,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
              .clickable { showForgotPinDialog = true }
              .padding(8.dp),
          )
        } else {
          // Tampilan Fallback jika hanya biometrik
          Box(
            modifier = Modifier
              .size(72.dp)
              .clip(CircleShape)
              .background(colors.accent.copy(alpha = 0.15f))
              .clickable { onUnlockRequest() },
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Rounded.Fingerprint,
              contentDescription = "Buka Kunci",
              tint = colors.accent,
              modifier = Modifier.size(42.dp),
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "Ketuk untuk Memindai Sidik Jari",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.accent,
            modifier = Modifier.clickable { onUnlockRequest() },
          )
        }
      }
    }
  }

  // Dialog Informasi Lupa PIN
  if (showForgotPinDialog) {
    AppConfirmationDialog(
      title = "Lupa PIN Keamanan?",
      message = "Demi melindungi data finansial Anda, jika lupa PIN, Anda dapat keluar dari sesi ini dan login kembali menggunakan email/username dan kata sandi akun Anda.",
      confirmText = "Keluar Akun",
      cancelText = "Batal",
      isDestructive = true,
      onConfirm = {
        showForgotPinDialog = false
        onLogoutRequest()
      },
      onDismiss = { showForgotPinDialog = false },
    )
  }
}
