package com.solvix.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppCard
import com.solvix.tabungan.AppDropdown
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.AppTextField
import com.solvix.tabungan.DangerButton
import com.solvix.tabungan.DateField
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

/**
 * Halaman Profil Pengguna yang modular, konsisten dengan kartu (AppCard),
 * serta ramah pengguna untuk semua kalangan usia.
 */
@Composable
fun ProfilePage(
  user: UserProfile?,
  strings: AppStrings,
  onSave: (UserProfile) -> Unit,
  onLogout: () -> Unit,
) {
  val colors = LocalAppColors.current
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

  Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    SectionTitle(
      icon = themePageVisualIcon(LocalThemeName.current, Page.Profile),
      title = strings["section_profile_title"],
      subtitle = strings["section_profile_subtitle"],
    )

    // KARTU 1: Informasi Profil Pengguna
    AppCard {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        val initials = (name.ifBlank { username.ifBlank { "U" } }).take(1).uppercase()
        Box(
          modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(colors.accent, colors.accent2))),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = initials,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color.White,
          )
        }

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = name.ifBlank { strings["guest"] },
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = colors.text,
          )
          Text(
            text = email.ifBlank { "@${username.ifBlank { "pengguna" }}" },
            fontSize = 12.sp,
            color = colors.muted,
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
      HorizontalDivider(thickness = 0.8.dp, color = colors.cardBorder.copy(alpha = 0.5f))
      Spacer(modifier = Modifier.height(10.dp))

      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ProfileLine(label = strings["label_name"], value = name.ifBlank { strings["guest"] })
        ProfileLine(label = strings["label_email"], value = email.ifBlank { "-" })
        ProfileLine(label = strings["label_country"], value = country.ifBlank { "-" })
        ProfileLine(label = strings["label_birthdate"], value = birthdate.ifBlank { "-" })
        ProfileLine(label = strings["label_bio"], value = bio.ifBlank { "-" })
        ProfileLine(label = strings["label_username"], value = username.ifBlank { "-" })
      }
    }

    // KARTU 2: Formulir Perbarui Data Profil
    AppCard {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(colors.accent.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(20.dp),
          )
        }

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = strings["profile_update"],
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = colors.text,
          )
          Text(
            text = "Perbarui nama, tanggal lahir, dan informasi akun Anda",
            fontSize = 11.sp,
            color = colors.muted,
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AppTextField(strings["label_name"], value = name, onValueChange = { name = it })
        AppTextField(
          strings["label_email"],
          value = email,
          onValueChange = { email = it },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
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

    // KARTU 3: Sesi & Keluar Akun (Konsisten dengan AppCard)
    AppCard {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(colors.danger.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.Logout,
            contentDescription = null,
            tint = colors.danger,
            modifier = Modifier.size(20.dp),
          )
        }

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Sesi Akun",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = colors.text,
          )
          Text(
            text = "Keluar dari sesi akun aktif pada perangkat ini",
            fontSize = 11.sp,
            color = colors.muted,
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      DangerButton(
        text = strings["logout"],
        fillMaxWidth = true,
        onClick = onLogout,
      )
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
fun ProfileLine(label: String, value: String) {
  val colors = LocalAppColors.current
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(text = label, color = colors.muted, fontSize = 13.sp)
    Text(text = value, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.text)
  }
}
