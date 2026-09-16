package com.solvix.tabungan

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.pages.settings.BackupSettingsCard
import com.solvix.tabungan.pages.settings.CurrencySettingsCard
import com.solvix.tabungan.pages.settings.FullAppBackup

@Composable
fun SettingsPage(
  fingerprintEnabled: Boolean,
  onFingerprintToggle: (Boolean) -> Unit,
  aiInsightsEnabled: Boolean,
  onAiInsightsToggle: (Boolean) -> Unit,
  aiInsightsPrivateMode: Boolean,
  onAiInsightsPrivateModeToggle: (Boolean) -> Unit,
  language: AppLanguage,
  onLanguageChange: (AppLanguage) -> Unit,
  strings: AppStrings,
  onChangePassword: (currentPassword: String, newPassword: String, confirmPassword: String) -> Unit,
  canDeleteAccount: Boolean,
  onDeleteAccount: () -> Unit,
  onExportBackup: () -> Unit = {},
  onRestoreBackup: (FullAppBackup) -> Unit = {},
  onToast: (String) -> Unit = {},
) {
  val context = LocalContext.current
  val colors = LocalAppColors.current
  val deleteButtonRed = Color(0xFFD32F2F)
  val prefs = remember { context.getSharedPreferences("tabungan_prefs", Context.MODE_PRIVATE) }

  var langIndex by rememberSaveable { mutableIntStateOf(0) }
  var currentPassword by rememberSaveable { mutableStateOf("") }
  var newPassword by rememberSaveable { mutableStateOf("") }
  var confirmPassword by rememberSaveable { mutableStateOf("") }

  // Auto-Lock Timeout (Tanpa PIN, murni biometrik timeout)
  val autoLockKeys = listOf("off", "instant", "1min", "5min")
  val autoLockLabels = mapOf(
    "off" to (if (language == AppLanguage.ID) "Nonaktif" else "Off"),
    "instant" to (if (language == AppLanguage.ID) "Segera" else "Instant"),
    "1min" to (if (language == AppLanguage.ID) "1 Menit" else "1 Min"),
    "5min" to (if (language == AppLanguage.ID) "5 Menit" else "5 Min"),
  )
  var selectedAutoLockKey by remember {
    val raw = prefs.getString("auto_lock_timeout", "off") ?: "off"
    // Backward compatibility for previously saved Indonesian strings
    val normalized = when (raw) {
      "Nonaktif" -> "off"
      "Segera" -> "instant"
      "1 Menit" -> "1min"
      "5 Menit" -> "5min"
      else -> raw
    }
    mutableStateOf(normalized)
  }

  // Pengingat Harian
  var dailyReminderEnabled by remember {
    mutableStateOf(prefs.getBoolean("daily_reminder_enabled", true))
  }

  // Privasi Layar (FLAG_SECURE)
  var privacyScreenEnabled by remember {
    mutableStateOf(prefs.getBoolean("privacy_screen_enabled", false))
  }

  Column {
    LaunchedEffect(language) {
      langIndex = if (language == AppLanguage.ID) 0 else 1
    }
    SectionTitle(
      icon = themePageVisualIcon(LocalThemeName.current, Page.Settings),
      title = strings["section_settings_title"],
      subtitle = strings["section_settings_subtitle"],
    )

    // Keamanan & Privasi
    AppCard {
      Text(text = strings["settings_security"], fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.text)
      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(text = strings["settings_fingerprint"], fontSize = 12.sp, color = colors.text)
          Text(
            text = strings["settings_fingerprint_desc"],
            color = colors.muted,
            fontSize = 11.sp,
          )
        }
        Switch(
          checked = fingerprintEnabled,
          onCheckedChange = onFingerprintToggle,
        )
      }

      // Auto-Lock Timeout Selector
      Spacer(modifier = Modifier.height(12.dp))
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = strings["settings_auto_lock_title"].ifBlank { "Kunci Otomatis Aplikasi" }, fontSize = 12.sp, color = colors.text, fontWeight = FontWeight.SemiBold)
        Text(text = strings["settings_auto_lock_desc"].ifBlank { "Minta sidik jari jika aplikasi ditinggalkan" }, color = colors.muted, fontSize = 11.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          autoLockKeys.forEach { key ->
            val isSelected = key == selectedAutoLockKey
            val label = autoLockLabels[key] ?: key
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) colors.accent else colors.bg2)
                .clickable {
                  selectedAutoLockKey = key
                  prefs.edit().putString("auto_lock_timeout", key).apply()
                }
                .padding(vertical = 7.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else colors.text,
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(text = strings["settings_ai_insights"], fontSize = 12.sp, color = colors.text)
          Text(
            text = strings["settings_ai_insights_desc"],
            color = colors.muted,
            fontSize = 11.sp,
          )
        }
        Switch(
          checked = aiInsightsEnabled,
          onCheckedChange = onAiInsightsToggle,
        )
      }
      Spacer(modifier = Modifier.height(12.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(text = strings["settings_ai_private_mode"], fontSize = 12.sp, color = colors.text)
          Text(
            text = strings["settings_ai_private_mode_desc"],
            color = colors.muted,
            fontSize = 11.sp,
          )
        }
        Switch(
          checked = aiInsightsPrivateMode,
          onCheckedChange = onAiInsightsPrivateModeToggle,
        )
      }
      Spacer(modifier = Modifier.height(12.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(text = strings["settings_privacy_screen_title"].ifBlank { "Privasi Layar (App Switcher)" }, fontSize = 12.sp, color = colors.text, fontWeight = FontWeight.SemiBold)
          Text(
            text = strings["settings_privacy_screen_desc"].ifBlank { "Sembunyikan saldo dan transaksi saat beralih aplikasi atau screenshot" },
            color = colors.muted,
            fontSize = 11.sp,
          )
        }
        Switch(
          checked = privacyScreenEnabled,
          onCheckedChange = {
            privacyScreenEnabled = it
            prefs.edit().putBoolean("privacy_screen_enabled", it).apply()
            val activity = context as? android.app.Activity
            if (it) {
              activity?.window?.setFlags(
                android.view.WindowManager.LayoutParams.FLAG_SECURE,
                android.view.WindowManager.LayoutParams.FLAG_SECURE,
              )
            } else {
              activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            }
          },
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Notifikasi & Pengingat Harian
    AppCard {
      Text(text = strings["settings_notification_title"].ifBlank { "Notifikasi & Pengingat" }, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.text)
      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(text = strings["settings_daily_reminder_title"].ifBlank { "Pengingat Harian (Pukul 20:00)" }, fontSize = 12.sp, color = colors.text, fontWeight = FontWeight.SemiBold)
          Text(
            text = strings["settings_daily_reminder_desc"].ifBlank { "Kirim notifikasi setiap malam agar tidak lupa mencatat pengeluaran hari ini" },
            color = colors.muted,
            fontSize = 11.sp,
          )
        }
        Switch(
          checked = dailyReminderEnabled,
          onCheckedChange = {
            dailyReminderEnabled = it
            DailyReminderHelper.setDailyReminder(context, it)
          },
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    AppCard {
      Text(text = strings["settings_language"], fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.text)
      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(text = strings["settings_language"], fontSize = 12.sp, color = colors.text)
        ChipToggle(
          options = listOf("IN", "EN"),
          selectedIndex = langIndex,
          onSelect = {
            langIndex = it
            onLanguageChange(if (it == 0) AppLanguage.ID else AppLanguage.EN)
          },
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Format Mata Uang (Multi-Currency)
    CurrencySettingsCard()

    Spacer(modifier = Modifier.height(12.dp))

    // Pencadangan & Pemulihan Data
    BackupSettingsCard(
      onExportBackup = onExportBackup,
      onRestoreBackup = onRestoreBackup,
      onToast = onToast,
    )

    Spacer(modifier = Modifier.height(12.dp))

    AppCard {
      Text(text = strings["settings_change_password"], fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.text)
      Spacer(modifier = Modifier.height(12.dp))
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AppTextField(strings["password_current"], value = currentPassword, onValueChange = { currentPassword = it }, isPassword = true)
        AppTextField(strings["password_new"], value = newPassword, onValueChange = { newPassword = it }, isPassword = true)
        AppTextField(strings["password_confirm"], value = confirmPassword, onValueChange = { confirmPassword = it }, isPassword = true)
        GradientButton(text = strings["save_password"], onClick = {
          onChangePassword(currentPassword, newPassword, confirmPassword)
        })
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    AppCard {
      Text(text = strings["settings_delete_account"], fontWeight = FontWeight.Bold, fontSize = 16.sp, color = deleteButtonRed)
      Spacer(modifier = Modifier.height(8.dp))
      Text(text = strings["settings_delete_account_desc"], color = colors.muted, fontSize = 12.sp)
      Spacer(modifier = Modifier.height(12.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 40.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(if (canDeleteAccount) deleteButtonRed else colors.muted.copy(alpha = 0.45f))
          .clickable(enabled = canDeleteAccount, onClick = onDeleteAccount),
        contentAlignment = Alignment.Center,
      ) {
        Text(text = strings["delete_account_action"], color = Color.White, fontWeight = FontWeight.Bold)
      }
    }
  }
}
