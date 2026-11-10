package com.solvix.tabungan.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.GradientButton
import com.solvix.tabungan.LocalAppColors

/**
 * Layar kunci biometrik otomatis saat aplikasi kembali dari latar belakang (Background).
 * Menjamin privasi saldo dan catatan transaksi tidak terlihat jika ponsel dipinjam orang lain.
 */
@Composable
fun AppLockScreen(
  isLocked: Boolean,
  onUnlockRequest: () -> Unit,
) {
  val colors = LocalAppColors.current

  LaunchedEffect(isLocked) {
    if (isLocked) {
      onUnlockRequest()
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
        .clickable(enabled = true, onClick = {}),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(colors.accent, colors.accent2))),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Rounded.Lock,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(36.dp),
          )
        }

        Text(
          text = "Aplikasi Terkunci",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = colors.text,
        )

        Text(
          text = "Data finansial Anda dilindungi. Gunakan sidik jari atau biometrik perangkat untuk membuka.",
          fontSize = 13.sp,
          color = colors.muted,
          textAlign = TextAlign.Center,
          lineHeight = 18.sp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        GradientButton(
          text = "Buka dengan Sidik Jari 👆",
          modifier = Modifier.fillMaxWidth(),
          onClick = onUnlockRequest,
        )
      }
    }
  }
}
