package com.solvix.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

  // Palet Warna Adaptif Harmonik per Tema
  val incomeColor = when (theme) {
    ThemeName.CartoonPlant -> Color(0xFF22C55E)
    ThemeName.CartoonSea -> Color(0xFF14B8A6)
    ThemeName.CartoonPinky -> Color(0xFF10B981)
    else -> if (isDark) Color(0xFF4ADE80) else Color(0xFF16A34A)
  }

  val expenseColor = when (theme) {
    ThemeName.CartoonPinky -> Color(0xFFFB7185)
    ThemeName.CartoonMonster -> Color(0xFFFF5252)
    else -> if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
  }

  val transferColor = when (theme) {
    ThemeName.CartoonFood -> Color(0xFFEA580C)
    ThemeName.CartoonSpace -> Color(0xFF38BDF8)
    ThemeName.CartoonPinky -> Color(0xFF818CF8)
    else -> Color(0xFF0288D1)
  }

  val dreamsColor = when (theme) {
    ThemeName.CartoonFood -> Color(0xFFF59E0B)
    ThemeName.CartoonPinky -> Color(0xFFF472B6)
    ThemeName.CartoonSpace -> Color(0xFFA855F7)
    ThemeName.CartoonMonster -> Color(0xFF84CC16)
    else -> Color(0xFFF59E0B)
  }

  AppCard {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = strings["dashboard_quick_actions"].ifBlank { "Aksi Cepat" },
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = colors.text,
        )
        Text(
          text = "Akses instan",
          fontSize = 11.sp,
          color = colors.muted,
          fontWeight = FontWeight.Medium,
        )
      }

      // Baris 1: Transaksi Keuangan Utama
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        QuickActionButton(
          icon = themePageVisualIcon(theme, Page.Income),
          label = strings["dashboard_add_income"].ifBlank { "Pemasukan" },
          modifier = Modifier.weight(1f),
          color = incomeColor,
          onClick = { onNavigateTo(Page.Income) },
        )
        QuickActionButton(
          icon = themePageVisualIcon(theme, Page.Expense),
          label = strings["dashboard_add_expense"].ifBlank { "Pengeluaran" },
          modifier = Modifier.weight(1f),
          color = expenseColor,
          onClick = { onNavigateTo(Page.Expense) },
        )
        QuickActionButton(
          icon = themePageVisualIcon(theme, Page.Transfer),
          label = strings["page_transfer"].ifBlank { "Transfer" },
          modifier = Modifier.weight(1f),
          color = transferColor,
          onClick = { onNavigateTo(Page.Transfer) },
        )
      }

      // Baris 2: Navigasi Fitur Cepat & Produktivitas
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        QuickActionButton(
          icon = themePageVisualIcon(theme, Page.History),
          label = strings["page_history"].ifBlank { "Riwayat" },
          modifier = Modifier.weight(1f),
          color = colors.accent,
          onClick = { onNavigateTo(Page.History) },
        )
        QuickActionButton(
          icon = themePageVisualIcon(theme, Page.Dreams),
          label = strings["page_dreams"].ifBlank { "Target" },
          modifier = Modifier.weight(1f),
          color = dreamsColor,
          onClick = { onNavigateTo(Page.Dreams) },
        )
        QuickActionButton(
          icon = themePageVisualIcon(theme, Page.AIChat),
          label = strings["page_ai_chat"].ifBlank { "AI Chat" },
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
  val theme = LocalThemeName.current
  val isDark = isDarkTheme(theme)

  val tileShape = RoundedCornerShape(16.dp)
  val badgeShape = RoundedCornerShape(12.dp)

  Box(
    modifier = modifier
      .heightIn(min = 86.dp)
      .clip(tileShape)
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            color.copy(alpha = if (isDark) 0.18f else 0.12f),
            color.copy(alpha = if (isDark) 0.07f else 0.03f),
          ),
        ),
      )
      .border(
        width = 1.dp,
        color = color.copy(alpha = if (isDark) 0.32f else 0.20f),
        shape = tileShape,
      )
      .clickable(onClick = onClick)
      .padding(vertical = 10.dp, horizontal = 4.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(badgeShape)
          .background(
            brush = Brush.linearGradient(
              colors = listOf(
                color.copy(alpha = if (isDark) 0.28f else 0.20f),
                color.copy(alpha = if (isDark) 0.14f else 0.08f),
              ),
            ),
          )
          .border(
            width = 0.8.dp,
            color = color.copy(alpha = if (isDark) 0.45f else 0.28f),
            shape = badgeShape,
          ),
        contentAlignment = Alignment.Center,
      ) {
        ThemedBadgeIcon(
          icon = icon,
          containerSize = 34.dp,
          iconSize = 18.dp,
          tint = color,
          bgAlpha = 0f,
        )
      }

      Text(
        text = label,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        color = colors.text,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        lineHeight = 14.sp,
      )
    }
  }
}
