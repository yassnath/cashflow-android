package com.solvix.tabungan

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReceiptParseResult(
  val amount: Long?,
  val date: String?,
  val merchant: String?,
  val rawText: String,
)

object ReceiptOcrHelper {

  fun processReceiptImage(
    context: Context,
    imageUri: Uri,
    onResult: (ReceiptParseResult) -> Unit,
  ) {
    try {
      val image = InputImage.fromFilePath(context, imageUri)
      val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

      recognizer.process(image)
        .addOnSuccessListener { visionText ->
          val raw = visionText.text
          val result = parseReceiptText(raw)
          onResult(result)
        }
        .addOnFailureListener {
          onResult(ReceiptParseResult(null, null, null, ""))
        }
    } catch (_: Exception) {
      onResult(ReceiptParseResult(null, null, null, ""))
    }
  }

  fun parseReceiptText(rawText: String): ReceiptParseResult {
    if (rawText.isBlank()) return ReceiptParseResult(null, null, null, "")

    val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

    var detectedAmount: Long? = null
    var detectedDate: String? = null
    var detectedMerchant: String? = null

    // 1. Ekstraksi Nama Toko / Merchant (biasanya ada di baris 1-3 teratas)
    val candidateMerchant = lines.take(4).firstOrNull { line ->
      val l = line.lowercase()
      line.length in 3..40 &&
        !l.contains("selamat datang") &&
        !l.contains("struk") &&
        !l.contains("terima kasih") &&
        !l.contains("jl.") &&
        !l.contains("jalan") &&
        !l.contains("telp") &&
        !line.all { it.isDigit() || it in "-./: " }
    }
    if (candidateMerchant != null) {
      detectedMerchant = candidateMerchant.take(35)
    }

    // 2. Ekstraksi Tanggal Transaksi (misal: 12/05/2024 atau 12-05-24 atau 12 May 2024)
    val dateRegex = Regex("""\b([0-3]?[0-9][\/\-.][0-1]?[0-9][\/\-.](?:20)?[0-9]{2})\b""")
    for (line in lines) {
      val match = dateRegex.find(line)
      if (match != null) {
        val rawDate = match.groupValues[1]
        // Normalisasi separator ke '-'
        val normalized = rawDate.replace('/', '-').replace('.', '-')
        val parts = normalized.split('-')
        if (parts.size == 3) {
          val day = parts[0].padStart(2, '0')
          val month = parts[1].padStart(2, '0')
          var year = parts[2]
          if (year.length == 2) year = "20$year"
          detectedDate = "$day-$month-$year"
          break
        }
      }
    }

    // 3. Ekstraksi Total Nominal Pembayaran
    // Prioritaskan baris yang mengandung kata kunci total/bayar
    val totalKeywords = listOf("total", "grand total", "tagihan", "jumlah", "bayar", "subtotal", "tunai", "cash", "debit")
    val allDetectedAmounts = mutableListOf<Long>()

    for (line in lines) {
      val lower = line.lowercase()
      val hasKeyword = totalKeywords.any { lower.contains(it) }

      // Cari angka di baris ini
      val cleaned = line.replace("rp", "", ignoreCase = true)
        .replace("idr", "", ignoreCase = true)
        .trim()

      val numMatches = Regex("""(?:[0-9]{1,3}[.,])*[0-9]{3,}|\b[0-9]{4,8}\b""").findAll(cleaned)
      for (m in numMatches) {
        val digitsOnly = m.value.replace(".", "").replace(",", "")
        val parsed = digitsOnly.toLongOrNull() ?: 0L
        if (parsed in 500..500_000_000L) {
          allDetectedAmounts.add(parsed)
          if (hasKeyword && detectedAmount == null) {
            detectedAmount = parsed
          }
        }
      }
    }

    // Jika belum ketemu di baris keyword, ambil nominal terbesar yang masuk akal
    if (detectedAmount == null && allDetectedAmounts.isNotEmpty()) {
      detectedAmount = allDetectedAmounts.maxOrNull()
    }

    return ReceiptParseResult(
      amount = detectedAmount,
      date = detectedDate,
      merchant = detectedMerchant,
      rawText = rawText,
    )
  }
}
