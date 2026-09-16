package com.solvix.tabungan

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ReceiptStorageHelper {

  fun saveReceiptToInternalStorage(context: Context, sourceUri: Uri): String {
    return try {
      val receiptsDir = File(context.filesDir, "receipts").apply { mkdirs() }
      val fileName = "receipt_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
      val targetFile = File(receiptsDir, fileName)

      context.contentResolver.openInputStream(sourceUri)?.use { input ->
        FileOutputStream(targetFile).use { output ->
          input.copyTo(output)
        }
      }

      if (targetFile.exists() && targetFile.length() > 0) {
        targetFile.absolutePath
      } else {
        sourceUri.toString()
      }
    } catch (_: Exception) {
      sourceUri.toString()
    }
  }

  fun deleteReceiptFile(path: String) {
    if (path.startsWith("/")) {
      runCatching { File(path).delete() }
    }
  }
}
