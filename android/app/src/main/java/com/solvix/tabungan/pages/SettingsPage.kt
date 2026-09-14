package com.solvix.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppCard
import com.solvix.tabungan.AppLanguage
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.AppTextField
import com.solvix.tabungan.ChipToggle
import com.solvix.tabungan.GradientButton
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.LocalThemeName
import com.solvix.tabungan.Page
import com.solvix.tabungan.SectionTitle
import com.solvix.tabungan.themePageIcon

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
) {
  val colors = LocalAppColors.current
  val deleteButtonRed = Color(0xFFD32F2F)
  var langIndex by rememberSaveable { mutableIntStateOf(0) }
  var currentPassword by rememberSaveable { mutableStateOf("") }
  var newPassword by rememberSaveable { mutableStateOf("") }
  var confirmPassword by rememberSaveable { mutableStateOf("") }

  Column {
    LaunchedEffect(language) {
      langIndex = if (language == AppLanguage.ID) 0 else 1
    }
    SectionTitle(icon = themePageIcon(LocalThemeName.current, Page.Settings), title = strings["section_settings_title"], subtitle = strings["section_settings_subtitle"])
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
