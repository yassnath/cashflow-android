package com.solvix.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppCard
import com.solvix.tabungan.AppDimens
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.LocalStrings
import com.solvix.tabungan.LocalThemeName
import com.solvix.tabungan.Page
import com.solvix.tabungan.SectionTitle
import com.solvix.tabungan.themePageIcon
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.E
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

@Composable
fun CalculatorPage() {
  var display by rememberSaveable { mutableStateOf("0") }
  val strings = LocalStrings.current
  val colors = LocalAppColors.current
  Column {
    SectionTitle(icon = themePageIcon(LocalThemeName.current, Page.Calculator), title = strings["calculator_title"], subtitle = strings["calculator_subtitle"])
    AppCard {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.radiusSm))
            .background(colors.bg2)
            .testTag("calculator_display")
            .padding(12.dp),
          contentAlignment = Alignment.CenterEnd,
        ) {
          Text(text = display, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.text)
        }

        val rows = listOf(
          listOf("sin", "cos", "tan", "ln", "log"),
          listOf("√", "^", "(", ")", "π"),
          listOf("7", "8", "9", "÷", "C"),
          listOf("4", "5", "6", "×", "⌫"),
          listOf("1", "2", "3", "−", "%"),
          listOf("0", ".", "e", "+", "="),
        )
        rows.forEach { row ->
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            row.forEach { key ->
              CalculatorKey(
                label = key,
                isPrimary = key == "=",
                onPress = { display = calculate(display, key) },
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun RowScope.CalculatorKey(label: String, onPress: () -> Unit, isPrimary: Boolean = false) {
  val colors = LocalAppColors.current
  val isOperator = label in listOf("÷", "×", "−", "+", "^", "%")
  val isFunction = label in listOf("sin", "cos", "tan", "ln", "log", "√")
  val backgroundColor = when {
    label == "C" || label == "⌫" -> colors.danger.copy(alpha = 0.2f)
    isOperator -> colors.accent.copy(alpha = 0.2f)
    isFunction -> colors.accent2.copy(alpha = 0.18f)
    else -> colors.bg2
  }
  val backgroundModifier = if (isPrimary) {
    Modifier.background(brush = Brush.linearGradient(listOf(colors.accent, colors.accent2)))
  } else {
    Modifier.background(color = backgroundColor)
  }
  val textColor = when {
    isPrimary -> Color.White
    label == "C" || label == "⌫" -> Color(0xFFB92643)
    isOperator -> Color(0xFFA55B00)
    isFunction -> colors.accent2
    else -> colors.text
  }
  val fontSize = if (label.length > 1) 12.sp else 18.sp

  Box(
    modifier = Modifier
      .weight(1f)
      .height(48.dp)
      .clip(RoundedCornerShape(14.dp))
      .then(backgroundModifier)
      .clickable { onPress() }
      .padding(vertical = 12.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(text = label, fontSize = fontSize, fontWeight = FontWeight.SemiBold, color = textColor)
  }
}

sealed class CalcToken {
  data class Number(val value: Double) : CalcToken()
  data class Operator(val op: String) : CalcToken()
  data class Function(val name: String) : CalcToken()
  object LeftParen : CalcToken()
  object RightParen : CalcToken()
}

fun calculate(current: String, key: String): String {
  val safeCurrent = if (current == "Error") "0" else current
  val lastChar = safeCurrent.lastOrNull()
  val isZeroState = safeCurrent == "0"

  fun needsMultiply(): Boolean {
    return !isZeroState && lastChar != null && (lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e')
  }

  fun prefixForValueStart(): String {
    return when {
      isZeroState -> ""
      needsMultiply() -> safeCurrent + "×"
      else -> safeCurrent
    }
  }

  fun openParenCount(text: String): Int = text.count { it == '(' }
  fun closeParenCount(text: String): Int = text.count { it == ')' }

  return when (key) {
    "C" -> "0"
    "⌫" -> {
      val trimmed = safeCurrent.dropLast(1)
      if (trimmed.isBlank() || trimmed == "-") "0" else trimmed
    }
    "=" -> evaluateExpression(safeCurrent)
    "sin", "cos", "tan", "ln", "log" -> {
      prefixForValueStart() + key + "("
    }
    "√" -> {
      prefixForValueStart() + "√("
    }
    "π", "e" -> {
      prefixForValueStart() + key
    }
    "(" -> {
      prefixForValueStart() + "("
    }
    ")" -> {
      if (openParenCount(safeCurrent) > closeParenCount(safeCurrent) && lastChar != null && lastChar !in listOf('+', '−', '×', '÷', '^', '(')) {
        safeCurrent + ")"
      } else {
        safeCurrent
      }
    }
    "." -> {
      if (safeCurrent.isBlank() || lastChar == null || lastChar in listOf('+', '−', '×', '÷', '^', '(')) {
        safeCurrent + "0."
      } else {
        val lastNumber = safeCurrent.takeLastWhile { it.isDigit() || it == '.' }
        if (lastNumber.contains(".")) safeCurrent else safeCurrent + "."
      }
    }
    "+", "−", "×", "÷", "^" -> {
      when {
        safeCurrent == "0" && key != "−" -> safeCurrent
        lastChar == null -> if (key == "−") "-" else safeCurrent
        lastChar in listOf('+', '−', '×', '÷', '^') -> safeCurrent.dropLast(1) + key
        else -> safeCurrent + key
      }
    }
    "%" -> {
      if (lastChar != null && (lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e')) {
        safeCurrent + "%"
      } else {
        safeCurrent
      }
    }
    else -> {
      if (key.all { it.isDigit() }) {
        if (safeCurrent == "0") key else safeCurrent + key
      } else {
        safeCurrent + key
      }
    }
  }
}

private fun evaluateExpression(expression: String): String {
  return try {
    val tokens = tokenize(expression)
    val rpn = toRpn(tokens)
    val result = evalRpn(rpn)
    formatCalcResult(result)
  } catch (_: Exception) {
    "Error"
  }
}

private fun tokenize(raw: String): List<CalcToken> {
  val expression = raw.replace("×", "*").replace("÷", "/").replace("−", "-")
  val tokens = mutableListOf<CalcToken>()
  var i = 0
  while (i < expression.length) {
    val ch = expression[i]
    when {
      ch.isWhitespace() -> i++
      ch.isDigit() || ch == '.' -> {
        val start = i
        var hasDot = ch == '.'
        i++
        while (i < expression.length && (expression[i].isDigit() || (!hasDot && expression[i] == '.'))) {
          if (expression[i] == '.') hasDot = true
          i++
        }
        val numberText = expression.substring(start, i)
        val value = numberText.toDoubleOrNull() ?: 0.0
        tokens.add(CalcToken.Number(value))
      }
      ch == 'π' -> {
        tokens.add(CalcToken.Number(PI))
        i++
      }
      ch == 'e' -> {
        tokens.add(CalcToken.Number(E))
        i++
      }
      ch == '√' -> {
        tokens.add(CalcToken.Function("sqrt"))
        i++
      }
      ch.isLetter() -> {
        val start = i
        i++
        while (i < expression.length && expression[i].isLetter()) i++
        val name = expression.substring(start, i)
        when (name.lowercase(Locale.US)) {
          "sin", "cos", "tan", "log", "ln" -> tokens.add(CalcToken.Function(name.lowercase(Locale.US)))
          "pi" -> tokens.add(CalcToken.Number(PI))
          "e" -> tokens.add(CalcToken.Number(E))
          else -> throw IllegalArgumentException("Unknown token")
        }
      }
      ch == '(' -> {
        tokens.add(CalcToken.LeftParen)
        i++
      }
      ch == ')' -> {
        tokens.add(CalcToken.RightParen)
        i++
      }
      ch in listOf('+', '-', '*', '/', '^', '%') -> {
        tokens.add(CalcToken.Operator(ch.toString()))
        i++
      }
      else -> i++
    }
  }
  return insertImplicitMultiplication(tokens)
}

private fun insertImplicitMultiplication(tokens: List<CalcToken>): List<CalcToken> {
  val output = mutableListOf<CalcToken>()
  fun isValue(token: CalcToken): Boolean = token is CalcToken.Number || token is CalcToken.RightParen
  fun startsValue(token: CalcToken): Boolean = token is CalcToken.Number || token is CalcToken.Function || token is CalcToken.LeftParen
  tokens.forEach { token ->
    val prev = output.lastOrNull()
    if (prev != null && isValue(prev) && startsValue(token)) {
      output.add(CalcToken.Operator("*"))
    }
    output.add(token)
  }
  return output
}

private fun toRpn(tokens: List<CalcToken>): List<CalcToken> {
  val output = mutableListOf<CalcToken>()
  val stack = mutableListOf<CalcToken>()
  var prev: CalcToken? = null

  fun precedence(op: String): Int = when (op) {
    "+", "-" -> 1
    "*", "/" -> 2
    "^" -> 3
    "%" -> 4
    else -> 0
  }

  fun isRightAssoc(op: String): Boolean = op == "^"

  tokens.forEach { token ->
    when (token) {
      is CalcToken.Number -> output.add(token)
      is CalcToken.Function -> stack.add(token)
      is CalcToken.Operator -> {
        val op = token.op
        val isUnaryMinus = op == "-" && (prev == null || prev is CalcToken.Operator || prev is CalcToken.LeftParen)
        if (isUnaryMinus) {
          stack.add(CalcToken.Function("neg"))
        } else {
          while (stack.isNotEmpty()) {
            val top = stack.last()
            if (top is CalcToken.Operator) {
              val topOp = top.op
              val shouldPop = if (isRightAssoc(op)) precedence(op) < precedence(topOp) else precedence(op) <= precedence(topOp)
              if (shouldPop) {
                output.add(stack.removeAt(stack.lastIndex))
              } else {
                break
              }
            } else if (top is CalcToken.Function) {
              output.add(stack.removeAt(stack.lastIndex))
            } else {
              break
            }
          }
          stack.add(token)
        }
      }
      is CalcToken.LeftParen -> stack.add(token)
      is CalcToken.RightParen -> {
        while (stack.isNotEmpty() && stack.last() !is CalcToken.LeftParen) {
          output.add(stack.removeAt(stack.lastIndex))
        }
        if (stack.isNotEmpty() && stack.last() is CalcToken.LeftParen) {
          stack.removeAt(stack.lastIndex)
        }
        if (stack.isNotEmpty() && stack.last() is CalcToken.Function) {
          output.add(stack.removeAt(stack.lastIndex))
        }
      }
    }
    prev = token
  }

  while (stack.isNotEmpty()) {
    val top = stack.removeAt(stack.lastIndex)
    if (top !is CalcToken.LeftParen && top !is CalcToken.RightParen) {
      output.add(top)
    }
  }
  return output
}

private fun evalRpn(tokens: List<CalcToken>): Double {
  val stack = mutableListOf<Double>()
  tokens.forEach { token ->
    when (token) {
      is CalcToken.Number -> stack.add(token.value)
      is CalcToken.Operator -> {
        if (token.op == "%") {
          val a = stack.removeAt(stack.lastIndex)
          stack.add(a / 100.0)
        } else {
          val b = stack.removeAt(stack.lastIndex)
          val a = stack.removeAt(stack.lastIndex)
          val result = when (token.op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> a / b
            "^" -> a.pow(b)
            else -> a
          }
          stack.add(result)
        }
      }
      is CalcToken.Function -> {
        val a = stack.removeAt(stack.lastIndex)
        val result = when (token.name) {
          "neg" -> -a
          "sin" -> sin(Math.toRadians(a))
          "cos" -> cos(Math.toRadians(a))
          "tan" -> tan(Math.toRadians(a))
          "log" -> log10(a)
          "ln" -> ln(a)
          "sqrt" -> sqrt(a)
          else -> a
        }
        stack.add(result)
      }
      else -> Unit
    }
  }
  return stack.lastOrNull() ?: 0.0
}

private fun formatCalcResult(value: Double): String {
  if (value.isNaN() || value.isInfinite()) return "Error"
  val sanitized = if (abs(value) < 1e-10) 0.0 else value
  val symbols = DecimalFormatSymbols(Locale.US)
  val formatter = DecimalFormat("#.##########", symbols)
  return formatter.format(sanitized)
}
