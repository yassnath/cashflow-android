package com.solvix.tabungan.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
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
import com.solvix.tabungan.optionList

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

  ModalOverlay {
    ModalCard(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .heightIn(max = 520.dp),
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
}
