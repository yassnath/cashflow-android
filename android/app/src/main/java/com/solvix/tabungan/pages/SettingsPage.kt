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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppPinSecurityManager
import com.solvix.tabungan.components.AppConfirmationDialog
import com.solvix.tabungan.components.PinSecuritySettingsModal
import com.solvix.tabungan.components.PinSetupModal
import com.solvix.tabungan.pages.settings.BackupSettingsCard
import com.solvix.tabungan.pages.settings.CurrencySettingsCard
import com.solvix.tabungan.pages.settings.FullAppBackup

/**
 * Halaman Pengaturan (SettingsPage) yang rapi, modular, dan seragam:
 * - Seluruh bagian konsisten dibungkus dalam [AppCard] berdesain senada
 * - Menu PIN 6-digit membuka Modal Dialog khusus yang interaktif dan ramah pengguna
 * - Tipografi berjarak proporsional dan kontras tinggi, nyaman bagi anak muda maupun lansia
 */
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
  onResetAllData: () -> Unit = {},
  onToast: (String) -> Unit = {},
  userId: String = "",
) {
  val context = LocalContext.current
  val colors = LocalAppColors.current
  val deleteButtonRed = Color(0xFFD32F2F)

  var showPinManagementModal by remember { mutableStateOf(false) }
  var showPinSetupModal by remember { mutableStateOf(false) }
  var showResetConfirmDialog by remember { mutableStateOf(false) }

  val prefs = remember { context.getSharedPreferences("tabungan_prefs", Context.MODE_PRIVATE) }
  var langIndex by rememberSaveable { mutableIntStateOf(0) }
  var currentPassword by rememberSaveable { mutableStateOf("") }
  var newPassword by rememberSaveable { mutableStateOf("") }
  var confirmPassword by rememberSaveable { mutableStateOf("") }

  // Auto-Lock Timeout
  val autoLockKeys = listOf("off", "instant", "1min", "5min")
  val autoLockLabels = mapOf(
    "off" to (if (language == AppLanguage.ID) "Nonaktif" else "Off"),
    "instant" to (if (language == AppLanguage.ID) "Segera" else "Instant"),
    "1min" to (if (language == AppLanguage.ID) "1 Menit" else "1 Min"),
    "5min" to (if (language == AppLanguage.ID) "5 Menit" else "5 Min"),
  )
  var selectedAutoLockKey by remember {
    val raw = prefs.getString("auto_lock_timeout", "off") ?: "off"
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

  Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    LaunchedEffect(language) {
      langIndex = if (language == AppLanguage.ID) 0 else 1
    }

    SectionTitle(
      icon = themePageVisualIcon(LocalThemeName.current, Page.Settings),
      title = strings["section_settings_title"],
      subtitle = strings["section_settings_subtitle"],
    )

    // 1. KARTU KEAMANAN & PRIVASI
    AppCard {
      SettingsCardHeader(
        icon = Icons.Rounded.Security,
        title = strings["settings_security"].ifBlank { "Keamanan & Kunci Aplikasi" },
        subtitle = "Amankan akses ke seluruh data finansial dan riwayat transaksi",
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Row PIN 6-Digit (Klik membuka modal pop-up dedicated)
      val hasSecurityPin = remember(AppPinSecurityManager.versionState.intValue) {
        AppPinSecurityManager.isPinSet(context)
      }
      SettingsActionRow(
        icon = Icons.Rounded.Lock,
        title = "Kunci PIN 6-Digit",
        subtitle = if (hasSecurityPin) "PIN aktif (tersimpan aman di cloud & HP)" else "Amankan aplikasi dengan PIN 6-angka",
        trailing = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                  if (hasSecurityPin) Color(0xFF2EC4B6).copy(alpha = 0.15f)
                  else colors.bg2
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
              Text(
                text = if (hasSecurityPin) "AKTIF" else "NONAKTIF",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (hasSecurityPin) Color(0xFF2EC4B6) else colors.muted,
              )
            }
            Icon(
              imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
              contentDescription = "Detail",
              tint = colors.muted,
              modifier = Modifier.size(18.dp),
            )
          }
        },
        onClick = { showPinManagementModal = true },
      )

      SettingsDivider()

      // Row Sidik Jari (Biometrik)
      SettingsToggleRow(
        icon = Icons.Rounded.Fingerprint,
        title = strings["settings_fingerprint"],
        subtitle = strings["settings_fingerprint_desc"],
        checked = fingerprintEnabled,
        onCheckedChange = onFingerprintToggle,
      )

      SettingsDivider()

      // Row Waktu Kunci Otomatis (Timeout)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = strings["settings_auto_lock_title"].ifBlank { "Waktu Kunci Otomatis" },
              fontSize = 13.sp,
              color = colors.text,
              fontWeight = FontWeight.SemiBold,
            )
            Text(
              text = strings["settings_auto_lock_desc"].ifBlank { "Minta kunci keamanan jika aplikasi ditinggalkan" },
              color = colors.muted,
              fontSize = 11.sp,
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          autoLockKeys.forEach { key ->
            val isSelected = key == selectedAutoLockKey
            val label = autoLockLabels[key] ?: key
            Box(
              modifier = Modifier
                .weight(1f)
                .height(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) colors.accent else colors.bg2)
                .clickable {
                  selectedAutoLockKey = key
                  prefs.edit().putString("auto_lock_timeout", key).apply()
                },
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else colors.text,
              )
            }
          }
        }
      }

      SettingsDivider()

      // Row Privasi Layar (FLAG_SECURE)
      SettingsToggleRow(
        icon = Icons.Rounded.Visibility,
        title = strings["settings_privacy_screen_title"].ifBlank { "Privasi Layar (Anti-Screenshot)" },
        subtitle = strings["settings_privacy_screen_desc"].ifBlank { "Sembunyikan saldo saat beralih aplikasi atau tangkapan layar" },
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

    // 2. KARTU NOTIFIKASI & PENGINGAT
    AppCard {
      SettingsCardHeader(
        icon = Icons.Rounded.Notifications,
        title = strings["settings_notification_title"].ifBlank { "Notifikasi & Pengingat" },
        subtitle = "Kelola jadwal notifikasi agar tidak lupa mencatat keuangan harian",
      )

      Spacer(modifier = Modifier.height(14.dp))

      SettingsToggleRow(
        icon = Icons.Rounded.Notifications,
        title = strings["settings_daily_reminder_title"].ifBlank { "Pengingat Harian (Pukul 20:00)" },
        subtitle = strings["settings_daily_reminder_desc"].ifBlank { "Kirim notifikasi setiap malam agar tidak lupa mencatat pengeluaran hari ini" },
        checked = dailyReminderEnabled,
        onCheckedChange = {
          dailyReminderEnabled = it
          DailyReminderHelper.setDailyReminder(context, it)
        },
      )
    }

    // 3. KARTU PREFERENSI BAHASA
    AppCard {
      SettingsCardHeader(
        icon = Icons.Rounded.Translate,
        title = strings["settings_language"].ifBlank { "Bahasa Aplikasi" },
        subtitle = "Pilih bahasa tampilan yang Anda sukai (Indonesia / English)",
      )

      Spacer(modifier = Modifier.height(14.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = if (language == AppLanguage.ID) "Bahasa Indonesia" else "English",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = colors.text,
        )
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

    // 4. KARTU FORMAT MATA UANG (MULTI-CURRENCY)
    CurrencySettingsCard()

    // 5. KARTU KECERDASAN BUATAN (AI INSIGHTS)
    AppCard {
      SettingsCardHeader(
        icon = Icons.Rounded.AutoAwesome,
        title = "Kecerdasan Buatan (CashFlow AI)",
        subtitle = "Fitur cerdas analisis pengeluaran & rekomendasi otomatis",
      )

      Spacer(modifier = Modifier.height(14.dp))

      SettingsToggleRow(
        icon = Icons.Rounded.AutoAwesome,
        title = strings["settings_ai_insights"],
        subtitle = strings["settings_ai_insights_desc"],
        checked = aiInsightsEnabled,
        onCheckedChange = onAiInsightsToggle,
      )

      SettingsDivider()

      SettingsToggleRow(
        icon = Icons.Rounded.Security,
        title = strings["settings_ai_private_mode"],
        subtitle = strings["settings_ai_private_mode_desc"],
        checked = aiInsightsPrivateMode,
        onCheckedChange = onAiInsightsPrivateModeToggle,
      )
    }

    // 6. KARTU PENCADANGAN & PEMULIHAN
    BackupSettingsCard(
      onExportBackup = onExportBackup,
      onRestoreBackup = onRestoreBackup,
      onToast = onToast,
    )

    // 7. KARTU KATA SANDI AKUN
    AppCard {
      SettingsCardHeader(
        icon = Icons.Rounded.Key,
        title = strings["settings_change_password"],
        subtitle = "Perbarui kata sandi akun untuk menjaga keamanan data di cloud",
      )

      Spacer(modifier = Modifier.height(14.dp))

      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AppTextField(strings["password_current"], value = currentPassword, onValueChange = { currentPassword = it }, isPassword = true)
        AppTextField(strings["password_new"], value = newPassword, onValueChange = { newPassword = it }, isPassword = true)
        AppTextField(strings["password_confirm"], value = confirmPassword, onValueChange = { confirmPassword = it }, isPassword = true)
        GradientButton(text = strings["save_password"], onClick = {
          onChangePassword(currentPassword, newPassword, confirmPassword)
        })
      }
    }

    // 8. KARTU ZONA BAHAYA (RESET & HAPUS AKUN)
    AppCard {
      SettingsCardHeader(
        icon = Icons.Rounded.Warning,
        title = "Zona Bahaya & Reset Data",
        subtitle = "Tindakan di bawah ini bersifat permanen dan tidak dapat dibatalkan",
        isDanger = true,
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Tombol Reset Data
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = strings["settings_reset_title"].ifBlank { "Reset Data Transaksi ke 0" },
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp,
          color = colors.text,
        )
        Text(
          text = strings["settings_reset_desc"].ifBlank {
            "Hapus seluruh riwayat transaksi pemasukan, pengeluaran, transfer, dan saldo awal. Semua saldo akan kembali ke 0."
          },
          color = colors.muted,
          fontSize = 11.sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.danger.copy(alpha = 0.12f))
            .border(1.dp, colors.danger.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .clickable { showResetConfirmDialog = true },
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = strings["settings_reset_btn"].ifBlank { "🗑️ Reset Data ke 0" },
            color = colors.danger,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
          )
        }
      }

      SettingsDivider()

      // Tombol Hapus Akun
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = strings["settings_delete_account"],
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp,
          color = deleteButtonRed,
        )
        Text(
          text = strings["settings_delete_account_desc"],
          color = colors.muted,
          fontSize = 11.sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (canDeleteAccount) deleteButtonRed else colors.muted.copy(alpha = 0.35f))
            .clickable(enabled = canDeleteAccount, onClick = onDeleteAccount),
          contentAlignment = Alignment.Center,
        ) {
          Text(text = strings["delete_account_action"], color = Color.White, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))
  }

  // MODAL 1: Dialog Pengaturan PIN 6-Digit
  if (showPinManagementModal) {
    PinSecuritySettingsModal(
      onDismiss = { showPinManagementModal = false },
      onOpenPinSetup = { showPinSetupModal = true },
      onPinRemoved = {
        onToast("PIN keamanan berhasil dihapus dari cloud & HP")
      },
      userId = userId,
    )
  }

  // MODAL 2: Keypad Buat / Ubah PIN 6-Digit
  if (showPinSetupModal) {
    PinSetupModal(
      onDismiss = { showPinSetupModal = false },
      onPinSaved = { pin ->
        AppPinSecurityManager.setPin(context, userId, pin)
        onToast("PIN keamanan berhasil disimpan ke database cloud & perangkat!")
      },
    )
  }

  // MODAL 3: Dialog Konfirmasi Reset Data
  if (showResetConfirmDialog) {
    AppConfirmationDialog(
      title = strings["settings_reset_confirm_title"].ifBlank { "Reset Semua Data Transaksi?" },
      message = strings["settings_reset_confirm_message"].ifBlank {
        "Tindakan ini akan menghapus permanen seluruh transaksi pemasukan, pengeluaran, transfer, dan saldo awal. Saldo akan kembali menjadi Rp 0. Lanjutkan?"
      },
      confirmText = strings["settings_reset_confirm_action"].ifBlank { "Ya, Hapus Semua" },
      cancelText = strings["confirm_cancel"].ifBlank { "Batal" },
      isDestructive = true,
      onConfirm = {
        showResetConfirmDialog = false
        onResetAllData()
      },
      onDismiss = { showResetConfirmDialog = false },
    )
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Komponen Helper Internal untuk Menjamin Keseragaman Tampilan Setting
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsCardHeader(
  icon: ImageVector,
  title: String,
  subtitle: String,
  isDanger: Boolean = false,
) {
  val colors = LocalAppColors.current
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .background(
          if (isDanger) colors.danger.copy(alpha = 0.15f)
          else colors.accent.copy(alpha = 0.15f)
        ),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isDanger) colors.danger else colors.accent,
        modifier = Modifier.size(20.dp),
      )
    }

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = if (isDanger) colors.danger else colors.text,
      )
      Text(
        text = subtitle,
        fontSize = 11.sp,
        color = colors.muted,
        lineHeight = 15.sp,
      )
    }
  }
}

@Composable
private fun SettingsActionRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  trailing: @Composable () -> Unit,
  onClick: () -> Unit,
) {
  val colors = LocalAppColors.current
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .clickable { onClick() }
      .padding(vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(colors.bg2),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = colors.accent,
          modifier = Modifier.size(17.dp),
        )
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = colors.text,
        )
        Text(
          text = subtitle,
          fontSize = 11.sp,
          color = colors.muted,
          lineHeight = 15.sp,
        )
      }
    }

    trailing()
  }
}

@Composable
private fun SettingsToggleRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  val colors = LocalAppColors.current
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(colors.bg2),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (checked) colors.accent else colors.muted,
          modifier = Modifier.size(17.dp),
        )
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = colors.text,
        )
        Text(
          text = subtitle,
          fontSize = 11.sp,
          color = colors.muted,
          lineHeight = 15.sp,
        )
      }
    }

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
    )
  }
}

@Composable
private fun SettingsDivider() {
  val colors = LocalAppColors.current
  HorizontalDivider(
    modifier = Modifier.padding(vertical = 8.dp),
    thickness = 0.8.dp,
    color = colors.cardBorder.copy(alpha = 0.5f),
  )
}
