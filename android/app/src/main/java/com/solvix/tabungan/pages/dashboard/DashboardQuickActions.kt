package com.solvix.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardQuickActions(
  strings: AppStrings,
  onNavigateTo: (Page) -> Unit,
) {
  val colors = LocalAppColors.current
  val theme = LocalThemeName.current
  val isDark = isDarkTheme(theme)

  val incomeColor = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
  val expenseColor = if (isDark) Color(0xFFFF8A80) else Color(0xFFC62828)

  AppCard {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text(
        text = strings["dashboard_quick_actions"],
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = colors.text,
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        QuickActionButton(
          icon = themePageVisualIcon(theme, Page.Income),
          label = strings["dashboard_add_income"],
          modifier = Modifier.weight(1f),
          color = incomeColor,
          onClick = { onNavigateTo(Page.Income) },
        )
        QuickActionButton(
          icon = themePageVisualIcon(theme, Page.Expense),
          label = strings["dashboard_add_expense"],
          modifier = Modifier.weight(1f),
          color = expenseColor,
          onClick = { onNavigateTo(Page.Expense) },
        )
        QuickActionButton(
          icon = themePageVisualIcon(theme, Page.Dreams),
          label = strings["page_dreams"],
          modifier = Modifier.weight(1f),
          color = colors.accent,
          onClick = { onNavigateTo(Page.Dreams) },
        )
        QuickActionButton(
          icon = themePageVisualIcon(theme, Page.AIChat),
          label = strings["menu_ai_chat"],
          modifier = Modifier.weight(1f),
          color = colors.accent2,
          onClick = { onNavigateTo(Page.AIChat) },
        )
      }
    }
  }
}

@Composable
private fun QuickActionButton(
  icon: ThemedVisualIcon,
  label: String,
  modifier: Modifier = Modifier,
  color: Color,
  onClick: () -> Unit,
) {
  val colors = LocalAppColors.current

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(AppDimens.radiusSm))
      .background(color.copy(alpha = 0.12f))
      .clickable(onClick = onClick)
      .padding(vertical = 10.dp, horizontal = 4.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      ThemedBadgeIcon(
        icon = icon,
        containerSize = 32.dp,
        iconSize = 18.dp,
        tint = color,
        bgAlpha = 0.22f,
      )
      Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = colors.text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}
