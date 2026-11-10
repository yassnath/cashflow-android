package com.solvix.tabungan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
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

  /**
   * Mengunggah foto bukti/struk transaksi ke Supabase Storage (bucket: receipts).
   * Menghasilkan URL publik cloud agar struk dapat diakses lintas perangkat.
   */
  suspend fun uploadReceiptToCloud(
    context: Context,
    localPathOrUri: String,
    userId: String,
  ): String? = withContext(Dispatchers.IO) {
    if (localPathOrUri.isBlank() || userId.isBlank()) return@withContext null
    if (localPathOrUri.startsWith("http://") || localPathOrUri.startsWith("https://")) {
      return@withContext localPathOrUri
    }

    try {
      val bytes = if (localPathOrUri.startsWith("/")) {
        val file = File(localPathOrUri)
        if (!file.exists()) return@withContext null
        file.readBytes()
      } else {
        val uri = Uri.parse(localPathOrUri)
        context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@withContext null
      }

      val fileName = "rcpt_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
      val rawSupabaseUrl = BuildConfig.SUPABASE_URL.trimEnd('/')
      val uploadUrl = "$rawSupabaseUrl/storage/v1/object/receipts/$userId/$fileName"
      val publicUrl = "$rawSupabaseUrl/storage/v1/object/public/receipts/$userId/$fileName"

      val conn = (URL(uploadUrl).openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        doOutput = true
        setRequestProperty("apikey", BuildConfig.SUPABASE_ANON_KEY)
        setRequestProperty("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
        setRequestProperty("Content-Type", "image/jpeg")
        setRequestProperty("x-upsert", "true")
        connectTimeout = 10000
        readTimeout = 15000
      }

      conn.outputStream.use { os ->
        os.write(bytes)
        os.flush()
      }

      val responseCode = conn.responseCode
      if (responseCode in 200..299) {
        publicUrl
      } else {
        null
      }
    } catch (_: Exception) {
      null
    }
  }

  /**
   * Memuat bitmap struk belanja dari file lokal, content URI, atau URL cloud Supabase.
   * Dilengkapi cache disk lokal otomatis agar tidak mendownload berulang kali.
   */
  suspend fun loadBitmap(context: Context, uriOrPath: String): Bitmap? = withContext(Dispatchers.IO) {
    if (uriOrPath.isBlank()) return@withContext null

    try {
      if (uriOrPath.startsWith("http://") || uriOrPath.startsWith("https://")) {
        val cacheDir = File(context.cacheDir, "receipt_cache").apply { if (!exists()) mkdirs() }
        val cacheFileName = "${uriOrPath.hashCode()}.jpg"
        val cacheFile = File(cacheDir, cacheFileName)

        if (cacheFile.exists() && cacheFile.length() > 0) {
          BitmapFactory.decodeFile(cacheFile.absolutePath)
        } else {
          val url = URL(uriOrPath)
          val conn = url.openConnection() as HttpURLConnection
          conn.connectTimeout = 8000
          conn.readTimeout = 8000
          conn.inputStream.use { input ->
            FileOutputStream(cacheFile).use { output ->
              input.copyTo(output)
            }
          }
          if (cacheFile.exists() && cacheFile.length() > 0) {
            BitmapFactory.decodeFile(cacheFile.absolutePath)
          } else {
            null
          }
        }
      } else if (uriOrPath.startsWith("/")) {
        val file = File(uriOrPath)
        if (file.exists()) {
          BitmapFactory.decodeFile(file.absolutePath)
        } else {
          null
        }
      } else {
        val uri = Uri.parse(uriOrPath)
        context.contentResolver.openInputStream(uri)?.use { stream ->
          BitmapFactory.decodeStream(stream)
        }
      }
    } catch (_: Exception) {
      null
    }
  }
}
