package com.solvix.tabungan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class ModelsUtilsTest {

  @Test
  fun ensureDateHasTimeAddsFallbackOnlyWhenMissing() {
    assertEquals("12-02-2026 10:26", ensureDateHasTime("12-02-2026", "10:26"))
    assertEquals("12-02-2026 08:01", ensureDateHasTime("12-02-2026 08:01", "10:26"))
  }

  @Test
  fun parseDateTimeSupportsDayMonthYearAndTime() {
    val millis = parseDateTimeMillis("12-02-2026 10:26")
    assertNotNull(millis)
  }

  @Test
  fun filterByRangeMonthReturnsOnlyCurrentMonthItems() {
    val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.US)
    val todayMillis = formatter.parse("18-02-2026")!!.time
    val entries = listOf(
      MoneyEntry(type = EntryType.Income, amount = 100, date = "01-02-2026", category = "-", note = "-", sourceOrMethod = "-", channelOrBank = "-"),
      MoneyEntry(type = EntryType.Income, amount = 200, date = "10-02-2026", category = "-", note = "-", sourceOrMethod = "-", channelOrBank = "-"),
      MoneyEntry(type = EntryType.Income, amount = 300, date = "10-01-2026", category = "-", note = "-", sourceOrMethod = "-", channelOrBank = "-"),
    )

    val result = filterByRange(entries, SummaryRange.Month, todayMillis)
    assertEquals(2, result.size)
    assertTrue(result.all { it.date.contains("-02-2026") })
  }
}

