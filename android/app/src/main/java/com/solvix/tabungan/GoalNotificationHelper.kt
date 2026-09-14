package com.solvix.tabungan

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

private const val GOAL_REACHED_CHANNEL_ID = "goal_reached_channel"
private const val GOAL_DEADLINE_WORK_TAG = "goal_deadline_worker"

fun showGoalReachedNotification(context: Context, goal: DreamEntry, strings: AppStrings) {
  val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    val channel = NotificationChannel(
      GOAL_REACHED_CHANNEL_ID,
      strings["goal_reached_notification_title"],
      NotificationManager.IMPORTANCE_DEFAULT,
    ).apply {
      description = strings["goal_reached_notification_desc"]
    }
    notificationManager.createNotificationChannel(channel)
  }
  if (
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
    ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
  ) {
    return
  }
  val body = strings["goal_reached_notification_body"]
    .replace("{title}", goal.title.ifBlank { strings["label_goal"] })
    .replace(
      "{message}",
      if (goal.sourceType == "expense") strings["goal_reached_popup_expense"]
      else strings["goal_reached_popup_income_balance"],
    )
  val notification = NotificationCompat.Builder(context, GOAL_REACHED_CHANNEL_ID)
    .setSmallIcon(R.drawable.logo2)
    .setContentTitle(strings["goal_reached_notification_title"])
    .setContentText(body)
    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    .setAutoCancel(true)
    .build()
  NotificationManagerCompat.from(context).notify(goal.id.hashCode(), notification)
}

fun scheduleGoalDeadlineWorker(context: Context) {
  val zone = ZoneId.of("Asia/Jakarta")
  val now = ZonedDateTime.now(zone)
  val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zone)
  val initialDelay = Duration.between(now, nextMidnight).toMillis().coerceAtLeast(0)
  val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .build()
  val request = PeriodicWorkRequestBuilder<GoalDeadlineWorker>(24, TimeUnit.HOURS)
    .setConstraints(constraints)
    .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
    .build()
  WorkManager.getInstance(context)
    .enqueueUniquePeriodicWork(
      GOAL_DEADLINE_WORK_TAG,
      ExistingPeriodicWorkPolicy.UPDATE,
      request,
    )
}

fun cancelGoalDeadlineWorker(context: Context) {
  WorkManager.getInstance(context).cancelUniqueWork(GOAL_DEADLINE_WORK_TAG)
}
