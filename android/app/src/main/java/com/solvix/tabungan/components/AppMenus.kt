package com.solvix.tabungan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties

@Composable
fun ToastMessage(text: String, visible: Boolean, modifier: Modifier = Modifier) {
  val colors = LocalAppColors.current
  val isSuccess = text.contains("berhasil", ignoreCase = true) || text.contains("sukses", ignoreCase = true)
  AnimatedVisibility(
    visible = visible,
    enter = fadeIn() + slideInVertically { it / 2 },
    exit = fadeOut() + slideOutVertically { it / 2 },
  ) {
    Box(
      modifier = modifier
        .shadow(8.dp, RoundedCornerShape(999.dp))
        .clip(RoundedCornerShape(999.dp))
        .background(colors.card)
        .border(
          width = 1.dp,
          color = if (isSuccess) Color(0xFF10B981).copy(alpha = 0.5f) else colors.accent.copy(alpha = 0.45f),
          shape = RoundedCornerShape(999.dp),
        )
        .padding(horizontal = 16.dp, vertical = 10.dp),
      contentAlignment = Alignment.Center,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Box(
          modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(
              if (isSuccess) Color(0xFF10B981).copy(alpha = 0.18f)
              else colors.accent.copy(alpha = 0.18f),
            ),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = if (isSuccess) Icons.Rounded.CheckCircle else Icons.Rounded.Info,
            contentDescription = null,
            tint = if (isSuccess) Color(0xFF10B981) else colors.accent,
            modifier = Modifier.size(16.dp),
          )
        }
        Text(
          text = text,
          fontWeight = FontWeight.SemiBold,
          fontSize = 12.5.sp,
          color = colors.text,
        )
      }
    }
  }
}

@Composable
fun ActionSnackbar(
  text: String,
  actionLabel: String,
  visible: Boolean,
  onActionClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = LocalAppColors.current
  AnimatedVisibility(
    visible = visible,
    enter = fadeIn() + slideInVertically { it / 2 },
    exit = fadeOut() + slideOutVertically { it / 2 },
  ) {
    Box(
      modifier = modifier
        .fillMaxWidth(0.92f)
        .clip(RoundedCornerShape(14.dp))
        .background(colors.card)
        .border(1.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
        .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = text,
          fontWeight = FontWeight.Medium,
          fontSize = 13.sp,
          color = colors.text,
          modifier = Modifier.weight(1f, fill = false),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = actionLabel,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 13.sp,
          color = colors.accent,
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onActionClick)
          .heightIn(min = 44.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        )
      }
    }
  }
}

@Composable
fun FadeInPage(key: Any?, content: @Composable () -> Unit) {
  val alphaAnim = remember { Animatable(0f) }
  LaunchedEffect(key) {
    alphaAnim.snapTo(0f)
    alphaAnim.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
    )
  }
  Box(modifier = Modifier.graphicsLayer { alpha = alphaAnim.value }) {
    content()
  }
}

@Composable
fun DropDownMenuCard(
  expanded: Boolean,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  anchorBounds: IntRect = IntRect.Zero,
  alignRight: Boolean = true,
  xOffsetDp: Dp = 0.dp,
  yOffsetDp: Dp = 10.dp,
  content: @Composable ColumnScope.() -> Unit,
) {
  if (!expanded) return
  val colors = LocalAppColors.current
  val density = LocalDensity.current
  val extraYOffsetPx = with(density) { yOffsetDp.roundToPx() }
  val extraXOffsetPx = with(density) { xOffsetDp.roundToPx() }
  val positionProvider = remember(anchorBounds, alignRight, extraYOffsetPx, extraXOffsetPx) {
    object : PopupPositionProvider {
      override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
      ): IntOffset {
        val bounds = anchorBounds
        var x = if (alignRight) {
          bounds.right - popupContentSize.width
        } else if (layoutDirection == LayoutDirection.Rtl) {
          bounds.right - popupContentSize.width
        } else {
          bounds.left
        }
        x += extraXOffsetPx
        val downY = bounds.bottom + extraYOffsetPx
        val upY = bounds.top - popupContentSize.height - extraYOffsetPx
        var y = if (downY + popupContentSize.height <= windowSize.height) downY else upY
        if (x + popupContentSize.width > windowSize.width) {
          x = windowSize.width - popupContentSize.width
        }
        if (x < 0) x = 0
        if (y < 0) y = 0
        return IntOffset(x, y)
      }
    }
  }
  Popup(
    popupPositionProvider = positionProvider,
    properties = PopupProperties(focusable = true),
    onDismissRequest = onDismiss,
  ) {
    Column(
      modifier = modifier
        .shadow(16.dp, RoundedCornerShape(20.dp))
        .clip(RoundedCornerShape(20.dp))
        .background(colors.card, RoundedCornerShape(20.dp))
        .border(1.dp, colors.accent.copy(alpha = 0.40f), RoundedCornerShape(20.dp))
        .padding(12.dp),
      content = content,
    )
  }
}

@Composable
fun MenuItem(
  text: String,
  icon: ThemedVisualIcon,
  color: Color? = null,
  active: Boolean = false,
  onClick: () -> Unit,
) {
  val colors = LocalAppColors.current
  val itemColor = color ?: colors.text
  val bgModifier = if (active) {
    Modifier.background(
      brush = Brush.linearGradient(
        listOf(
          colors.accent,
          colors.accent2,
        ),
      ),
    )
  } else {
    Modifier.background(colors.bg2)
  }
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .heightIn(min = 44.dp)
      .clip(RoundedCornerShape(12.dp))
      .then(bgModifier)
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    ThemedBadgeIcon(
      icon = icon,
      containerSize = 30.dp,
      iconSize = 16.dp,
      tint = if (active) colors.onGradient else itemColor,
      bgAlpha = if (active) 0.35f else 0.12f,
    )
    Spacer(modifier = Modifier.width(10.dp))
    Text(
      text = text,
      fontWeight = FontWeight.SemiBold,
      fontSize = 13.sp,
      color = if (active) colors.onGradient else itemColor,
    )
  }
}

@Composable
fun MenuItem(
  text: String,
  emoji: String,
  color: Color? = null,
  active: Boolean = false,
  onClick: () -> Unit,
) = MenuItem(
  text = text,
  icon = ThemedVisualIcon.ThemedBadge(emoji),
  color = color,
  active = active,
  onClick = onClick,
)
