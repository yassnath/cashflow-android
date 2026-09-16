package com.solvix.tabungan

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

  fun isImplicitMultiplyTarget(ch: Char?): Boolean {
    if (ch == null) return false
    return ch.isDigit() || ch == ')' || ch == 'π' || ch == 'e' || ch == '%'
  }

  fun prefixForValueStart(): String {
    return if (safeCurrent == "0") "" else if (isImplicitMultiplyTarget(lastChar)) safeCurrent + "×" else safeCurrent
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

fun evaluateExpression(expression: String): String {
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
