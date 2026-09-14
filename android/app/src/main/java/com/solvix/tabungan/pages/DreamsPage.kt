package com.solvix.tabungan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppCard
import com.solvix.tabungan.AppDimens
import com.solvix.tabungan.AppDropdown
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.AppTextField
import com.solvix.tabungan.DreamEntry
import com.solvix.tabungan.FadeInPage
import com.solvix.tabungan.GhostButton
import com.solvix.tabungan.GradientButton
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.LocalStrings
import com.solvix.tabungan.LocalThemeName
import com.solvix.tabungan.Page
import com.solvix.tabungan.SectionTitle
import com.solvix.tabungan.UUIDString
import com.solvix.tabungan.formatRupiah
import com.solvix.tabungan.isDarkTheme
import com.solvix.tabungan.parseAmount
import com.solvix.tabungan.themePageIcon
import kotlin.math.roundToInt

@Composable
fun DreamsPage(
  entries: List<DreamEntry>,
  incomeTotal: Int,
  expenseTotal: Int,
  balanceTotal: Int,
  onInvalid: () -> Unit,
  onSave: (DreamEntry) -> Unit,
  onUpdate: (DreamEntry) -> Unit,
  onDelete: (DreamEntry) -> Unit,
  goalReachSourceType: String?,
  onGoalReachDismiss: () -> Unit,
  strings: AppStrings,
) {
  var title by rememberSaveable { mutableStateOf("") }
  var target by rememberSaveable { mutableStateOf("") }
  var deadline by rememberSaveable { mutableStateOf("") }
  var note by rememberSaveable { mutableStateOf("") }
  var sourceType by rememberSaveable { mutableStateOf("") }
  var editingId by rememberSaveable { mutableStateOf<String?>(null) }
  var formFadeSeed by rememberSaveable { mutableIntStateOf(0) }

  fun beginEdit(entry: DreamEntry) {
    editingId = entry.id
    title = entry.title
    target = entry.target.toString()
    deadline = entry.deadline
    note = entry.note
    sourceType = entry.sourceType.ifBlank { "" }
    formFadeSeed += 1
  }

  Column {
    SectionTitle(icon = themePageVisualIcon(LocalThemeName.current, Page.Dreams), title = strings["section_dreams_title"], subtitle = strings["section_dreams_subtitle"])
    if (!goalReachSourceType.isNullOrBlank()) {
      GoalReachedPopup(
        sourceType = goalReachSourceType,
        onDismiss = onGoalReachDismiss,
        strings = strings,
      )
      Spacer(modifier = Modifier.height(12.dp))
    }
    FadeInPage(key = "goals_form_${editingId ?: "new"}_$formFadeSeed") {
      AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          AppTextField(strings["label_target_name"], value = title, onValueChange = { title = it }, placeholder = strings["placeholder_target"])
          AppTextField(strings["label_target_amount"], value = target, onValueChange = { target = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
          DateField(strings["label_deadline"], value = deadline, onValueChange = { deadline = it }, placeholder = strings["placeholder_date"])
          val sourceLabels = mapOf(
            "income" to strings["goal_source_income"],
            "expense" to strings["goal_source_expense"],
            "balance" to strings["goal_source_balance"],
          )
          AppDropdown(
            label = strings["label_goal_source"],
            placeholder = strings["placeholder_goal_source"],
            options = listOf(
              strings["goal_source_income"],
              strings["goal_source_expense"],
              strings["goal_source_balance"],
            ),
            selected = sourceLabels[sourceType].orEmpty(),
            onSelected = { label ->
              sourceType = sourceLabels.entries.firstOrNull { it.value == label }?.key ?: "income"
            },
          )
          AppTextField(strings["label_note"], value = note, onValueChange = { note = it }, placeholder = strings["placeholder_strategy"], minLines = 2)
          val trimmedTitle = title.trim()
          val targetAmount = parseAmount(target)
          val isFormValid = trimmedTitle.isNotBlank() && targetAmount > 0 && deadline.isNotBlank() && sourceType.isNotBlank()
          GradientButton(
            text = if (editingId == null) strings["save_dream"] else strings["update_dream"],
            enabled = isFormValid,
            onClick = {
              if (!isFormValid) {
                onInvalid()
                return@GradientButton
              }
              val entry = DreamEntry(
                id = editingId ?: UUIDString(),
                title = trimmedTitle,
                target = targetAmount,
                current = 0,
                deadline = deadline,
                note = note,
                sourceType = sourceType,
              )
              if (editingId == null) onSave(entry) else onUpdate(entry)
              title = ""
              target = ""
              deadline = ""
              note = ""
              sourceType = ""
              editingId = null
            },
          )
        }
      }
    }
    val visibleEntries = entries.filter { it.target > 0 }
    if (visibleEntries.isNotEmpty()) {
      Spacer(modifier = Modifier.height(12.dp))
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        visibleEntries.forEach { entry ->
          val baseProgress = when (entry.sourceType) {
            "balance" -> balanceTotal
            "expense" -> expenseTotal
            else -> incomeTotal
          }
          val progressValue = baseProgress.coerceAtLeast(0)
          GoalProgressCard(
            entry = entry,
            progressAmount = progressValue.coerceAtMost(entry.target),
            onEdit = { beginEdit(entry) },
            onDelete = { onDelete(entry) },
          )
        }
      }
    }
  }
}

