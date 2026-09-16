package com.solvix.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object AppDimens {
  val radiusLg = 24.dp
  val radiusMd = 16.dp
  val radiusSm = 12.dp
  val pagePadding = 18.dp
  val cardPadding = 16.dp
  val shadow = 16.dp
}

@Composable
fun AppCard(
  modifier: Modifier = Modifier,
  shape: RoundedCornerShape = RoundedCornerShape(AppDimens.radiusLg),
  content: @Composable ColumnScope.() -> Unit,
) {
  val colors = LocalAppColors.current
  val shadowColor = Color.Black.copy(alpha = 0.18f)
  val cardModifier = if (modifier == Modifier) Modifier.fillMaxWidth() else modifier
  CompositionLocalProvider(LocalContentColor provides colors.text) {
    Column(
      modifier = cardModifier
        .shadow(AppDimens.shadow, shape, ambientColor = shadowColor, spotColor = shadowColor)
        .border(1.dp, colors.cardBorder, shape)
        .background(colors.card, shape)
        .padding(AppDimens.cardPadding),
      content = content,
    )
  }
}

@Composable
fun MiniCard(
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  val colors = LocalAppColors.current
  val shadowColor = Color.Black.copy(alpha = 0.18f)
  CompositionLocalProvider(LocalContentColor provides colors.text) {
    Column(
      modifier = modifier
        .shadow(10.dp, RoundedCornerShape(AppDimens.radiusMd), ambientColor = shadowColor, spotColor = shadowColor)
        .border(1.dp, colors.cardBorder, RoundedCornerShape(AppDimens.radiusMd))
        .background(colors.card, RoundedCornerShape(AppDimens.radiusMd))
        .padding(12.dp),
      content = content,
    )
  }
}

@Composable
fun ModalCard(
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  val colors = LocalAppColors.current
  Column(
    modifier = modifier
      .shadow(20.dp, RoundedCornerShape(24.dp))
      .background(colors.card, RoundedCornerShape(24.dp))
      .border(1.dp, colors.cardBorder, RoundedCornerShape(24.dp))
      .padding(20.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    content = content,
  )
}
