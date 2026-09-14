package com.solvix.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InsightsPage(
  insights: List<InsightItem>,
  defaultTimeframe: InsightTimeframe,
  onRefresh: (InsightTimeframe) -> Unit,
  onAction: (InsightItem) -> Unit,
  onFeedback: (InsightItem, Boolean) -> Unit,
  isRefreshing: Boolean,
  strings: AppStrings,
) {
  val colors = LocalAppColors.current
  var timeframe by rememberSaveable { mutableStateOf(defaultTimeframe) }
  var categoryFilter by rememberSaveable { mutableStateOf("all") }
  var severityFilter by rememberSaveable { mutableStateOf("all") }

  LaunchedEffect(defaultTimeframe) {
    timeframe = defaultTimeframe
  }

  val timeframeLabels = mapOf(
    InsightTimeframe.D7 to "7d",
    InsightTimeframe.D30 to "30d",
    InsightTimeframe.MONTH to strings["summary_range_month"],
    InsightTimeframe.YEAR to strings["summary_range_year"],
    InsightTimeframe.ALL to strings["summary_range_all"],
  )
  val categoryOptions = listOf("all", "spending", "income", "debt", "goals", "habits")
  val severityOptions = listOf("all", "info", "warn", "alert")
  val filtered = insights.filter { item ->
    val passTimeframe = timeframe == InsightTimeframe.ALL || item.timeframe == timeframe
    val passCategory = categoryFilter == "all" || item.category.name.equals(categoryFilter, ignoreCase = true)
    val passSeverity = severityFilter == "all" || item.severity.name.equals(severityFilter, ignoreCase = true)
    passTimeframe && passCategory && passSeverity
  }

  Column {
    SectionTitle(
      icon = themePageVisualIcon(LocalThemeName.current, Page.Insights),
      title = strings["section_insights_title"],
      subtitle = strings["section_insights_subtitle"],
    )
    AppCard {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AppDropdown(
          label = strings["insights_filter_timeframe"],
          placeholder = "7d",
          options = timeframeLabels.values.toList(),
          selected = timeframeLabels[timeframe].orEmpty(),
          onSelected = { selected ->
            timeframe = timeframeLabels.entries.firstOrNull { it.value == selected }?.key ?: InsightTimeframe.D7
          },
        )
        AppDropdown(
          label = strings["insights_filter_category"],
          placeholder = "all",
          options = categoryOptions,
          selected = categoryFilter,
          onSelected = { categoryFilter = it },
        )
        AppDropdown(
          label = strings["insights_filter_severity"],
          placeholder = "all",
          options = severityOptions,
          selected = severityFilter,
          onSelected = { severityFilter = it },
        )
        GradientButton(
          text = if (isRefreshing) strings["insights_refreshing"] else strings["insights_refresh"],
          enabled = !isRefreshing,
          onClick = { onRefresh(timeframe) },
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))
    if (filtered.isEmpty()) {
      AppCard {
        Text(text = strings["insights_empty"], color = colors.muted, fontSize = 13.sp)
      }
    } else {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        filtered.forEach { insight ->
          InsightCard(
            item = insight,
            onAction = { onAction(insight) },
            onThumbUp = { onFeedback(insight, true) },
            onThumbDown = { onFeedback(insight, false) },
          )
        }
      }
    }
  }
}

@Composable
private fun InsightCard(
  item: InsightItem,
  onAction: () -> Unit,
  onThumbUp: () -> Unit,
  onThumbDown: () -> Unit,
) {
  val colors = LocalAppColors.current
  val severityColor = when (item.severity) {
    InsightSeverity.INFO -> colors.accent2
    InsightSeverity.WARN -> Color(0xFFFF9800)
    InsightSeverity.ALERT -> colors.danger
  }
  AppCard(shape = RoundedCornerShape(AppDimens.radiusMd)) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(text = item.title, fontWeight = FontWeight.Bold, color = colors.text)
        Text(
          text = item.severity.name,
          color = severityColor,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
        )
      }
      Text(text = item.message, color = colors.muted, fontSize = 12.sp)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        item.metricChips.forEach { chip ->
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(999.dp))
              .background(colors.bg2)
              .border(1.dp, colors.cardBorder, RoundedCornerShape(999.dp))
              .padding(horizontal = 10.dp, vertical = 6.dp),
          ) {
            Text(text = chip, fontSize = 11.sp, color = colors.text)
          }
        }
      }
      Text(
        text = "${item.createdAt} • ${item.timeframe.name}",
        color = colors.muted,
        fontSize = 10.sp,
      )
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Box(
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.linearGradient(listOf(colors.accent, colors.accent2)))
            .clickable { onAction() },
          contentAlignment = Alignment.Center,
        ) {
          Text(text = item.actionCta, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(colors.bg2)
            .border(1.dp, colors.cardBorder, CircleShape)
            .clickable { onThumbUp() },
          contentAlignment = Alignment.Center,
        ) { Text("👍") }
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(colors.bg2)
            .border(1.dp, colors.cardBorder, CircleShape)
            .clickable { onThumbDown() },
          contentAlignment = Alignment.Center,
        ) { Text("👎") }
      }
    }
  }
}
