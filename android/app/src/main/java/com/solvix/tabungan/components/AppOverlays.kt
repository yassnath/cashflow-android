package com.solvix.tabungan.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.DangerButton
import com.solvix.tabungan.GhostButton
import com.solvix.tabungan.GradientButton
import com.solvix.tabungan.LoadingLogo
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.LogoCircle
import com.solvix.tabungan.ModalCard
import com.solvix.tabungan.ModalOverlay
import com.solvix.tabungan.OutlineButton

@Composable
fun SplashModal(
  strings: AppStrings,
  fingerprintEnabled: Boolean,
  biometricAllowed: Boolean,
  canUseFingerprint: Boolean,
  onDismiss: () -> Unit,
  onPrimaryClick: () -> Unit,
) {
  val colors = LocalAppColors.current
  ModalOverlay {
    ModalCard(
      modifier = Modifier.fillMaxWidth(0.92f),
    ) {
      Text(
        text = "✕",
        color = colors.muted,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier
          .align(Alignment.End)
          .clickable { onDismiss() }
          .padding(4.dp),
      )
      Spacer(modifier = Modifier.height(4.dp))
      LogoCircle()
      Spacer(modifier = Modifier.height(12.dp))
      Text(text = strings["welcome_title"], fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.text)
      Spacer(modifier = Modifier.height(8.dp))
      Text(text = strings["welcome_subtitle"], color = colors.muted, fontSize = 13.sp)
      Spacer(modifier = Modifier.height(16.dp))
      GradientButton(text = strings["welcome_action"]) {
        onPrimaryClick()
      }
    }
  }
}

@Composable
fun LoadingOverlay(
  showLoading: Boolean,
  loadingAlpha: Float,
) {
  AnimatedVisibility(
    visible = showLoading,
    enter = fadeIn(tween(600)),
    exit = fadeOut(tween(600)),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
        .alpha(loadingAlpha),
      contentAlignment = Alignment.Center,
    ) {
      LoadingLogo()
    }
  }
}

@Composable
fun ConfirmModal(
  strings: AppStrings,
  confirmMessage: String,
  onCancel: () -> Unit,
  onConfirm: () -> Unit,
) {
  val colors = LocalAppColors.current
  ModalOverlay {
    ModalCard {
      Text(text = strings["confirm_title"], fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.text)
      Spacer(modifier = Modifier.height(8.dp))
      Text(text = confirmMessage, color = colors.muted, fontSize = 13.sp)
      Spacer(modifier = Modifier.height(12.dp))
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlineButton(text = strings["confirm_cancel"]) { onCancel() }
        GradientButton(text = strings["confirm_ok"]) { onConfirm() }
      }
    }
  }
}

@Composable
fun AlertModal(
  strings: AppStrings,
  alertMessage: String,
  onDismiss: () -> Unit,
) {
  val colors = LocalAppColors.current
  ModalOverlay {
    ModalCard {
      Text(text = strings["alert_title"], fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.text)
      Spacer(modifier = Modifier.height(8.dp))
      Text(text = alertMessage, color = colors.muted, fontSize = 13.sp)
      Spacer(modifier = Modifier.height(12.dp))
      GradientButton(text = strings["ok"]) { onDismiss() }
    }
  }
}
