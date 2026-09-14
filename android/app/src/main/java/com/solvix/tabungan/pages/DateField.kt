package com.solvix.tabungan

import androidx.compose.foundation.clickable
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppTextField
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.LocalStrings
import com.solvix.tabungan.parseDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
) {
  val colors = LocalAppColors.current
  val strings = LocalStrings.current
  var showPicker by remember { mutableStateOf(false) }
  val initialMillis = parseDate(value) ?: System.currentTimeMillis()
  val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
  val formatter = remember { SimpleDateFormat("dd-MM-yyyy", Locale.US) }

  AppTextField(
    label = label,
    value = value,
    onValueChange = onValueChange,
    placeholder = placeholder,
    textFontSize = 12.sp,
    placeholderFontSize = 11.sp,
    trailing = {
      Text(
        text = "📅",
        color = colors.muted,
        modifier = Modifier.clickable { showPicker = true },
      )
    },
  )

  if (showPicker) {
    DatePickerDialog(
      onDismissRequest = { showPicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            val selected = datePickerState.selectedDateMillis
            if (selected != null) {
              onValueChange(formatter.format(Date(selected)))
            }
            showPicker = false
          },
        ) {
          Text(text = strings["ok"], color = colors.text)
        }
      },
      dismissButton = {
        TextButton(onClick = { showPicker = false }) {
          Text(text = strings["confirm_cancel"], color = colors.muted)
        }
      },
      colors = DatePickerDefaults.colors(
        containerColor = colors.card,
        titleContentColor = colors.text,
        headlineContentColor = colors.text,
        weekdayContentColor = colors.muted,
        subheadContentColor = colors.muted,
        dayContentColor = colors.text,
        selectedDayContainerColor = colors.accent,
        selectedDayContentColor = Color.White,
        todayDateBorderColor = colors.accent2,
        todayContentColor = colors.text,
      ),
    ) {
      DatePicker(
        state = datePickerState,
        title = null,
        headline = null,
        showModeToggle = false,
      )
    }
  }
}
