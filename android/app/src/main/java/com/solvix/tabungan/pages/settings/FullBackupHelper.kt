package com.solvix.tabungan.pages.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FullBackupHelper {

  private val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
    encodeDefaults = true
  }

  fun createBackupFile(context: Context, backup: FullAppBackup): Uri? {
    return try {
      val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
      val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
      val fileName = "Tabungan_Backup_${timeStamp}.json"
      val file = File(exportDir, fileName)
      file.writeText(json.encodeToString(backup))

      FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    } catch (_: Exception) {
      null
    }
  }

  fun shareBackup(context: Context, backup: FullAppBackup) {
    val uri = createBackupFile(context, backup) ?: return
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
      type = "application/json"
      putExtra(Intent.EXTRA_STREAM, uri)
      putExtra(Intent.EXTRA_SUBJECT, "Backup Data Tabungan Solvix")
      putExtra(Intent.EXTRA_TEXT, "File cadangan penuh data Tabungan (${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())})")
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Bagikan / Simpan File Cadangan"))
  }

  fun readBackupFromUri(context: Context, uri: Uri): FullAppBackup? {
    return try {
      val content = context.contentResolver.openInputStream(uri)?.use { stream ->
        stream.bufferedReader().use { it.readText() }
      } ?: return null
      json.decodeFromString<FullAppBackup>(content)
    } catch (_: Exception) {
      null
    }
  }
}
