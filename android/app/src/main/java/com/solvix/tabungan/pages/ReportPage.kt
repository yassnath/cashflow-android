package com.solvix.tabungan

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.solvix.tabungan.pages.report.FinancialCalendarView
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppDimens
import com.solvix.tabungan.AppLanguage
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.ChartSeries
import com.solvix.tabungan.GhostButton
import com.solvix.tabungan.GradientButton
import com.solvix.tabungan.LineChart
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.LocalThemeName
import com.solvix.tabungan.MoneyEntry
import com.solvix.tabungan.Page
import com.solvix.tabungan.SectionTitle
import com.solvix.tabungan.buildMonthlySeries
import com.solvix.tabungan.buildYearlySeries
import com.solvix.tabungan.formatRupiah
import com.solvix.tabungan.isDarkTheme
import com.solvix.tabungan.themePageIcon
import com.solvix.tabungan.totalsFromSeries
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportPage(
  income: List<MoneyEntry>,
  expense: List<MoneyEntry>,
  strings: AppStrings,
  language: AppLanguage,
  startYear: Int,
  onToast: (String) -> Unit,
) {
  val colors = LocalAppColors.current
  val theme = LocalThemeName.current
  val context = LocalContext.current
  var modeIndex by rememberSaveable { mutableIntStateOf(0) }
  var reportTab by rememberSaveable { mutableStateOf("analytics") }
  val monthly = buildMonthlySeries(income, expense, language)
  val yearly = buildYearlySeries(income, expense, startYear)
  val series = if (modeIndex == 0) monthly else yearly
  val totals = if (modeIndex == 0) totalsFromSeries(monthly) else totalsFromSeries(yearly)
  val modeLabel = if (modeIndex == 0) strings["report_monthly"] else strings["report_yearly"]
  val reportTitle = "${strings["section_report_title"]} - $modeLabel"
  val dateTag = remember { SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) }

  val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
    if (uri == null) return@rememberLauncherForActivityResult
    try {
      writeReportPdf(context = context, uri = uri, title = reportTitle, series = series, totals = totals)
      openExportedFile(context, uri, "application/pdf")
      onToast(strings["export_success_pdf"])
    } catch (_: Exception) {
      onToast(strings["export_failed"])
    }
  }

  val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
    if (uri == null) return@rememberLauncherForActivityResult
    try {
      writeReportCsv(context = context, uri = uri, title = reportTitle, series = series, totals = totals)
      openExportedFile(context, uri, "text/csv")
      onToast(strings["export_success_csv"])
    } catch (_: Exception) {
      onToast(strings["export_failed"])
    }
  }

  val chartBackground = if (isDarkTheme(theme)) {
    Brush.linearGradient(listOf(Color(0xFF2A2F4D), Color(0xFF3A4270)))
  } else {
    Brush.linearGradient(listOf(Color(0xFF0F1220), Color(0xFF1B2034)))
  }

  Column {
    SectionTitle(icon = themePageVisualIcon(LocalThemeName.current, Page.Report), title = strings["section_report_title"])

    // Segmented Tab Pilihan Tampilan
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(AppDimens.radiusSm))
        .background(colors.bg2)
        .padding(4.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(AppDimens.radiusSm))
          .background(if (reportTab == "analytics") colors.accent else Color.Transparent)
          .clickable { reportTab = "analytics" }
          .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "📊 " + strings["report_tab_analytics"].ifBlank { "Grafik & Tren" },
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = if (reportTab == "analytics") Color.White else colors.muted,
        )
      }

      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(AppDimens.radiusSm))
          .background(if (reportTab == "calendar") colors.accent else Color.Transparent)
          .clickable { reportTab = "calendar" }
          .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "📅 " + strings["report_tab_calendar"].ifBlank { "Kalender Arus Kas" },
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = if (reportTab == "calendar") Color.White else colors.muted,
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (reportTab == "calendar") {
      FinancialCalendarView(
        income = income,
        expense = expense,
        strings = strings,
      )
    } else {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(12.dp, RoundedCornerShape(AppDimens.radiusLg))
          .clip(RoundedCornerShape(AppDimens.radiusLg))
          .background(chartBackground)
          .padding(16.dp),
      ) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val inactiveBrush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.08f)))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(999.dp))
              .background(if (modeIndex == 0) Brush.linearGradient(listOf(colors.accent, colors.accent2)) else inactiveBrush)
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = strings["report_monthly"],
              color = Color.White,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.clickable { modeIndex = 0 },
            )
          }
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(999.dp))
              .background(if (modeIndex == 1) Brush.linearGradient(listOf(colors.accent, colors.accent2)) else inactiveBrush)
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = strings["report_yearly"],
              color = Color.White,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.clickable { modeIndex = 1 },
            )
          }
        }

        LineChart(series = series)

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          ReportLine(label = strings["report_total_income"], value = formatRupiah(totals.first), positive = true)
          ReportLine(label = strings["report_total_expense"], value = formatRupiah(totals.second), positive = false)
          ReportLine(label = strings["report_total_balance"], value = formatRupiah(totals.first - totals.second), positive = true)
        }
      }
    }
    Spacer(modifier = Modifier.height(12.dp))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      GradientButton(
        text = strings["export_pdf"],
        onClick = { pdfLauncher.launch("CashFlow_${modeLabel.lowercase()}_$dateTag.pdf") },
      )
      GradientButton(
        text = strings["export_csv"],
        onClick = { csvLauncher.launch("CashFlow_${modeLabel.lowercase()}_$dateTag.csv") },
      )
      GhostButton(
        text = "Bagikan Laporan (Share)",
        onClick = { shareReportText(context, reportTitle, series, totals) },
      )
    }
    }
  }
}

@Composable
private fun ReportLine(label: String, value: String, positive: Boolean) {
  val colors = LocalAppColors.current
  val theme = LocalThemeName.current
  val labelColor = if (isDarkTheme(theme)) colors.text else Color.White.copy(alpha = 0.8f)
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(text = label, color = labelColor, fontSize = 13.sp)
    Text(
      text = value,
      fontWeight = FontWeight.Bold,
      color = if (positive) colors.accent2 else colors.danger,
      fontSize = 13.sp,
    )
  }
}

