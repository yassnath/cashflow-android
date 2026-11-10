package com.solvix.tabungan.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.solvix.tabungan.AppCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppDropdown
import com.solvix.tabungan.AppLanguage
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.AppTextField
import com.solvix.tabungan.AuthTab
import com.solvix.tabungan.AuthTabButton
import com.solvix.tabungan.DateField
import com.solvix.tabungan.GhostButton
import com.solvix.tabungan.GradientButton
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.ModalCard
import com.solvix.tabungan.ModalOverlay
import com.solvix.tabungan.OptionList
import com.solvix.tabungan.OutlineButton
import com.solvix.tabungan.SupabaseRepository
import com.solvix.tabungan.optionList
import kotlinx.coroutines.launch

@Composable
fun AuthModalSheet(
  strings: AppStrings,
  authTab: AuthTab,
  currentLang: AppLanguage,
  signInUsername: String,
  onSignInUsernameChange: (String) -> Unit,
  signInPassword: String,
  onSignInPasswordChange: (String) -> Unit,
  signUpName: String,
  onSignUpNameChange: (String) -> Unit,
  signUpEmail: String,
  onSignUpEmailChange: (String) -> Unit,
  signUpCountry: String,
  onSignUpCountryChange: (String) -> Unit,
  signUpBirthdate: String,
  onSignUpBirthdateChange: (String) -> Unit,
  signUpBio: String,
  onSignUpBioChange: (String) -> Unit,
  signUpUsername: String,
  onSignUpUsernameChange: (String) -> Unit,
  signUpPassword: String,
  onSignUpPasswordChange: (String) -> Unit,
  fingerprintEnabled: Boolean,
  hasRegistered: Boolean,
  biometricAllowed: Boolean,
  onTabSelect: (AuthTab) -> Unit,
  onFingerprintLogin: () -> Unit,
  onSignInClick: () -> Unit,
  onSignUpClick: () -> Unit,
  onCloseClick: () -> Unit,
) {
  val colors = LocalAppColors.current
  val scope = rememberCoroutineScope()
  var showForgotPasswordDialog by remember { mutableStateOf(false) }
  var resetEmail by remember { mutableStateOf("") }
  var resetLoading by remember { mutableStateOf(false) }
  var resetMessage by remember { mutableStateOf<String?>(null) }
  var resetSuccess by remember { mutableStateOf(false) }

  val configuration = LocalConfiguration.current
  val maxModalHeight = (configuration.screenHeightDp * 0.88f).dp
  val isSmallScreen = configuration.screenWidthDp < 360

  ModalOverlay {
    ModalCard(
      modifier = Modifier
        .fillMaxWidth(if (isSmallScreen) 0.96f else 0.92f)
        .widthIn(max = 480.dp)
        .heightIn(max = maxModalHeight),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.bg2),
        ) {
          AuthTabButton(
            text = strings["auth_sign_in"],
            active = authTab == AuthTab.SignIn,
            onClick = { onTabSelect(AuthTab.SignIn) },
          )
          AuthTabButton(
            text = strings["auth_sign_up"],
            active = authTab == AuthTab.SignUp,
            onClick = { onTabSelect(AuthTab.SignUp) },
          )
        }
        if (authTab == AuthTab.SignIn) {
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTextField(
              label = strings["label_signin_identifier"],
              value = signInUsername,
              onValueChange = onSignInUsernameChange,
            )
            AppTextField(
              label = strings["password"],
              value = signInPassword,
              onValueChange = onSignInPasswordChange,
              isPassword = true,
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End,
            ) {
              Text(
                text = strings["auth_forgot_password"].ifBlank { "Lupa kata sandi?" },
                color = colors.accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .clickable {
                    resetEmail = signInUsername.trim()
                    resetMessage = null
                    resetSuccess = false
                    showForgotPasswordDialog = true
                  }
                  .padding(vertical = 2.dp, horizontal = 4.dp),
              )
            }
            if (fingerprintEnabled && hasRegistered && biometricAllowed) {
              OutlineButton(text = strings["auth_fingerprint"]) {
                onFingerprintLogin()
              }
            }
            GradientButton(text = strings["auth_login"]) {
              onSignInClick()
            }
          }
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTextField(label = strings["label_name"], value = signUpName, onValueChange = onSignUpNameChange)
            AppTextField(label = strings["label_email"], value = signUpEmail, onValueChange = onSignUpEmailChange)
            AppDropdown(
              label = strings["label_country"],
              placeholder = strings["placeholder_country"],
              options = optionList(currentLang, OptionList.Countries),
              selected = signUpCountry,
              onSelected = onSignUpCountryChange,
            )
            DateField(
              label = strings["label_birthdate"],
              value = signUpBirthdate,
              onValueChange = onSignUpBirthdateChange,
              placeholder = strings["placeholder_date"],
            )
            AppTextField(
              label = strings["label_bio"],
              value = signUpBio,
              onValueChange = onSignUpBioChange,
              minLines = 2,
            )
            AppTextField(
              label = strings["label_username"],
              value = signUpUsername,
              onValueChange = onSignUpUsernameChange,
            )
            AppTextField(
              label = strings["password"],
              value = signUpPassword,
              onValueChange = onSignUpPasswordChange,
              isPassword = true,
            )
            GradientButton(text = strings["auth_register"]) {
              onSignUpClick()
            }
          }
        }
        GhostButton(text = strings["close"]) {
          onCloseClick()
        }
      }
    }
  }

  if (showForgotPasswordDialog) {
    Dialog(
      onDismissRequest = { if (!resetLoading) showForgotPasswordDialog = false },
      properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(0.92f)
          .padding(16.dp),
        contentAlignment = Alignment.Center,
      ) {
        AppCard(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(22.dp),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Box(
              modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(colors.accent.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Rounded.MailOutline,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(28.dp),
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = strings["auth_forgot_password_title"].ifBlank { "Atur Ulang Kata Sandi" },
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp,
              color = colors.text,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = strings["auth_forgot_password_desc"].ifBlank {
                "Masukkan email akun Anda. Kami akan mengirimkan tautan pemulihan kata sandi via Supabase."
              },
              fontSize = 12.sp,
              color = colors.muted,
              lineHeight = 16.sp,
            )

            Spacer(modifier = Modifier.height(14.dp))

            AppTextField(
              label = strings["label_email"],
              value = resetEmail,
              onValueChange = { resetEmail = it; resetMessage = null },
            )

            resetMessage?.let { msg ->
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = msg,
                fontSize = 12.sp,
                color = if (resetSuccess) colors.accent else colors.danger,
                lineHeight = 16.sp,
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Box(modifier = Modifier.weight(1f)) {
                GhostButton(
                  text = strings["close"].ifBlank { "Batal" },
                  fillMaxWidth = true,
                  onClick = { if (!resetLoading) showForgotPasswordDialog = false },
                )
              }

              Box(modifier = Modifier.weight(1f)) {
                GradientButton(
                  text = if (resetLoading) "Mengirim..." else strings["auth_send_reset_link"].ifBlank { "Kirim Tautan" },
                  enabled = !resetLoading && resetEmail.isNotBlank(),
                  onClick = {
                    scope.launch {
                      resetLoading = true
                      resetMessage = null
                      val res = SupabaseRepository.resetPasswordForEmail(resetEmail)
                      resetLoading = false
                      if (res.isSuccess) {
                        resetSuccess = true
                        resetMessage = strings["auth_reset_link_sent"].ifBlank { "Tautan reset telah dikirim ke email Anda!" }
                      } else {
                        resetSuccess = false
                        resetMessage = res.exceptionOrNull()?.message ?: "Gagal mengirim email reset. Pastikan email terdaftar."
                      }
                    }
                  },
                )
              }
            }
          }
        }
      }
    }
  }
}