@Composable
fun GoalProgressCard(
  entry: DreamEntry,
  progressAmount: Int,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  val colors = LocalAppColors.current
  val strings = LocalStrings.current
  val clampedProgress = progressAmount.coerceIn(0, entry.target.coerceAtLeast(0))
  val hasProgress = clampedProgress > 0
  val track = colors.muted.copy(alpha = if (isDarkTheme(LocalThemeName.current)) 0.25f else 0.2f)
  val valueColor = when {
    entry.sourceType == "expense" -> colors.danger
    hasProgress -> colors.accent2
    else -> colors.muted
  }
  val percentValue = if (entry.target > 0) {
    ((clampedProgress.toFloat() / entry.target) * 100f).coerceIn(0f, 100f)
  } else {
    0f
  }
  val percentText = "${percentValue.roundToInt()}%"
  val remainder = (entry.target - clampedProgress).coerceAtLeast(0)
  val progressBrush = if (entry.sourceType == "expense") {
    Brush.sweepGradient(listOf(colors.danger, colors.danger.copy(alpha = 0.7f), colors.danger))
  } else {
    Brush.sweepGradient(listOf(colors.accent, colors.accent2, colors.accent))
  }
  val progressColor = if (entry.sourceType == "expense") colors.danger else colors.accent
  val slices = listOf(
    DonutSlice(label = entry.title, value = clampedProgress, color = progressColor, brush = progressBrush),
    DonutSlice(label = "remaining", value = remainder, color = track),
  )
  val noteText = if (entry.note.isBlank()) "-" else entry.note

  AppCard(shape = androidx.compose.foundation.shape.RoundedCornerShape(AppDimens.radiusMd)) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        DonutChart(
          slices = slices,
          modifier = Modifier.size(88.dp),
          strokeWidth = 14.dp,
          centerLabel = percentText,
          centerSubLabel = null,
          trackColor = Color.Transparent,
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(text = entry.title, fontWeight = FontWeight.Bold, color = colors.text)
          Text(
            text = "${formatRupiah(clampedProgress)} / ${formatRupiah(entry.target)}",
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            fontSize = 12.sp,
          )
          Text(text = percentText, color = colors.muted, fontSize = 11.sp)
          if (entry.deadline.isNotBlank()) {
            Text(text = "${strings["label_deadline"]}: ${entry.deadline}", color = colors.muted, fontSize = 11.sp)
          }
          Text(
            text = "${strings["note_label"]}: $noteText",
            color = colors.muted,
            fontSize = 11.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        GhostButton(text = strings["edit"], fillMaxWidth = false, modifier = Modifier.weight(1f), onClick = onEdit)
        GhostButton(text = strings["delete"], fillMaxWidth = false, modifier = Modifier.weight(1f), onClick = onDelete)
      }
    }
  }
}

@Composable
fun DreamsList(
  entries: List<DreamEntry>,
  onEdit: (DreamEntry) -> Unit,
  onDelete: (DreamEntry) -> Unit,
) {
  val strings = LocalStrings.current
  if (entries.isEmpty()) return
  Spacer(modifier = Modifier.height(12.dp))
  Text(text = strings["list_dreams"], fontWeight = FontWeight.Bold, fontSize = 14.sp)
  Spacer(modifier = Modifier.height(8.dp))
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    entries.forEach { entry ->
      AppCard(shape = androidx.compose.foundation.shape.RoundedCornerShape(AppDimens.radiusMd)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
          Text(text = entry.title, fontWeight = FontWeight.Bold)
          Text(text = formatRupiah(entry.target), fontWeight = FontWeight.Bold)
        }
        Text(text = "${strings["label_target_current"]}: ${formatRupiah(entry.current)}", color = LocalAppColors.current.muted, fontSize = 12.sp)
        Text(text = "${strings["label_deadline"]}: ${entry.deadline}", color = LocalAppColors.current.muted, fontSize = 12.sp)
        if (entry.note.isNotBlank()) {
          Text(text = entry.note, color = LocalAppColors.current.muted, fontSize = 12.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
          GhostButton(text = strings["edit"], fillMaxWidth = false, modifier = Modifier.weight(1f), onClick = { onEdit(entry) })
          GhostButton(text = strings["delete"], fillMaxWidth = false, modifier = Modifier.weight(1f), onClick = { onDelete(entry) })
        }
      }
    }
  }
}

@Composable
fun GoalReachedPopup(
  sourceType: String,
  onDismiss: () -> Unit,
  strings: AppStrings,
) {
  val colors = LocalAppColors.current
  val isExpenseSource = sourceType == "expense"
  val message = if (isExpenseSource) {
    strings["goal_reached_popup_expense"]
  } else {
    strings["goal_reached_popup_income_balance"]
  }
  val messageColor = if (isExpenseSource) colors.danger else Color(0xFF16A34A)

  AppCard {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
      ) {
        Text(
          text = "x",
          color = colors.muted,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.clickable { onDismiss() },
        )
      }
      Text(
        text = message,
        color = messageColor,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
      )
    }
  }
}
