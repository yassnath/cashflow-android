package com.solvix.tabungan

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlin.math.roundToInt

@Composable
fun DashboardCategoryBudgets(
  thisMonthExpenses: List<MoneyEntry>,
  allExpenseEntries: List<MoneyEntry>,
  strings: AppStrings,
) {
  val context = LocalContext.current
  val colors = LocalAppColors.current
  val theme = LocalThemeName.current
  val isDark = isDarkTheme(theme)

  val safeColor = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
  val warningColor = if (isDark) Color(0xFFFFB74D) else Color(0xFFF57C00)
  val dangerColor = if (isDark) Color(0xFFFF8A80) else Color(0xFFC62828)

  var budgets by remember { mutableStateOf(CategoryBudgetManager.loadBudgets(context)) }
  var showManageDialog by remember { mutableStateOf(false) }

  val availableExpenseCategories = remember(allExpenseEntries) {
    val presets = CategoryManager.getExpenseCategories(context)
    val fromEntries = allExpenseEntries.map { it.category }.distinct().filter { it.isNotBlank() }
    (presets + fromEntries).distinct()
  }

  AppCard {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SectionTitle(
          icon = themeSectionVisualIcon(theme, "target", "🎯"),
          title = strings["dashboard_category_budgets"],
          modifier = Modifier.weight(1f, fill = false),
          compact = true,
        )
        Spacer(modifier = Modifier.width(6.dp))
        ChipButton(
          text = strings["dashboard_manage_budget"],
          onClick = { showManageDialog = true },
        )
      }

      if (budgets.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.radiusSm))
            .background(colors.bg2.copy(alpha = 0.5f))
            .padding(14.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = strings["dashboard_no_budgets"],
            color = colors.muted,
            fontSize = 12.sp,
            lineHeight = 18.sp,
          )
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          budgets.forEach { (category, limit) ->
            val spent = thisMonthExpenses
              .filter { it.category.equals(category, ignoreCase = true) }
              .sumOf { it.amount }
            val rawRatio = if (limit > 0) spent.toFloat() / limit else 1f
            val percent = (rawRatio * 100).roundToInt()

            val (statusText, statusColor) = when {
              rawRatio >= 1.0f -> Pair(strings["dashboard_budget_exceeded"], dangerColor)
              rawRatio >= 0.8f -> Pair(strings["dashboard_budget_warning"], warningColor)
              else -> Pair(strings["dashboard_budget_safe"], safeColor)
            }

            val animatedRatio by animateFloatAsState(
              targetValue = rawRatio.coerceIn(0.02f, 1f),
              animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
              label = "budget_ratio_$category",
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = category,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 13.sp,
                  color = colors.text,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f, fill = false),
                )
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(statusColor.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                  Text(
                    text = "$statusText ($percent%)",
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                  )
                }
              }

              // Progress Bar
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(8.dp)
                  .clip(RoundedCornerShape(999.dp))
                  .background(colors.bg2),
              ) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth(animatedRatio)
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                      Brush.horizontalGradient(
                        listOf(statusColor.copy(alpha = 0.7f), statusColor)
                      )
                    ),
                )
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                  text = "Terpakai: ${formatRupiah(spent)}",
                  fontSize = 11.sp,
                  color = colors.muted,
                )
                Text(
                  text = "Batas: ${formatRupiah(limit)}",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = colors.text,
                )
              }
            }
          }
        }
      }
    }
  }

  // Dialog Atur Anggaran
  if (showManageDialog) {
    ManageBudgetDialog(
      availableCategories = availableExpenseCategories,
      existingBudgets = budgets,
      strings = strings,
      onDismiss = { showManageDialog = false },
      onSave = { category, limit ->
        CategoryBudgetManager.saveBudget(context, category, limit)
        budgets = CategoryBudgetManager.loadBudgets(context)
      },
      onDelete = { category ->
        CategoryBudgetManager.deleteBudget(context, category)
        budgets = CategoryBudgetManager.loadBudgets(context)
      },
    )
  }
}

@Composable
private fun ManageBudgetDialog(
  availableCategories: List<String>,
  existingBudgets: Map<String, Int>,
  strings: AppStrings,
  onDismiss: () -> Unit,
  onSave: (category: String, limit: Int) -> Unit,
  onDelete: (category: String) -> Unit,
) {
  val colors = LocalAppColors.current
  var selectedCategory by remember { mutableStateOf(availableCategories.firstOrNull() ?: "") }
  var limitText by remember {
    mutableStateOf(existingBudgets[selectedCategory]?.toString() ?: "")
  }

  Dialog(onDismissRequest = onDismiss) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
      Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
          text = strings["dashboard_set_budget"],
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = colors.text,
        )

        AppDropdown(
          label = "Kategori Pengeluaran",
          placeholder = "Pilih Kategori",
          options = availableCategories,
          selected = selectedCategory,
          onSelected = { cat ->
            selectedCategory = cat
            limitText = existingBudgets[cat]?.toString() ?: ""
          },
        )

        AppTextField(
          label = strings["dashboard_budget_limit_hint"],
          value = limitText,
          onValueChange = { limitText = it.filter { ch -> ch.isDigit() } },
          placeholder = "Contoh: 1500000",
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          if (existingBudgets.containsKey(selectedCategory)) {
            DangerButton(
              text = strings["delete"],
              modifier = Modifier.weight(1f),
              onClick = {
                onDelete(selectedCategory)
                onDismiss()
              },
            )
          }

          GradientButton(
            text = strings["save"],
            modifier = Modifier.weight(1f),
            enabled = selectedCategory.isNotBlank() && limitText.isNotBlank(),
            onClick = {
              val limit = limitText.toIntOrNull() ?: 0
              if (limit > 0) {
                onSave(selectedCategory, limit)
              }
              onDismiss()
            },
          )
        }
      }
    }
  }
}
