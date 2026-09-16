package com.solvix.tabungan

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

private val EaseOutExpo = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

/**
 * State helper yang menganimasikan nilai angka Long dari 0 (atau nilai sebelumnya) ke target
 * dengan kurva deselerasi mulus EaseOutExpo khas aplikasi finansial modern.
 */
@Composable
fun rememberAnimatedAmountLong(targetAmount: Long, durationMillis: Int = 900): Long {
  val animatable = remember { Animatable(0f) }
  var previousTarget by remember { mutableStateOf(0L) }

  LaunchedEffect(targetAmount) {
    animatable.snapTo(0f)
    animatable.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = durationMillis, easing = EaseOutExpo),
    )
    previousTarget = targetAmount
  }

  val progress = animatable.value
  return (previousTarget + ((targetAmount - previousTarget) * progress)).roundToLong()
}

/**
 * State helper yang menganimasikan nilai angka integer dari 0 (atau nilai sebelumnya) ke target.
 */
@Composable
fun rememberAnimatedAmount(targetAmount: Int, durationMillis: Int = 900): Int {
  return rememberAnimatedAmountLong(targetAmount.toLong(), durationMillis).toInt()
}

/**
 * State helper yang menganimasikan nilai pecahan Float dari 0f ke target (0f..1f).
 */
@Composable
fun rememberAnimatedFloat(targetValue: Float, durationMillis: Int = 850): Float {
  val animatable = remember { Animatable(0f) }
  LaunchedEffect(targetValue) {
    animatable.animateTo(
      targetValue = targetValue,
      animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
    )
  }
  return animatable.value
}

/**
 * Komponen teks animasi mata uang (Running Counter) yang menghitung naik secara dinamis dari 0
 * ke nominal target sesuai mata uang aktif di CurrencyHelper.
 */
@Composable
fun AnimatedAmountText(
  amount: Long,
  modifier: Modifier = Modifier,
  fontSize: TextUnit = TextUnit.Unspecified,
  fontWeight: FontWeight? = null,
  color: Color = Color.Unspecified,
  maxLines: Int = 1,
  showPlusSign: Boolean = false,
  showMinusSign: Boolean = true,
  isMasked: Boolean = false,
  durationMillis: Int = 900,
) {
  if (isMasked) {
    Text(
      text = "${CurrencyHelper.currentCurrency.symbol} ••••••••",
      modifier = modifier,
      fontSize = fontSize,
      fontWeight = fontWeight,
      color = color,
      maxLines = maxLines,
      overflow = TextOverflow.Ellipsis,
    )
    return
  }

  val animatedValue = rememberAnimatedAmountLong(amount, durationMillis)
  val formatted = if (animatedValue > 0 && showPlusSign) {
    "+ " + CurrencyHelper.format(animatedValue)
  } else if (animatedValue < 0 && !showMinusSign) {
    CurrencyHelper.format(abs(animatedValue))
  } else {
    CurrencyHelper.format(animatedValue)
  }

  Text(
    text = formatted,
    modifier = modifier,
    fontSize = fontSize,
    fontWeight = fontWeight,
    color = color,
    maxLines = maxLines,
    overflow = TextOverflow.Ellipsis,
  )
}

@Composable
fun AnimatedAmountText(
  amount: Int,
  modifier: Modifier = Modifier,
  fontSize: TextUnit = TextUnit.Unspecified,
  fontWeight: FontWeight? = null,
  color: Color = Color.Unspecified,
  maxLines: Int = 1,
  showPlusSign: Boolean = false,
  showMinusSign: Boolean = true,
  isMasked: Boolean = false,
  durationMillis: Int = 900,
) {
  AnimatedAmountText(
    amount = amount.toLong(),
    modifier = modifier,
    fontSize = fontSize,
    fontWeight = fontWeight,
    color = color,
    maxLines = maxLines,
    showPlusSign = showPlusSign,
    showMinusSign = showMinusSign,
    isMasked = isMasked,
    durationMillis = durationMillis,
  )
}
