package com.solvix.tabungan

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatRow(
  icon: String,
  label: String,
  value: String,
  modifier: Modifier = Modifier,
  accentColor: Color? = null,
) {
  val colors = LocalAppColors.current
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(AppDimens.radiusMd))
      .background(colors.bg2)
      .border(1.dp, colors.cardBorder, RoundedCornerShape(AppDimens.radiusMd))
      .padding(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(text = icon, fontSize = 22.sp)
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(text = label, color = colors.muted, fontSize = 12.sp)
      Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = accentColor ?: colors.text)
    }
  }
}

@Composable
fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
  val colors = LocalAppColors.current
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(AppDimens.radiusSm))
      .background(colors.bg2)
      .border(1.dp, colors.cardBorder, RoundedCornerShape(AppDimens.radiusSm))
      .padding(horizontal = 10.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(text = label, color = colors.muted, fontSize = 12.sp)
    Text(text = value, fontWeight = FontWeight.Bold, fontSize = 13.sp)
  }
}

@Composable
fun ThemeCard(
  emoji: String,
  label: String,
  active: Boolean,
  background: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  val colors = LocalAppColors.current
  val shadowColor = Color.Black.copy(alpha = 0.18f)
  val borderColor = if (active) colors.accent else colors.cardBorder
  val elevation by animateFloatAsState(if (active) 12f else 6f, label = "theme-elevation")
  Row(
    modifier = modifier
      .fillMaxWidth()
      .shadow(elevation.dp, RoundedCornerShape(14.dp), ambientColor = shadowColor, spotColor = shadowColor)
      .border(1.dp, borderColor, RoundedCornerShape(14.dp))
      .background(background, RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(text = emoji, fontSize = 16.sp)
    Spacer(modifier = Modifier.width(12.dp))
    Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
  }
}
