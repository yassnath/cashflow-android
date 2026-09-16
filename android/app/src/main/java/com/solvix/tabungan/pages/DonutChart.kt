package com.solvix.tabungan

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

data class DonutSlice(
  val label: String,
  val value: Int,
  val color: Color,
  val brush: Brush? = null,
)

@Composable
fun DonutChart(
  slices: List<DonutSlice>,
  modifier: Modifier = Modifier,
  strokeWidth: Dp = 18.dp,
  centerLabel: String? = null,
  centerSubLabel: String? = null,
  trackColor: Color = Color.Unspecified,
) {
  val colors = LocalAppColors.current
  val total = slices.sumOf { it.value }
  val ringColor = if (trackColor == Color.Unspecified) colors.bg2 else trackColor

  val animProgress = remember { Animatable(0f) }
  LaunchedEffect(slices) {
    animProgress.snapTo(0f)
    animProgress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
    )
  }

  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
      drawArc(
        color = ringColor,
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = false,
        style = stroke,
      )
      if (total > 0) {
        var startAngle = -90f
        val gap = 2f
        val progress = animProgress.value
        slices.forEach { slice ->
          val sweep = (slice.value.toFloat() / total) * 360f
          val adjustedSweep = (sweep - gap).coerceAtLeast(0f) * progress
          if (slice.brush != null) {
            drawArc(
              brush = slice.brush,
              startAngle = startAngle,
              sweepAngle = adjustedSweep,
              useCenter = false,
              style = stroke,
            )
          } else {
            drawArc(
              color = slice.color,
              startAngle = startAngle,
              sweepAngle = adjustedSweep,
              useCenter = false,
              style = stroke,
            )
          }
          startAngle += sweep * progress
        }
      }
    }
    if (centerLabel != null) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = centerLabel, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.text)
        if (centerSubLabel != null) {
          Text(text = centerSubLabel, fontSize = 11.sp, color = colors.muted)
        }
      }
    }
  }
}

@Composable
fun DonutLegend(slices: List<DonutSlice>) {
  val colors = LocalAppColors.current
  val total = slices.sumOf { it.value }.coerceAtLeast(1)
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    slices.forEach { slice ->
      val percent = (slice.value.toFloat() / total) * 100f
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(slice.color),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "${slice.label} • ${slice.value} (${percent.roundToInt()}%)",
          color = colors.text,
          fontSize = 12.sp,
        )
      }
    }
  }
}
