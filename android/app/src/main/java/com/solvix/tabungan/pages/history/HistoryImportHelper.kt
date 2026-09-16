package com.solvix.tabungan

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader

object HistoryImportHelper {

  data class ImportResult(
    val successEntries: List<MoneyEntry>,
    val failedRowCount: Int,
    val totalRows: Int,
  )

  fun parseCsvFromUri(context: Context, uri: Uri): ImportResult {
    val entries = mutableListOf<MoneyEntry>()
    var failedCount = 0
    var rowCount = 0

    try {
      context.contentResolver.openInputStream(uri)?.use { inputStream ->
        BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
          var line = reader.readLine()
          // Check if first line is header
          if (line != null && (line.contains("Tanggal", ignoreCase = true) || line.contains("Date", ignoreCase = true))) {
            line = reader.readLine()
          }

          while (line != null) {
            val trimmed = line.trim().removePrefix("\uFEFF")
            if (trimmed.isNotBlank()) {
              rowCount++
              val parsed = parseCsvLine(trimmed)
              if (parsed != null) {
                entries.add(parsed)
              } else {
                failedCount++
              }
            }
            line = reader.readLine()
          }
        }
      }
    } catch (_: Exception) {
      // Error reading stream
    }

    return ImportResult(
      successEntries = entries,
      failedRowCount = failedCount,
      totalRows = rowCount,
    )
  }

  private fun parseCsvLine(line: String): MoneyEntry? {
    val tokens = splitCsvLine(line)
    if (tokens.size < 4) return null

    // Support formats:
    // Format A (from export): No, Tanggal, Tipe, Kategori, Nominal, Metode, Bank, Catatan
    // Format B: Tanggal, Tipe, Kategori, Nominal, Catatan
    val isExportFormat = tokens.size >= 8 && tokens[0].toIntOrNull() != null

    val date: String
    val typeStr: String
    val category: String
    val amountStr: String
    val method: String
    val bank: String
    val note: String

    if (isExportFormat) {
      date = tokens[1]
      typeStr = tokens[2]
      category = tokens[3]
      amountStr = tokens[4]
      method = tokens[5]
      bank = tokens[6]
      note = tokens.getOrElse(7) { "" }
    } else {
      date = tokens[0]
      typeStr = tokens[1]
      category = tokens[2]
      amountStr = tokens[3]
      method = tokens.getOrElse(4) { "Tunai" }
      bank = tokens.getOrElse(5) { "-" }
      note = tokens.getOrElse(6) { "" }
    }

    val amount = parseAmount(amountStr)
    if (amount <= 0) return null

    val type = if (isIncomeType(typeStr)) EntryType.Income else EntryType.Expense
    val normalizedDate = if (date.isNotBlank()) date else nowJakartaText()

    return MoneyEntry(
      id = UUIDString(),
      type = type,
      amount = amount,
      date = normalizedDate,
      category = category.ifBlank { "Lainnya" },
      note = note,
      sourceOrMethod = method.ifBlank { "Tunai" },
      channelOrBank = bank.ifBlank { "-" },
      createdAt = nowJakartaText(),
    )
  }

  private fun splitCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    val sb = StringBuilder()
    var inQuotes = false

    for (char in line) {
      when {
        char == '\"' -> inQuotes = !inQuotes
        (char == ',' || char == ';') && !inQuotes -> {
          result.add(sb.toString().trim().removeSurrounding("\""))
          sb.clear()
        }
        else -> sb.append(char)
      }
    }
    result.add(sb.toString().trim().removeSurrounding("\""))
    return result
  }
}
