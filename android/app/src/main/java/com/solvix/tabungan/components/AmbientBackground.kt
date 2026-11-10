package com.solvix.tabungan

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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.LocalAppColors
import kotlin.math.roundToInt

@Composable
fun AmbientBackground() {
  val colors = LocalAppColors.current
  val transition = rememberInfiniteTransition(label = "ambient")
  val float1 by transition.animateFloat(
    initialValue = 0f,
    targetValue = 20f,
    animationSpec = infiniteRepeatable(tween(8000), RepeatMode.Reverse),
    label = "float1",
  )
  val float2 by transition.animateFloat(
    initialValue = 0f,
    targetValue = -20f,
    animationSpec = infiniteRepeatable(tween(9000), RepeatMode.Reverse),
    label = "float2",
  )
  val sparkle by transition.animateFloat(
    initialValue = 0.3f,
    targetValue = 0.8f,
    animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse),
    label = "sparkle",
  )

  Box(modifier = Modifier.fillMaxSize()) {
    Blob(
      modifier = Modifier
        .size(280.dp)
        .offset {
          IntOffset(
            x = (-60).dp.roundToPx(),
            y = (-80 + float1).dp.roundToPx(),
          )
        },
      brush = Brush.radialGradient(
        listOf(colors.accent, Color.Transparent),
      ),
    )
    Blob(
      modifier = Modifier
        .size(280.dp)
        .align(Alignment.BottomEnd)
        .offset {
          IntOffset(
            x = 80.dp.roundToPx(),
            y = (120 + float2).dp.roundToPx(),
          )
        },
      brush = Brush.radialGradient(
        listOf(colors.accent2, Color.Transparent),
      ),
    )
    Blob(
      modifier = Modifier
        .size(280.dp)
        .align(Alignment.CenterEnd)
        .offset {
          IntOffset(
            x = 120.dp.roundToPx(),
            y = float1.dp.roundToPx(),
          )
        },
      brush = Brush.radialGradient(
        listOf(Color(0xFF8AA7FF), Color.Transparent),
      ),
    )
    Text(
      text = "✦",
      modifier = Modifier
        .offset(x = 48.dp, y = 140.dp)
        .alpha(sparkle),
      color = Color.White.copy(alpha = 0.7f),
      fontSize = 18.sp,
    )
    Text(
      text = "✦",
      modifier = Modifier
        .offset(x = 260.dp, y = 360.dp)
        .alpha(sparkle),
      color = Color.White.copy(alpha = 0.7f),
      fontSize = 18.sp,
    )
    Text(
      text = "✦",
      modifier = Modifier
        .offset(x = 160.dp, y = 640.dp)
        .alpha(sparkle),
      color = Color.White.copy(alpha = 0.7f),
      fontSize = 18.sp,
    )
  }
}

@Composable
private fun Blob(modifier: Modifier, brush: Brush) {
  Box(
    modifier = modifier
      .clip(CircleShape)
      .blur(12.dp)
      .background(brush)
      .alpha(0.3f),
  )
}
