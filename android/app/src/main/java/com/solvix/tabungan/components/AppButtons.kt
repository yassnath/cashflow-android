package com.solvix.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GradientButton(
  text: String,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  onClick: () -> Unit,
) {
  val colors = LocalAppColors.current
  val context = LocalContext.current
  val background = if (enabled) {
    Brush.linearGradient(listOf(colors.accent, colors.accent2))
  } else {
    Brush.linearGradient(listOf(colors.muted, colors.muted))
  }
  Box(
    modifier = modifier
      .fillMaxWidth()
      .heightIn(min = 44.dp)
      .clip(RoundedCornerShape(14.dp))
      .background(background)
      .clickable(
        enabled = enabled,
        onClick = {
          HapticHelper.vibrateClick(context)
          onClick()
        },
      ),
    contentAlignment = Alignment.Center,
  ) {
    val textColor = if (enabled) Color.White else colors.text.copy(alpha = 0.6f)
    Text(text = text, color = textColor, fontWeight = FontWeight.Bold)
  }
}

@Composable
fun GhostButton(
  text: String,
  modifier: Modifier = Modifier,
  fillMaxWidth: Boolean = true,
  onClick: () -> Unit,
) {
  val colors = LocalAppColors.current
  val context = LocalContext.current
  val baseModifier = if (fillMaxWidth) modifier.fillMaxWidth() else modifier
  Box(
    modifier = baseModifier
      .heightIn(min = 44.dp)
      .clip(RoundedCornerShape(14.dp))
      .background(colors.bg2.copy(alpha = 0.7f))
      .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp))
      .clickable(
        onClick = {
          HapticHelper.vibrateClick(context)
          onClick()
        },
      )
      .padding(horizontal = 16.dp, vertical = 10.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(text = text, color = colors.text, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
  }
}

@Composable
fun OutlineButton(
  text: String,
  modifier: Modifier = Modifier,
  fillMaxWidth: Boolean = true,
  onClick: () -> Unit,
) {
  val colors = LocalAppColors.current
  val context = LocalContext.current
  val baseModifier = if (fillMaxWidth) modifier.fillMaxWidth() else modifier
  Box(
    modifier = baseModifier
      .heightIn(min = 40.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(colors.card)
      .border(1.5.dp, colors.accent, RoundedCornerShape(12.dp))
      .clickable(
        onClick = {
          HapticHelper.vibrateClick(context)
          onClick()
        },
      )
      .padding(horizontal = 14.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(text = text, color = colors.accent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
  }
}

@Composable
fun DangerButton(
  text: String,
  modifier: Modifier = Modifier,
  fillMaxWidth: Boolean = true,
  onClick: () -> Unit,
) {
  val colors = LocalAppColors.current
  val context = LocalContext.current
  val baseModifier = if (fillMaxWidth) modifier.fillMaxWidth() else modifier
  Box(
    modifier = baseModifier
      .heightIn(min = 40.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(colors.danger.copy(alpha = 0.12f))
      .border(1.dp, colors.danger.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
      .clickable(
        onClick = {
          HapticHelper.vibrateClick(context)
          onClick()
        },
      )
      .padding(horizontal = 14.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(text = text, color = colors.danger, fontWeight = FontWeight.Bold, fontSize = 13.sp)
  }
}

@Composable
fun ChipButton(
  text: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  val colors = LocalAppColors.current
  val borderColor = colors.accent.copy(alpha = 0.45f)
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(999.dp))
      .background(colors.accent.copy(alpha = 0.08f))
      .border(1.dp, borderColor, RoundedCornerShape(999.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 5.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = text,
      fontWeight = FontWeight.SemiBold,
      fontSize = 11.sp,
      color = colors.accent,
      maxLines = 1,
    )
  }
}

@Composable
fun ChipToggle(
  options: List<String>,
  selectedIndex: Int,
  modifier: Modifier = Modifier,
  onSelect: (Int) -> Unit,
) {
  val colors = LocalAppColors.current
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(999.dp))
      .background(colors.bg2)
      .padding(4.dp),
  ) {
    options.forEachIndexed { index, option ->
      val active = index == selectedIndex
      val chipModifier = if (active) {
        Modifier.background(Brush.linearGradient(listOf(colors.accent, colors.accent2)))
      } else {
        Modifier.background(Color.Transparent)
      }
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(999.dp))
          .then(chipModifier)
          .clickable { onSelect(index) }
          .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = option,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = if (active) Color.White else colors.text,
        )
      }
      if (index != options.lastIndex) {
        Spacer(modifier = Modifier.width(4.dp))
      }
    }
  }
}
