package com.solvix.tabungan.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.solvix.tabungan.AppCard
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.AppTextField
import com.solvix.tabungan.GhostButton
import com.solvix.tabungan.GradientButton
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.LocalStrings

@Composable
fun AddCategoryDialog(
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit,
  placeholder: String,
  title: String? = null,
  strings: AppStrings = LocalStrings.current,
) {
  val colors = LocalAppColors.current
  var text by remember { mutableStateOf("") }

  Dialog(onDismissRequest = onDismiss) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = title ?: strings["category_add_title"].ifBlank { "Tambah Kategori Baru" },
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = colors.text,
        )
        AppTextField(
          label = strings["category_name"].ifBlank { "Nama Kategori" },
          value = text,
          onValueChange = { text = it },
          placeholder = placeholder,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          GhostButton(
            text = strings["confirm_cancel"],
            modifier = Modifier.weight(1f),
            onClick = onDismiss,
          )
          GradientButton(
            text = strings["confirm_ok"],
            modifier = Modifier.weight(1f),
            enabled = text.isNotBlank(),
            onClick = {
              val trimmed = text.trim()
              if (trimmed.isNotBlank()) {
                onConfirm(trimmed)
              }
            },
          )
        }
      }
    }
  }
}
