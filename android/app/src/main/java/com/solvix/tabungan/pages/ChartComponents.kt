package com.solvix.tabungan

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ChartSeries(
  val labels: List<String>,
  val income: List<Int>,
  val expense: List<Int>,
)

fun buildMonthlySeries(income: List<MoneyEntry>, expense: List<MoneyEntry>, language: AppLanguage): ChartSeries {
  val now = Calendar.getInstance()
  val currentYear = now.get(Calendar.YEAR)
  val labels = mutableListOf<String>()
  val incomeTotals = mutableListOf<Int>()
  val expenseTotals = mutableListOf<Int>()
  val locale = if (language == AppLanguage.ID) {
    Locale.Builder().setLanguage("id").setRegion("ID").build()
  } else {
    Locale.US
  }
  val fmt = SimpleDateFormat("MMM", locale)
  for (month in 0..11) {
    val cal = Calendar.getInstance().apply {
      set(Calendar.YEAR, currentYear)
      set(Calendar.MONTH, month)
      set(Calendar.DAY_OF_MONTH, 1)
    }
    labels.add(fmt.format(cal.time))
    incomeTotals.add(income.sumOf { entry ->
      val date = parseDate(entry.date) ?: return@sumOf 0
      val dCal = Calendar.getInstance().apply { timeInMillis = date }
      if (dCal.get(Calendar.YEAR) == currentYear && dCal.get(Calendar.MONTH) == month) entry.amount else 0
    })
    expenseTotals.add(expense.sumOf { entry ->
      val date = parseDate(entry.date) ?: return@sumOf 0
      val dCal = Calendar.getInstance().apply { timeInMillis = date }
      if (dCal.get(Calendar.YEAR) == currentYear && dCal.get(Calendar.MONTH) == month) entry.amount else 0
    })
  }
  return ChartSeries(labels, incomeTotals, expenseTotals)
}

fun buildYearlySeries(income: List<MoneyEntry>, expense: List<MoneyEntry>, startYear: Int): ChartSeries {
  val now = Calendar.getInstance()
  val currentYear = now.get(Calendar.YEAR)
  val baseEnd = startYear + 4
  val shift = (currentYear - baseEnd).coerceAtLeast(0)
  val windowStart = startYear + shift
  val windowEnd = windowStart + 4

  val labels = mutableListOf<String>()
  val incomeTotals = mutableListOf<Int>()
  val expenseTotals = mutableListOf<Int>()
  for (year in windowStart..windowEnd) {
    labels.add(year.toString())
    incomeTotals.add(income.sumOf { entry ->
      val date = parseDate(entry.date) ?: return@sumOf 0
      val dCal = Calendar.getInstance().apply { timeInMillis = date }
      if (dCal.get(Calendar.YEAR) == year) entry.amount else 0
    })
    expenseTotals.add(expense.sumOf { entry ->
      val date = parseDate(entry.date) ?: return@sumOf 0
      val dCal = Calendar.getInstance().apply { timeInMillis = date }
      if (dCal.get(Calendar.YEAR) == year) entry.amount else 0
    })
  }
  return ChartSeries(labels, incomeTotals, expenseTotals)
}

fun totalsFromSeries(series: ChartSeries): Pair<Int, Int> {
  return Pair(series.income.sum(), series.expense.sum())
}

@Composable
fun LineChart(
  series: ChartSeries,
  modifier: Modifier = Modifier,
) {
  val colors = LocalAppColors.current
  val maxValue = (series.income + series.expense).maxOrNull()?.coerceAtLeast(1) ?: 1
  val animProgress = remember { Animatable(0f) }

  LaunchedEffect(series) {
    animProgress.snapTo(0f)
    animProgress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
    )
  }

  Column(modifier = modifier) {
    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
        .padding(horizontal = 8.dp, vertical = 12.dp),
    ) {
      val progress = animProgress.value
      val width = size.width
      val height = size.height
      val stepX = if (series.labels.size > 1) width / (series.labels.size - 1) else width

      fun getPoints(values: List<Int>): List<Offset> {
        return values.mapIndexed { index, value ->
          val x = index * stepX
          val normalizedY = (value.toFloat() / maxValue) * progress
          val y = height - (normalizedY * height)
          Offset(x, y.coerceIn(0f, height))
        }
      }

      val incomePoints = getPoints(series.income)
      val expensePoints = getPoints(series.expense)

      for (i in 0..4) {
        val y = height * (i / 4f)
        drawLine(
          color = colors.cardBorder.copy(alpha = 0.5f),
          start = Offset(0f, y),
          end = Offset(width, y),
          strokeWidth = 1f,
        )
      }

      val incomeLineColor = Color(0xFF10B981)
      val expenseLineColor = colors.danger

      fun drawArea(points: List<Offset>, color: Color) {
        if (points.size < 2) return
        val path = androidx.compose.ui.graphics.Path().apply {
          moveTo(points.first().x, height)
          points.forEach { lineTo(it.x, it.y) }
          lineTo(points.last().x, height)
          close()
        }
        drawPath(
          path = path,
          brush = Brush.verticalGradient(
            colors = listOf(color.copy(alpha = 0.20f * progress), color.copy(alpha = 0.01f)),
            startY = 0f,
            endY = height,
          ),
        )
      }

      drawArea(incomePoints, incomeLineColor)
      drawArea(expensePoints, expenseLineColor)

      fun drawSmoothLine(points: List<Offset>, color: Color) {
        if (points.size < 2) return
        val path = androidx.compose.ui.graphics.Path().apply {
          moveTo(points.first().x, points.first().y)
          for (i in 0 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val cx = (p0.x + p1.x) / 2f
            cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
          }
        }
        drawPath(
          path = path,
          color = color,
          style = Stroke(width = 2.5f.dp.toPx(), cap = StrokeCap.Round),
        )
      }

      drawSmoothLine(incomePoints, incomeLineColor)
      drawSmoothLine(expensePoints, expenseLineColor)

      if (progress > 0.6f) {
        val dotAlpha = ((progress - 0.6f) / 0.4f).coerceIn(0f, 1f)
        incomePoints.forEach { pt ->
          drawCircle(
            color = incomeLineColor.copy(alpha = dotAlpha),
            radius = 3.5f.dp.toPx(),
            center = pt,
          )
          drawCircle(
            color = Color.White.copy(alpha = dotAlpha),
            radius = 1.6f.dp.toPx(),
            center = pt,
          )
        }
        expensePoints.forEach { pt ->
          drawCircle(
            color = expenseLineColor.copy(alpha = dotAlpha),
            radius = 3.5f.dp.toPx(),
            center = pt,
          )
          drawCircle(
            color = Color.White.copy(alpha = dotAlpha),
            radius = 1.6f.dp.toPx(),
            center = pt,
          )
        }
      }
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      series.labels.forEach { label ->
        Text(text = label, color = colors.muted, fontSize = 11.sp)
      }
    }
  }
}
