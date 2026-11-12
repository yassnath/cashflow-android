package com.solvix.tabungan

import android.provider.Settings
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.LocalAppColors
import kotlin.math.roundToInt

@Composable
fun AmbientBackground() {
  val colors = LocalAppColors.current
  val theme = LocalThemeName.current
  val isDark = isDarkTheme(theme)
  val context = LocalContext.current
  val reducedMotion = androidx.compose.runtime.remember {
    runCatching {
      Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f,
      ) == 0f
    }.getOrDefault(false)
  }
  val motionDistance = if (reducedMotion) 0f else 24f
  val transition = rememberInfiniteTransition(label = "ambient")
  val float1 by transition.animateFloat(
    initialValue = 0f,
    targetValue = motionDistance,
    animationSpec = infiniteRepeatable(tween(8000), RepeatMode.Reverse),
    label = "float1",
  )
  val float2 by transition.animateFloat(
    initialValue = 0f,
    targetValue = -motionDistance,
    animationSpec = infiniteRepeatable(tween(9000), RepeatMode.Reverse),
    label = "float2",
  )
  val sparkle by transition.animateFloat(
    initialValue = if (reducedMotion) 0f else 0.25f,
    targetValue = if (reducedMotion) 0f else 0.7f,
    animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse),
    label = "sparkle",
  )

  val blobAlpha = if (isDark) 0.40f else 0.30f
  val starColor = if (isDark) Color.White.copy(alpha = 0.85f) else colors.accent.copy(alpha = 0.4f)

  Box(modifier = Modifier.fillMaxSize()) {
    Blob(
      modifier = Modifier
        .size(280.dp)
        .offset {
          IntOffset(
            x = (-70).dp.roundToPx(),
            y = (-90 + float1).dp.roundToPx(),
          )
        },
      brush = Brush.radialGradient(
        listOf(colors.accent.copy(alpha = 0.75f), colors.accent.copy(alpha = 0.25f), Color.Transparent),
      ),
      alpha = blobAlpha,
    )
    Blob(
      modifier = Modifier
        .size(280.dp)
        .align(Alignment.BottomEnd)
        .offset {
          IntOffset(
            x = 90.dp.roundToPx(),
            y = (130 + float2).dp.roundToPx(),
          )
        },
      brush = Brush.radialGradient(
        listOf(colors.accent2.copy(alpha = 0.7f), colors.accent2.copy(alpha = 0.2f), Color.Transparent),
      ),
      alpha = blobAlpha,
    )
    Blob(
      modifier = Modifier
        .size(260.dp)
        .align(Alignment.CenterEnd)
        .offset {
          IntOffset(
            x = 130.dp.roundToPx(),
            y = float1.dp.roundToPx(),
          )
        },
      brush = Brush.radialGradient(
        listOf(colors.accent.copy(alpha = 0.5f), colors.accent2.copy(alpha = 0.2f), Color.Transparent),
      ),
      alpha = blobAlpha * 0.85f,
    )
    Text(
      text = "✦",
      modifier = Modifier
        .offset(x = 48.dp, y = 140.dp)
        .alpha(if (reducedMotion) 0f else sparkle),
      color = starColor,
      fontSize = 18.sp,
    )
    Text(
      text = "✦",
      modifier = Modifier
        .offset(x = 260.dp, y = 360.dp)
        .alpha(if (reducedMotion) 0f else sparkle),
      color = starColor,
      fontSize = 18.sp,
    )
    Text(
      text = "✦",
      modifier = Modifier
        .offset(x = 160.dp, y = 640.dp)
        .alpha(if (reducedMotion) 0f else sparkle),
      color = starColor,
      fontSize = 18.sp,
    )
  }
}

@Composable
private fun Blob(modifier: Modifier, brush: Brush, alpha: Float = 0.28f) {
  Box(
    modifier = modifier
      .clip(CircleShape)
      .blur(48.dp)
      .background(brush)
      .alpha(alpha),
  )
}
