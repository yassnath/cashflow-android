package com.solvix.tabungan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppLanguage
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.MoneyEntry
import com.solvix.tabungan.SupabaseUser
import com.solvix.tabungan.parseCreatedAtMillis
import com.solvix.tabungan.parseDate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

data class ChartSeries(
  val labels: List<String>,
  val income: List<Int>,
  val expense: List<Int>,
)

data class DonutSlice(
  val label: String,
  val value: Int,
  val color: Color,
  val brush: Brush? = null,
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

fun buildCountrySlices(
  users: List<SupabaseUser>,
  strings: AppStrings,
  palette: List<Color>,
  limit: Int = 5,
): List<DonutSlice> {
  if (users.isEmpty()) return emptyList()
  val counts = users
    .map { it.country.trim().ifBlank { strings["admin_unknown_country"] } }
    .groupingBy { it }
    .eachCount()
  val sorted = counts.entries.sortedByDescending { it.value }
  val top = sorted.take(limit).map { it.key to it.value }.toMutableList()
  val otherCount = sorted.drop(limit).sumOf { it.value }
  if (otherCount > 0) {
    top.add(strings["admin_other"] to otherCount)
  }
  return top
    .filter { it.second > 0 }
    .mapIndexed { index, (label, value) ->
      DonutSlice(label = label, value = value, color = palette[index % palette.size])
    }
}

fun buildRecencySlices(
  users: List<SupabaseUser>,
  strings: AppStrings,
  palette: List<Color>,
): List<DonutSlice> {
  if (users.isEmpty()) return emptyList()
  val now = System.currentTimeMillis()
  var last7 = 0
  var last30 = 0
  var last90 = 0
  var older = 0
  var unknown = 0
  users.forEach { user ->
    val millis = parseCreatedAtMillis(user.createdAt)
    if (millis == null) {
      unknown += 1
    } else {
      val days = ((now - millis) / 86_400_000L).coerceAtLeast(0L)
      when {
        days <= 7 -> last7 += 1
        days <= 30 -> last30 += 1
        days <= 90 -> last90 += 1
        else -> older += 1
      }
    }
  }
  val buckets = mutableListOf(
    strings["admin_recency_7d"] to last7,
    strings["admin_recency_30d"] to last30,
    strings["admin_recency_90d"] to last90,
    strings["admin_recency_older"] to older,
  )
  if (unknown > 0) {
    buckets.add(strings["admin_recency_unknown"] to unknown)
  }
  return buckets
    .filter { it.second > 0 }
    .mapIndexed { index, (label, value) ->
      DonutSlice(label = label, value = value, color = palette[index % palette.size])
    }
}

@Composable
fun LineChart(series: ChartSeries) {
  val colors = LocalAppColors.current
  val incomeValues = series.income.map { it.toFloat() }
  val expenseValues = series.expense.map { it.toFloat() }
  val allValues = incomeValues + expenseValues
  val maxVal = (allValues.maxOrNull() ?: 1f).coerceAtLeast(1f)
  val minVal = (allValues.minOrNull() ?: 0f).coerceAtMost(0f)
  val range = (maxVal - minVal).coerceAtLeast(1f)

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(Color.White.copy(alpha = 0.05f))
        .padding(10.dp),
    ) {
      val width = size.width
      val height = size.height
      if (incomeValues.isEmpty()) return@Canvas
      val stepX = if (incomeValues.size == 1) width else width / (incomeValues.size - 1)
      fun buildPoints(values: List<Float>): List<Offset> {
        return values.mapIndexed { index, value ->
          val x = index * stepX
          val y = height - ((value - minVal) / range) * height
          Offset(x, y)
        }
      }
      val incomePoints = buildPoints(incomeValues)
      val expensePoints = buildPoints(expenseValues)
      for (i in 0 until incomePoints.size - 1) {
        drawLine(
          color = colors.accent2,
          start = incomePoints[i],
          end = incomePoints[i + 1],
          strokeWidth = 6f,
          cap = StrokeCap.Round,
        )
      }
      for (i in 0 until expensePoints.size - 1) {
        drawLine(
          color = colors.danger,
          start = expensePoints[i],
          end = expensePoints[i + 1],
          strokeWidth = 6f,
          cap = StrokeCap.Round,
        )
      }
      drawPoints(
        points = incomePoints,
        pointMode = androidx.compose.ui.graphics.PointMode.Points,
        color = colors.accent,
        strokeWidth = 12f,
      )
      drawPoints(
        points = expensePoints,
        pointMode = androidx.compose.ui.graphics.PointMode.Points,
        color = colors.danger,
        strokeWidth = 12f,
      )
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      series.labels.forEach { label ->
        Text(text = label, color = colors.muted, fontSize = 11.sp)
      }
    }
  }
}

@Composable
fun DonutChart(
  slices: List<DonutSlice>,
  modifier: Modifier = Modifier,
  strokeWidth: Dp = 18.dp,
  centerLabel: String? = null,
  centerSubLabel: String? = null,
  trackColor: Color = Color.Unspecified,
) {
  val colors = LocalAppColors.current
  val total = slices.sumOf { it.value }
  val ringColor = if (trackColor == Color.Unspecified) colors.bg2 else trackColor
  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
      drawArc(
        color = ringColor,
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = false,
        style = stroke,
      )
      if (total > 0) {
        var startAngle = -90f
        val gap = 2f
        slices.forEach { slice ->
          val sweep = (slice.value.toFloat() / total) * 360f
          val adjustedSweep = (sweep - gap).coerceAtLeast(0f)
          if (slice.brush != null) {
            drawArc(
              brush = slice.brush,
              startAngle = startAngle,
              sweepAngle = adjustedSweep,
              useCenter = false,
              style = stroke,
            )
          } else {
            drawArc(
              color = slice.color,
              startAngle = startAngle,
              sweepAngle = adjustedSweep,
              useCenter = false,
              style = stroke,
            )
          }
          startAngle += sweep
        }
      }
    }
    if (centerLabel != null) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = centerLabel, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.text)
        if (centerSubLabel != null) {
          Text(text = centerSubLabel, fontSize = 11.sp, color = colors.muted)
        }
      }
    }
  }
}

@Composable
fun DonutLegend(slices: List<DonutSlice>) {
  val colors = LocalAppColors.current
  val total = slices.sumOf { it.value }.coerceAtLeast(1)
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    slices.forEach { slice ->
      val percent = (slice.value.toFloat() / total) * 100f
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(slice.color),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "${slice.label} • ${slice.value} (${percent.roundToInt()}%)",
          color = colors.text,
          fontSize = 12.sp,
        )
      }
    }
  }
}
