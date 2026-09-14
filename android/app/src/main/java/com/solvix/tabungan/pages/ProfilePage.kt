package com.solvix.tabungan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppCard
import com.solvix.tabungan.AppDropdown
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.AppTextField
import com.solvix.tabungan.GhostButton
import com.solvix.tabungan.GradientButton
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.LocalLanguage
import com.solvix.tabungan.LocalThemeName
import com.solvix.tabungan.OptionList
import com.solvix.tabungan.Page
import com.solvix.tabungan.SectionTitle
import com.solvix.tabungan.UserProfile
import com.solvix.tabungan.optionList
import com.solvix.tabungan.themePageIcon

@Composable
fun ProfilePage(
  user: UserProfile?,
  strings: AppStrings,
  onSave: (UserProfile) -> Unit,
  onLogout: () -> Unit,
) {
  val language = LocalLanguage.current
  var name by rememberSaveable { mutableStateOf(user?.name.orEmpty()) }
  var email by rememberSaveable { mutableStateOf(user?.email.orEmpty()) }
  var country by rememberSaveable { mutableStateOf(user?.country.orEmpty()) }
  var birthdate by rememberSaveable { mutableStateOf(user?.birthdate.orEmpty()) }
  var bio by rememberSaveable { mutableStateOf(user?.bio.orEmpty()) }
  var username by rememberSaveable { mutableStateOf(user?.username.orEmpty()) }

  LaunchedEffect(user) {
    name = user?.name.orEmpty()
    email = user?.email.orEmpty()
    country = user?.country.orEmpty()
    birthdate = user?.birthdate.orEmpty()
    bio = user?.bio.orEmpty()
    username = user?.username.orEmpty()
  }

  Column {
    SectionTitle(icon = themePageVisualIcon(LocalThemeName.current, Page.Profile), title = strings["section_profile_title"], subtitle = strings["section_profile_subtitle"])
    AppCard {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ProfileLine(label = strings["label_name"], value = name.ifBlank { strings["guest"] })
        ProfileLine(label = strings["label_email"], value = email.ifBlank { "-" })
        ProfileLine(label = strings["label_country"], value = country.ifBlank { "-" })
        ProfileLine(label = strings["label_birthdate"], value = birthdate.ifBlank { "-" })
        ProfileLine(label = strings["label_bio"], value = bio.ifBlank { "-" })
        ProfileLine(label = strings["label_username"], value = username.ifBlank { "-" })
      }
    }
    Spacer(modifier = Modifier.height(12.dp))
    AppCard {
      Text(text = strings["profile_update"], fontWeight = FontWeight.Bold, fontSize = 16.sp)
      Spacer(modifier = Modifier.height(12.dp))
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AppTextField(strings["label_name"], value = name, onValueChange = { name = it })
        AppTextField(strings["label_email"], value = email, onValueChange = { email = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
        AppDropdown(
          label = strings["label_country"],
          placeholder = strings["placeholder_country"],
          options = optionList(language, OptionList.Countries),
          selected = country,
          onSelected = { country = it },
        )
        DateField(
          label = strings["label_birthdate"],
          value = birthdate,
          onValueChange = { birthdate = it },
          placeholder = strings["placeholder_date"],
        )
        AppTextField(strings["label_bio"], value = bio, onValueChange = { bio = it }, minLines = 2)
        AppTextField(strings["label_username"], value = username, onValueChange = { username = it })
        GradientButton(text = strings["save_profile"]) {
          onSave(
            UserProfile(
              id = user?.id.orEmpty(),
              authId = user?.authId.orEmpty(),
              name = name,
              email = email,
              country = country,
              birthdate = birthdate,
              bio = bio,
              createdAt = user?.createdAt.orEmpty(),
              username = username,
              password = "",
            ),
          )
        }
      }
    }
    Spacer(modifier = Modifier.height(12.dp))
    GhostButton(text = strings["logout"], onClick = onLogout)
  }
}

@Composable
fun ProfileLine(label: String, value: String) {
  val colors = LocalAppColors.current
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(text = label, color = colors.muted, fontSize = 13.sp)
    Text(text = value, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.text)
  }
}
