package com.solvix.tabungan

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object HistoryExportHelper {

  fun exportToCsv(
    context: Context,
    entries: List<MoneyEntry>,
    filterTitle: String = "Riwayat_Transaksi",
  ): Uri {
    val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
    val timeTag = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val file = File(exportDir, "CashFlow_${filterTitle}_$timeTag.csv")

    FileOutputStream(file).use { fos ->
      OutputStreamWriter(fos, Charsets.UTF_8).use { writer ->
        // Write UTF-8 BOM for Excel compatibility
        writer.write("\uFEFF")
        writer.write("No,Tanggal,Tipe,Kategori,Nominal (Rp),Metode,Bank / Saluran,Catatan\n")

        entries.forEachIndexed { index, item ->
          val typeLabel = if (item.type == EntryType.Income) "Pemasukan" else "Pengeluaran"
          val cleanNote = item.note.replace("\"", "\"\"").replace("\n", " ")
          val cleanCat = item.category.replace("\"", "\"\"")
          val cleanMethod = item.sourceOrMethod.replace("\"", "\"\"")
          val cleanBank = item.channelOrBank.replace("\"", "\"\"")

          writer.write(
            "${index + 1},\"${item.date}\",\"$typeLabel\",\"$cleanCat\",${item.amount},\"$cleanMethod\",\"$cleanBank\",\"$cleanNote\"\n"
          )
        }
        writer.flush()
      }
    }

    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
  }

  fun exportToPdf(
    context: Context,
    entries: List<MoneyEntry>,
    periodText: String = "Periode Transaksi",
  ): Uri {
    val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
    val timeTag = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val file = File(exportDir, "CashFlow_Laporan_$timeTag.pdf")

    val pdfDocument = PdfDocument()
    val pageWidth = 595 // A4 standard width in points
    val pageHeight = 842 // A4 standard height in points
    var pageNumber = 1

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      textSize = 16f
      color = Color.rgb(20, 24, 40)
    }
    val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      textSize = 10f
      color = Color.rgb(100, 110, 130)
    }
    val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      textSize = 10f
      color = Color.rgb(40, 50, 80)
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      textSize = 9f
      color = Color.rgb(30, 35, 50)
    }
    val incomePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      textSize = 9f
      color = Color.rgb(38, 142, 60)
    }
    val expensePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      textSize = 9f
      color = Color.rgb(211, 47, 47)
    }
    val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.rgb(225, 230, 240)
      strokeWidth = 0.75f
    }
    val headerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.rgb(245, 247, 252)
    }

    val incomeSum = entries.filter { it.type == EntryType.Income }.sumOf { it.amount.toLong() }
    val expenseSum = entries.filter { it.type == EntryType.Expense }.sumOf { it.amount.toLong() }
    val netSum = incomeSum - expenseSum

    var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
    var page = pdfDocument.startPage(pageInfo)
    var canvas = page.canvas

    fun drawHeader(c: Canvas) {
      // Header brand
      c.drawText("CashFlow - Laporan Riwayat Transaksi", 36f, 45f, titlePaint)
      val dateStr = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.Builder().setLanguage("id").setRegion("ID").build()).format(Date())
      c.drawText("$periodText  •  Dicetak: $dateStr", 36f, 62f, subtitlePaint)

      // Summary mini box
      paint.color = Color.rgb(240, 244, 255)
      c.drawRoundRect(36f, 75f, pageWidth - 36f, 115f, 6f, 6f, paint)

      subtitlePaint.textSize = 8.5f
      c.drawText("Total Pemasukan: +Rp ${formatNumber(incomeSum)}", 48f, 98f, incomePaint)
      c.drawText("Total Pengeluaran: -Rp ${formatNumber(expenseSum)}", 210f, 98f, expensePaint)
      val netColor = if (netSum >= 0) Color.rgb(38, 142, 60) else Color.rgb(211, 47, 47)
      titlePaint.color = netColor
      titlePaint.textSize = 10f
      c.drawText("Saldo Bersih: Rp ${formatNumber(netSum)}", 400f, 98f, titlePaint)

      // Table Header
      val thY = 135f
      c.drawRect(36f, thY - 14f, pageWidth - 36f, thY + 8f, headerBgPaint)
      c.drawText("TANGGAL", 42f, thY, headerPaint)
      c.drawText("TIPE", 130f, thY, headerPaint)
      c.drawText("KATEGORI", 190f, thY, headerPaint)
      c.drawText("CATATAN", 310f, thY, headerPaint)
      c.drawText("NOMINAL", 490f, thY, headerPaint)
      c.drawLine(36f, thY + 8f, pageWidth - 36f, thY + 8f, linePaint)
    }

    drawHeader(canvas)
    var currentY = 160f

    entries.forEachIndexed { _, entry ->
      if (currentY > pageHeight - 60) {
        // Close page and start next page
        pdfDocument.finishPage(page)
        pageNumber++
        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas

        // Simple header on next pages
        canvas.drawText("CashFlow - Laporan Riwayat Transaksi (Hal. $pageNumber)", 36f, 40f, subtitlePaint)
        val thY = 60f
        canvas.drawRect(36f, thY - 14f, pageWidth - 36f, thY + 8f, headerBgPaint)
        canvas.drawText("TANGGAL", 42f, thY, headerPaint)
        canvas.drawText("TIPE", 130f, thY, headerPaint)
        canvas.drawText("KATEGORI", 190f, thY, headerPaint)
        canvas.drawText("CATATAN", 310f, thY, headerPaint)
        canvas.drawText("NOMINAL", 490f, thY, headerPaint)
        canvas.drawLine(36f, thY + 8f, pageWidth - 36f, thY + 8f, linePaint)
        currentY = 82f
      }

      // Draw Row
      val shortDate = if (entry.date.length >= 10) entry.date.take(10) else entry.date
      canvas.drawText(shortDate, 42f, currentY, bodyPaint)

      val isInc = entry.type == EntryType.Income
      val typeText = if (isInc) "Masuk" else "Keluar"
      val typeP = if (isInc) incomePaint else expensePaint
      canvas.drawText(typeText, 130f, currentY, typeP)

      val catText = if (entry.category.length > 20) entry.category.take(18) + "…" else entry.category
      canvas.drawText(catText, 190f, currentY, bodyPaint)

      val noteText = if (entry.note.length > 26) entry.note.take(24) + "…" else (entry.note.ifBlank { "-" })
      canvas.drawText(noteText, 310f, currentY, bodyPaint)

      val amountStr = "${if (isInc) "+" else "-"}Rp ${formatNumber(entry.amount.toLong())}"
      canvas.drawText(amountStr, 490f, currentY, typeP)

      canvas.drawLine(36f, currentY + 6f, pageWidth - 36f, currentY + 6f, linePaint)
      currentY += 22f
    }

    pdfDocument.finishPage(page)
    FileOutputStream(file).use { fos ->
      pdfDocument.writeTo(fos)
    }
    pdfDocument.close()

    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
  }

  fun shareExportedFile(context: Context, fileUri: Uri, mimeType: String, title: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
      type = mimeType
      putExtra(Intent.EXTRA_STREAM, fileUri)
      putExtra(Intent.EXTRA_SUBJECT, title)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, title).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
  }

  private fun formatNumber(number: Long): String {
    return String.format(Locale.US, "%,d", number).replace(',', '.')
  }
}
