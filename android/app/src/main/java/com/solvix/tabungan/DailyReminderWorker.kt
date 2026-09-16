package com.solvix.tabungan

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DailyReminderWorker(
  appContext: Context,
  params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

  override suspend fun doWork(): Result {
    val prefs = applicationContext.getSharedPreferences("tabungan_prefs", Context.MODE_PRIVATE)
    val isEnabled = prefs.getBoolean("daily_reminder_enabled", true)
    if (!isEnabled) return Result.success()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      val granted = ContextCompat.checkSelfPermission(
        applicationContext,
        Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED
      if (!granted) return Result.success()
    }

    sendReminderNotification()
    return Result.success()
  }

  private fun sendReminderNotification() {
    val channelId = "daily_reminder_channel"
    val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        channelId,
        "Pengingat Harian Keuangan",
        NotificationManager.IMPORTANCE_DEFAULT,
      ).apply {
        description = "Mengingatkan untuk mencatat transaksi harian"
      }
      notificationManager.createNotificationChannel(channel)
    }

    val intent = Intent(applicationContext, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
      applicationContext,
      0,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0),
    )

    val notification = NotificationCompat.Builder(applicationContext, channelId)
      .setSmallIcon(R.drawable.logo2)
      .setContentTitle("Waktunya Catat Keuangan! 📊")
      .setContentText("Jangan lupa catat pengeluaran & pemasukan hari ini agar keuangan tetap sehat.")
      .setStyle(NotificationCompat.BigTextStyle().bigText("Halo! Luangkan 1 menit untuk mencatat transaksi hari ini. Disiplin finansial dimulai dari catatan kecil yang konsisten! ✨"))
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setContentIntent(pendingIntent)
      .setAutoCancel(true)
      .build()

    try {
      NotificationManagerCompat.from(applicationContext).notify(1001, notification)
    } catch (_: SecurityException) {
      // Permission might have been revoked
    }
  }
}

object DailyReminderHelper {
  private const val WORK_NAME = "cashflow_daily_reminder"

  fun setDailyReminder(context: Context, enabled: Boolean) {
    val prefs = context.getSharedPreferences("tabungan_prefs", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("daily_reminder_enabled", enabled).apply()

    val workManager = WorkManager.getInstance(context)
    if (!enabled) {
      workManager.cancelUniqueWork(WORK_NAME)
      return
    }

    // Hitung delay hingga pukul 20:00 (8 PM)
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, 20)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }

    if (now.after(target)) {
      target.add(Calendar.DAY_OF_YEAR, 1)
    }

    val initialDelay = target.timeInMillis - now.timeInMillis

    val workRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
      .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
      .build()

    workManager.enqueueUniquePeriodicWork(
      WORK_NAME,
      ExistingPeriodicWorkPolicy.UPDATE,
      workRequest,
    )
  }
}
