package com.solvix.tabungan

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class LoanDueDateWorker(
  appContext: Context,
  params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

  override suspend fun doWork(): Result {
    return try {
      val securePrefs = SecurePrefs.open(applicationContext)
      val userId = securePrefs.getString("saved_user_id", "") ?: ""
      if (userId.isBlank()) return Result.success()

      val localJson = Json { ignoreUnknownKeys = true }
      val loans = loadLoanEntriesFromPrefs(userId, securePrefs, localJson)
      if (loans.isEmpty()) return Result.success()

      val today = LocalDate.now(ZoneId.of("Asia/Jakarta"))

      loans.forEach { loan ->
        val remaining = remainingLoanBalance(loan)
        if (remaining <= 0 || loan.dueDate.isBlank()) return@forEach

        val dueLocalDate = runCatching { LocalDate.parse(loan.dueDate.trim()) }.getOrNull()
          ?: return@forEach

        val daysLeft = ChronoUnit.DAYS.between(today, dueLocalDate)

        if (daysLeft in 0..3) {
          val typeLabel = if (loan.isReceivable) "Piutang" else "Hutang"
          val timeLabel = if (daysLeft == 0L) "HARI INI" else "$daysLeft hari lagi"
          val title = "⚠️ Pengingat $typeLabel: ${loan.title}"
          val message = "Jatuh tempo $timeLabel (${loan.dueDate}). Sisa tagihan: ${formatRupiah(remaining)}."
          val notifId = (loan.id.hashCode() and 0x7FFFFFFF)

          LoanNotificationHelper.showNotification(
            context = applicationContext,
            notificationId = notifId,
            title = title,
            content = message,
          )
        } else if (daysLeft < 0) {
          val typeLabel = if (loan.isReceivable) "Piutang" else "Hutang"
          val title = "🚨 $typeLabel Lewat Jatuh Tempo!"
          val message = "${loan.title} sudah melewati batas tempo (${loan.dueDate}). Sisa belum lunas: ${formatRupiah(remaining)}."
          val notifId = (loan.id.hashCode() and 0x7FFFFFFF)

          LoanNotificationHelper.showNotification(
            context = applicationContext,
            notificationId = notifId,
            title = title,
            content = message,
          )
        }
      }

      Result.success()
    } catch (_: Exception) {
      Result.retry()
    }
  }

  companion object {
    private const val WORK_TAG = "loan_due_date_worker"

    fun schedule(context: Context) {
      val workRequest = PeriodicWorkRequestBuilder<LoanDueDateWorker>(24, TimeUnit.HOURS)
        .addTag(WORK_TAG)
        .build()

      WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        WORK_TAG,
        ExistingPeriodicWorkPolicy.KEEP,
        workRequest,
      )
    }

    fun cancel(context: Context) {
      WorkManager.getInstance(context).cancelUniqueWork(WORK_TAG)
    }
  }
}
