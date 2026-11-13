package com.solvix.tabungan

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class Page(val label: String, val icon: String) {
  Dashboard("Dashboard", "🏠"),
  Income("Pemasukan", "💸"),
  Expense("Pengeluaran", "🧾"),
  Transfer("Transfer", "🔁"),
  Dreams("Target", "🌟"),
  History("Riwayat", "📒"),
  Insights("Insights", "🧠"),
  Loans("Hutang", "💳"),
  AIChat("AI Chat", "🤖"),
  Calculator("Kalkulator", "🧮"),
  Report("Laporan", "📈"),
  Profile("Profile", "👤"),
  Settings("Pengaturan", "⚙️"),
  Themes("Tema", "🎨"),
}

fun Page.showHero(): Boolean = this == Page.Income || this == Page.Expense

@Composable
fun BottomNav(
  current: Page,
  onSelect: (Page) -> Unit,
  onMoreClick: () -> Unit = {},
  strings: AppStrings,
  theme: ThemeName,
  modifier: Modifier = Modifier,
) {
  val colors = LocalAppColors.current
  val items = listOf(
    Page.Dashboard,
    Page.Income,
    Page.Expense,
    Page.History,
  )
  val navShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
  Row(
    modifier = modifier
      .fillMaxWidth()
      .heightIn(min = 94.dp)
      .navigationBarsPadding()
      .clip(navShape)
      .background(colors.card)
      .border(1.dp, colors.accent.copy(alpha = 0.35f), navShape)
      .padding(horizontal = 10.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    items.forEach { page ->
      val active = current == page
      val indicatorProgress by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = tween(durationMillis = 260),
        label = "bottom-nav-indicator-${page.name}",
      )
      Column(
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 4.dp, vertical = 4.dp)
          .offset(y = 5.dp)
          .clickable { onSelect(page) },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        ThemedIconView(
          icon = themePageVisualIcon(theme, page),
          tint = if (active) colors.accent else colors.muted,
          size = 26.dp,
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = pageLabel(page, strings),
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = if (active) colors.text else colors.muted,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Box(
          modifier = Modifier
            .padding(top = 5.dp)
            .width(32.dp)
            .height(3.dp)
            .graphicsLayer {
              scaleX = indicatorProgress
              alpha = indicatorProgress
              transformOrigin = TransformOrigin(0f, 0.5f)
            }
            .clip(RoundedCornerShape(999.dp))
            .background(Brush.linearGradient(listOf(colors.accent, colors.accent2))),
        )
      }
    }
  }
}

fun pageLabel(page: Page, strings: AppStrings): String {
  return when (page) {
    Page.Dashboard -> strings["page_dashboard"]
    Page.Income -> strings["page_income"]
    Page.Expense -> strings["page_expense"]
    Page.Transfer -> strings["page_transfer"].ifBlank { "Transfer" }
    Page.Dreams -> strings["page_dreams"]
    Page.History -> strings["page_history"]
    Page.Insights -> strings["page_insights"]
    Page.Loans -> strings["page_loans"]
    Page.AIChat -> strings["page_ai_chat"]
    Page.Calculator -> strings["page_calculator"]
    Page.Report -> strings["page_report"]
    Page.Profile -> strings["page_profile"]
    Page.Settings -> strings["page_settings"]
    Page.Themes -> strings["page_themes"]
  }
}

fun themeLabel(theme: ThemeName, strings: AppStrings): String {
  return when (theme) {
    ThemeName.StandardLight -> strings["theme_standard_light"]
    ThemeName.StandardDark -> strings["theme_standard_dark"]
    ThemeName.CartoonFood -> strings["theme_food"]
    ThemeName.CartoonSpace -> strings["theme_space"]
    ThemeName.CartoonMonster -> strings["theme_monster"]
    ThemeName.CartoonHero -> strings["theme_hero"]
    ThemeName.CartoonSea -> strings["theme_sea"]
    ThemeName.CartoonPlant -> strings["theme_plant"]
    ThemeName.CartoonPinky -> strings["theme_pinky"]
    ThemeName.CartoonColorful -> strings["theme_colorful"]
  }
}
