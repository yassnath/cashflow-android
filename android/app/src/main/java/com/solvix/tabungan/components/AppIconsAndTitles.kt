package com.solvix.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ThemedIconView(
  icon: ThemedVisualIcon,
  modifier: Modifier = Modifier,
  tint: Color = LocalAppColors.current.text,
  size: Dp = 22.dp,
) {
  when (icon) {
    is ThemedVisualIcon.Vector -> {
      Icon(
        imageVector = icon.imageVector,
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(size),
      )
    }
    is ThemedVisualIcon.ThemedBadge -> {
      Text(
        text = icon.symbol,
        fontSize = (size.value * 0.95f).sp,
        modifier = modifier,
      )
    }
  }
}

@Composable
fun ThemedBadgeIcon(
  icon: ThemedVisualIcon,
  modifier: Modifier = Modifier,
  containerSize: Dp = 38.dp,
  iconSize: Dp = 20.dp,
  tint: Color = LocalAppColors.current.accent,
  bgAlpha: Float = 0.12f,
) {
  val colors = LocalAppColors.current
  Box(
    modifier = modifier
      .size(containerSize)
      .clip(RoundedCornerShape(12.dp))
      .background(colors.accent.copy(alpha = bgAlpha))
      .border(1.dp, colors.accent.copy(alpha = 0.22f), RoundedCornerShape(12.dp)),
    contentAlignment = Alignment.Center,
  ) {
    when (icon) {
      is ThemedVisualIcon.Vector -> {
        Icon(
          imageVector = icon.imageVector,
          contentDescription = null,
          tint = tint,
          modifier = Modifier.size(iconSize),
        )
      }
      is ThemedVisualIcon.ThemedBadge -> {
        Text(
          text = icon.symbol,
          fontSize = (iconSize.value * 1.15f).sp,
        )
      }
    }
  }
}

@Composable
fun SectionTitle(
  icon: ThemedVisualIcon,
  title: String,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  compact: Boolean = false,
) {
  val colors = LocalAppColors.current
  Column(modifier = modifier) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
    ) {
      ThemedBadgeIcon(
        icon = icon,
        containerSize = if (compact) 32.dp else 36.dp,
        iconSize = if (compact) 18.dp else 20.dp,
        tint = colors.accent,
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = title,
        style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = colors.text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
    if (subtitle != null) {
      Text(
        text = subtitle,
        color = colors.muted,
        fontSize = 13.sp,
        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
      )
    }
  }
}

@Composable
fun SectionTitle(
  icon: String,
  title: String,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  compact: Boolean = false,
) = SectionTitle(
  icon = ThemedVisualIcon.ThemedBadge(icon),
  title = title,
  modifier = modifier,
  subtitle = subtitle,
  compact = compact,
)

@Composable
fun IconCircle(emoji: String, size: Dp = 28.dp) {
  val colors = LocalAppColors.current
  Box(
    modifier = Modifier
      .size(size)
      .clip(CircleShape)
      .background(colors.bg2),
    contentAlignment = Alignment.Center,
  ) {
    Text(text = emoji)
  }
}
