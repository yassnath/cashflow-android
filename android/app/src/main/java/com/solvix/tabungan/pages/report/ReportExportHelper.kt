package com.solvix.tabungan

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun shareReportText(context: Context, title: String, series: ChartSeries, totals: Pair<Int, Int>) {
  val content = buildString {
    appendLine("📊 $title")
    appendLine("---------------------------")
    appendLine("⬆️ Total Pemasukan: ${formatRupiah(totals.first)}")
    appendLine("⬇️ Total Pengeluaran: ${formatRupiah(totals.second)}")
    appendLine("✨ Total Saldo: ${formatRupiah(totals.first - totals.second)}")
    appendLine("---------------------------")
    appendLine("Rincian Ringkas:")
    val takeCount = series.labels.size.coerceAtMost(6)
    val startIndex = (series.labels.size - takeCount).coerceAtLeast(0)
    for (i in startIndex until series.labels.size) {
      val label = series.labels.getOrNull(i).orEmpty()
      val inc = series.income.getOrNull(i) ?: 0
      val exp = series.expense.getOrNull(i) ?: 0
      appendLine("• $label: Pemasukan ${formatRupiah(inc)} | Pengeluaran ${formatRupiah(exp)}")
    }
  }
  val intent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_SUBJECT, title)
    putExtra(Intent.EXTRA_TEXT, content)
  }
  context.startActivity(Intent.createChooser(intent, "Bagikan Laporan Keuangan via"))
}

fun openExportedFile(context: Context, uri: Uri, mimeType: String) {
  val intent = Intent(Intent.ACTION_VIEW).apply {
    setDataAndType(uri, mimeType)
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
  }
  try {
    context.startActivity(intent)
  } catch (_: Exception) {
    // Fallback if viewer is not available
  }
}

fun writeReportPdf(
  context: Context,
  uri: Uri,
  title: String,
  series: ChartSeries,
  totals: Pair<Int, Int>,
) {
  val document = PdfDocument()
  val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
  val page = document.startPage(pageInfo)
  val canvas = page.canvas
  val paint = Paint().apply {
    color = android.graphics.Color.BLACK
    textSize = 18f
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
  }
  var y = 40f
  canvas.drawText(title, 40f, y, paint)
  y += 22f
  paint.textSize = 11f
  paint.typeface = Typeface.DEFAULT
  val generated = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US).format(Date())
  canvas.drawText("Generated: $generated", 40f, y, paint)
  y += 22f

  paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
  canvas.drawText("Label", 40f, y, paint)
  canvas.drawText("Income", 240f, y, paint)
  canvas.drawText("Expense", 360f, y, paint)
  canvas.drawText("Balance", 480f, y, paint)
  y += 16f
  paint.typeface = Typeface.DEFAULT

  for (i in series.labels.indices) {
    val label = series.labels[i]
    val income = series.income.getOrNull(i) ?: 0
    val expense = series.expense.getOrNull(i) ?: 0
    val balance = income - expense
    canvas.drawText(label, 40f, y, paint)
    canvas.drawText(formatRupiah(income), 240f, y, paint)
    canvas.drawText(formatRupiah(expense), 360f, y, paint)
    canvas.drawText(formatRupiah(balance), 480f, y, paint)
    y += 16f
  }

  y += 12f
  paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
  canvas.drawText("Total Income: ${formatRupiah(totals.first)}", 40f, y, paint)
  y += 16f
  canvas.drawText("Total Expense: ${formatRupiah(totals.second)}", 40f, y, paint)
  y += 16f
  canvas.drawText("Total Balance: ${formatRupiah(totals.first - totals.second)}", 40f, y, paint)

  document.finishPage(page)
  context.contentResolver.openOutputStream(uri)?.use { output ->
    document.writeTo(output)
  }
  document.close()
}

fun writeReportCsv(
  context: Context,
  uri: Uri,
  title: String,
  series: ChartSeries,
  totals: Pair<Int, Int>,
) {
  context.contentResolver.openOutputStream(uri)?.use { output ->
    OutputStreamWriter(output).use { writer ->
      writer.appendLine(title)
      writer.appendLine("Label,Income,Expense,Balance")
      for (i in series.labels.indices) {
        val label = series.labels[i]
        val income = series.income.getOrNull(i) ?: 0
        val expense = series.expense.getOrNull(i) ?: 0
        val balance = income - expense
        writer.appendLine("$label,${formatRupiah(income)},${formatRupiah(expense)},${formatRupiah(balance)}")
      }
      writer.appendLine()
      writer.appendLine("Total Income,${formatRupiah(totals.first)}")
      writer.appendLine("Total Expense,${formatRupiah(totals.second)}")
      writer.appendLine("Total Balance,${formatRupiah(totals.first - totals.second)}")
      writer.flush()
    }
  }
}
