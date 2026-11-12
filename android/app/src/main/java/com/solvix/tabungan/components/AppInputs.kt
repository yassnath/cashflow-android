package com.solvix.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt

@Composable
fun AppTextField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String = "",
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  minLines: Int = 1,
  trailing: (@Composable () -> Unit)? = null,
  isPassword: Boolean = false,
  textFontSize: TextUnit = TextUnit.Unspecified,
  placeholderFontSize: TextUnit = TextUnit.Unspecified,
) {
  val colors = LocalAppColors.current
  var showPassword by remember { mutableStateOf(false) }
  val trailingIcon = trailing ?: if (isPassword) {
    {
      IconButton(onClick = { showPassword = !showPassword }) {
        Icon(
          imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
          contentDescription = if (showPassword) "Hide password" else "Show password",
          tint = colors.muted,
        )
      }
    }
  } else {
    null
  }
  Column(modifier = Modifier.fillMaxWidth()) {
    if (label.isNotBlank()) {
      Text(text = label, fontSize = 13.sp, color = colors.text)
      Spacer(modifier = Modifier.height(6.dp))
    }
    OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      placeholder = {
        if (placeholder.isNotBlank()) {
          Text(
            text = placeholder,
            color = colors.placeholder,
            fontSize = if (placeholderFontSize == TextUnit.Unspecified) 14.sp else placeholderFontSize,
          )
        }
      },
      keyboardOptions = keyboardOptions,
      singleLine = minLines == 1,
      minLines = minLines,
      trailingIcon = trailingIcon,
      visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
      textStyle = if (textFontSize == TextUnit.Unspecified) {
        MaterialTheme.typography.bodyMedium
      } else {
        TextStyle(
          fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
          fontWeight = MaterialTheme.typography.bodyMedium.fontWeight,
          fontSize = textFontSize,
        )
      },
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 52.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.accent,
        unfocusedBorderColor = colors.fieldBorder,
        focusedTextColor = colors.text,
        unfocusedTextColor = colors.text,
        focusedContainerColor = colors.card,
        unfocusedContainerColor = colors.card,
      ),
      shape = RoundedCornerShape(AppDimens.radiusSm),
    )
  }
}

@Composable
fun AppDropdown(
  label: String,
  placeholder: String,
  options: List<String>,
  selected: String,
  onSelected: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = LocalAppColors.current
  var expanded by remember { mutableStateOf(false) }
  var fieldWidthPx by remember { mutableIntStateOf(0) }
  var anchorBounds by remember { mutableStateOf(IntRect.Zero) }
  val density = LocalDensity.current
  val extraYOffsetPx = with(density) { 10.dp.roundToPx() }
  val dropdownModifier = if (modifier == Modifier) Modifier.fillMaxWidth() else modifier

  Column(modifier = dropdownModifier) {
    if (label.isNotBlank()) {
      Text(text = label, fontSize = 13.sp, color = colors.text)
      Spacer(modifier = Modifier.height(6.dp))
    }
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 48.dp)
        .clip(RoundedCornerShape(AppDimens.radiusSm))
        .border(1.dp, colors.fieldBorder, RoundedCornerShape(AppDimens.radiusSm))
        .background(colors.card)
        .clickable { expanded = true }
        .padding(horizontal = 12.dp, vertical = 6.dp)
        .onGloballyPositioned { coordinates ->
          fieldWidthPx = coordinates.size.width
          val rect = coordinates.boundsInWindow()
          anchorBounds = IntRect(
            rect.left.roundToInt(),
            rect.top.roundToInt(),
            rect.right.roundToInt(),
            rect.bottom.roundToInt(),
          )
        },
      contentAlignment = Alignment.CenterStart,
    ) {
      Text(
        text = if (selected.isBlank()) placeholder else selected,
        color = if (selected.isBlank()) colors.placeholder else colors.text,
        fontSize = 14.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(text = "▾", color = colors.muted, modifier = Modifier.align(Alignment.CenterEnd))
    }
    val menuScroll = rememberScrollState()
    if (expanded) {
      val positionProvider = remember(anchorBounds, extraYOffsetPx) {
        object : PopupPositionProvider {
          override fun calculatePosition(
            anchorBounds: IntRect,
            windowSize: IntSize,
            layoutDirection: LayoutDirection,
            popupContentSize: IntSize,
          ): IntOffset {
            val bounds = anchorBounds
            var x = if (layoutDirection == LayoutDirection.Rtl) {
              bounds.right - popupContentSize.width
            } else {
              bounds.left
            }
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
      val menuWidth = with(density) { fieldWidthPx.toDp() }
      Popup(
        popupPositionProvider = positionProvider,
        properties = PopupProperties(focusable = true),
        onDismissRequest = { expanded = false },
      ) {
        Column(
          modifier = Modifier
            .width(menuWidth)
            .clip(RoundedCornerShape(18.dp))
            .background(colors.card)
            .border(1.dp, colors.fieldBorder, RoundedCornerShape(18.dp))
            .padding(12.dp)
            .heightIn(max = 360.dp)
            .verticalScroll(menuScroll),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          options.forEach { option ->
            val isSelected = option == selected
            val itemShape = RoundedCornerShape(10.dp)
            val itemBackground = if (isSelected) {
              Brush.linearGradient(listOf(colors.accent, colors.accent2))
            } else {
              Brush.linearGradient(listOf(colors.bg2, colors.bg2))
            }
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .clip(itemShape)
                .background(itemBackground)
                .clickable {
                  onSelected(option)
                  expanded = false
                }
                .padding(10.dp),
              contentAlignment = Alignment.CenterStart,
            ) {
              Text(
                text = option,
                color = if (isSelected) colors.onGradient else colors.text,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }
    }
  }
}
