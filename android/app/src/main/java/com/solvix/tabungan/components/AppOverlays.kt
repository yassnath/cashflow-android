package com.solvix.tabungan.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.solvix.tabungan.AppCard
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
  val colors = LocalAppColors.current
  AnimatedVisibility(
    visible = showLoading,
    enter = fadeIn(tween(600)),
    exit = fadeOut(tween(600)),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(colors.bg)
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
    ModalCard(
      modifier = Modifier.fillMaxWidth(0.92f),
    ) {
      Box(
        modifier = Modifier
          .size(54.dp)
          .clip(CircleShape)
          .background(colors.accent.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
          contentDescription = null,
          tint = colors.accent,
          modifier = Modifier.size(28.dp),
        )
      }
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = strings["confirm_title"].ifBlank { "Konfirmasi Tindakan" },
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = colors.text,
        textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = confirmMessage,
        color = colors.muted,
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
        lineHeight = 18.sp,
      )
      Spacer(modifier = Modifier.height(20.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Box(modifier = Modifier.weight(1f)) {
          GhostButton(text = strings["confirm_cancel"].ifBlank { "Batal" }, fillMaxWidth = true) { onCancel() }
        }
        Box(modifier = Modifier.weight(1f)) {
          GradientButton(text = strings["confirm_ok"].ifBlank { "Lanjutkan" }) { onConfirm() }
        }
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
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(bottom = 84.dp, start = 16.dp, end = 16.dp),
    contentAlignment = Alignment.BottomCenter,
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 440.dp)
        .shadow(12.dp, RoundedCornerShape(18.dp))
        .clip(RoundedCornerShape(18.dp))
        .background(colors.card)
        .border(1.dp, colors.accent.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
        .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(colors.accent.copy(alpha = 0.18f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Rounded.Info,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(20.dp),
          )
        }

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = strings["alert_title"].ifBlank { "Pemberitahuan" },
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = colors.text,
          )
          Text(
            text = alertMessage,
            fontSize = 12.sp,
            color = colors.muted,
            lineHeight = 16.sp,
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.accent)
            .clickable { onDismiss() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = strings["ok"].ifBlank { "OK" },
            color = colors.onGradient,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
          )
        }
      }
    }
  }
}

/**
 * Dialog konfirmasi seragam bernilai tinggi untuk tindakan penting / destruktif di dalam aplikasi.
 */
@Composable
fun AppConfirmationDialog(
  title: String,
  message: String,
  confirmText: String = "Lanjutkan",
  cancelText: String = "Batal",
  isDestructive: Boolean = false,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  val colors = LocalAppColors.current
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(0.9f)
        .padding(16.dp),
      contentAlignment = Alignment.Center,
    ) {
      AppCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Box(
            modifier = Modifier
              .size(52.dp)
              .clip(CircleShape)
              .background(
                if (isDestructive) colors.danger.copy(alpha = 0.15f)
                else colors.accent.copy(alpha = 0.15f)
              ),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = if (isDestructive) Icons.Rounded.WarningAmber else Icons.AutoMirrored.Rounded.HelpOutline,
              contentDescription = null,
              tint = if (isDestructive) colors.danger else colors.accent,
              modifier = Modifier.size(28.dp),
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = colors.text,
            textAlign = TextAlign.Center,
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = message,
            fontSize = 13.sp,
            color = colors.muted,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
          )

          Spacer(modifier = Modifier.height(18.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.bg2)
                .clickable { onDismiss() }
                .padding(vertical = 12.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = cancelText,
                color = colors.text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
              )
            }

            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDestructive) colors.danger else colors.accent)
                .clickable {
                  onDismiss()
                  onConfirm()
                }
                .padding(vertical = 12.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = confirmText,
                color = if (isDestructive) colors.onDanger else colors.onGradient,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
              )
            }
          }
        }
      }
    }
  }
}
