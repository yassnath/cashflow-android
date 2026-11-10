package com.solvix.tabungan

internal fun insight(
  id: String,
  title: String,
  message: String,
  severity: InsightSeverity,
  category: InsightCategory,
  chips: List<String>,
  cta: String,
  target: Page,
  createdAt: String,
  timeframe: InsightTimeframe,
): InsightItem = InsightItem(
  id = id,
  title = title,
  message = message,
  severity = severity,
  category = category,
  metricChips = chips,
  actionCta = cta,
  actionTargetPage = target.name,
  createdAt = createdAt,
  timeframe = timeframe,
)

internal fun sumLastDays(entries: List<MoneyEntry>, days: Int, nowMillis: Long): Int {
  val start = nowMillis - (days - 1).toLong() * 24L * 60L * 60L * 1000L
  return entries.filter {
    val ts = parseDateTimeMillis(it.date) ?: return@filter false
    ts in start..nowMillis
  }.sumOf { it.amount }
}

internal fun sumBetweenDays(entries: List<MoneyEntry>, startDaysBack: Int, endDaysBack: Int, nowMillis: Long): Int {
  val start = nowMillis - endDaysBack.toLong() * 24L * 60L * 60L * 1000L
  val end = nowMillis - (startDaysBack - 1L) * 24L * 60L * 60L * 1000L
  return entries.filter {
    val ts = parseDateTimeMillis(it.date) ?: return@filter false
    ts in start..end
  }.sumOf { it.amount }
}

internal fun lastNWeeklySums(entries: List<MoneyEntry>, weeks: Int, nowMillis: Long): List<Double> {
  return (0 until weeks).map { index ->
    val end = nowMillis - (index * 7L) * 24L * 60L * 60L * 1000L
    sumLastDays(entries, 7, end).toDouble()
  }.reversed()
}
