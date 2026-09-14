package com.solvix.tabungan

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

enum class Page(val label: String, val icon: String) {
  Dashboard("Dashboard", "🏠"),
  Income("Pemasukkan", "💸"),
  Expense("Pengeluaran", "🧾"),
  Dreams("Target", "🌟"),
  History("History", "📒"),
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
fun TopBar(
  onProfileClick: () -> Unit,
  showMenu: Boolean,
  onDismissMenu: () -> Unit,
  onNavigate: (Page) -> Unit,
  onLogout: () -> Unit,
  displayName: String,
  displayUsername: String,
  strings: AppStrings,
  theme: ThemeName,
  currentPage: Page,
  isAdmin: Boolean,
) {
  val colors = LocalAppColors.current
  var menuAnchorBounds by remember { mutableStateOf(IntRect.Zero) }
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 20.dp, bottom = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(52.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(colors.card)
          .shadow(10.dp, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
      ) {
        Text(text = themeAppIcon(theme), fontSize = 26.sp)
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(text = strings["app_name"], fontWeight = FontWeight.Bold, fontSize = 20.sp, color = colors.text)
        Text(text = strings["app_tagline"], color = colors.muted, fontSize = 13.sp)
      }
    }

    Box {
      ChipButton(
        text = if (displayUsername.isBlank()) strings["guest"] else displayUsername,
        modifier = Modifier.onGloballyPositioned { coordinates ->
          val rect = coordinates.boundsInWindow()
          menuAnchorBounds = IntRect(
            rect.left.roundToInt(),
            rect.top.roundToInt(),
            rect.right.roundToInt(),
            rect.bottom.roundToInt(),
          )
        },
        onClick = onProfileClick,
      )
      DropDownMenuCard(
        expanded = showMenu,
        onDismiss = onDismissMenu,
        modifier = Modifier.width(220.dp),
        anchorBounds = menuAnchorBounds,
        alignRight = true,
        xOffsetDp = (-2).dp,
        yOffsetDp = 10.dp,
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(text = displayName, fontWeight = FontWeight.Bold, color = colors.text)
              Text(text = "@$displayUsername", color = colors.muted, fontSize = 12.sp)
            }
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(colors.bg2)
                .padding(4.dp)
                .clickable { onDismissMenu() },
              contentAlignment = Alignment.Center,
            ) {
              Text(text = "x", fontWeight = FontWeight.Bold, color = colors.text)
            }
          }
          if (isAdmin) {
            MenuItem(text = strings["admin_logout"], emoji = "🚪", color = colors.danger) { onLogout() }
          } else {
            MenuItem(
              text = strings["menu_history"],
              icon = themePageVisualIcon(theme, Page.History),
              active = currentPage == Page.History,
            ) { onNavigate(Page.History) }
            MenuItem(
              text = strings["menu_theme"],
              icon = themePageVisualIcon(theme, Page.Themes),
              active = currentPage == Page.Themes,
            ) { onNavigate(Page.Themes) }
            MenuItem(
              text = strings["menu_insights"],
              icon = themePageVisualIcon(theme, Page.Insights),
              active = currentPage == Page.Insights,
            ) { onNavigate(Page.Insights) }
            MenuItem(
              text = strings["menu_loans"],
              icon = themePageVisualIcon(theme, Page.Loans),
              active = currentPage == Page.Loans,
            ) { onNavigate(Page.Loans) }
            MenuItem(
              text = strings["menu_ai_chat"],
              icon = themePageVisualIcon(theme, Page.AIChat),
              active = currentPage == Page.AIChat,
            ) { onNavigate(Page.AIChat) }
            MenuItem(
              text = strings["menu_calculator"],
              icon = themePageVisualIcon(theme, Page.Calculator),
              active = currentPage == Page.Calculator,
            ) { onNavigate(Page.Calculator) }
            MenuItem(
              text = strings["menu_report"],
              icon = themePageVisualIcon(theme, Page.Report),
              active = currentPage == Page.Report,
            ) { onNavigate(Page.Report) }
            MenuItem(
              text = strings["menu_profile"],
              icon = themePageVisualIcon(theme, Page.Profile),
              active = currentPage == Page.Profile,
            ) { onNavigate(Page.Profile) }
            MenuItem(
              text = strings["menu_settings"],
              icon = themePageVisualIcon(theme, Page.Settings),
              active = currentPage == Page.Settings,
            ) { onNavigate(Page.Settings) }
            MenuItem(text = strings["menu_logout"], emoji = "🚪", color = colors.danger) { onLogout() }
          }
        }
      }
    }
  }
}

@Composable
fun BottomNav(
  current: Page,
  onSelect: (Page) -> Unit,
  strings: AppStrings,
  theme: ThemeName,
  modifier: Modifier = Modifier,
) {
  val colors = LocalAppColors.current
  val items = listOf(Page.Dashboard, Page.Income, Page.Expense, Page.Dreams)
  val navShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
  Row(
    modifier = modifier
      .fillMaxWidth()
      .heightIn(min = 94.dp)
      .navigationBarsPadding()
      .clip(navShape)
      .background(colors.card)
      .border(2.dp, colors.accent, navShape)
      .padding(horizontal = 10.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    items.forEach { item ->
      val active = current == item
      val indicatorProgress by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = tween(durationMillis = 260),
        label = "bottom-nav-indicator-${item.name}",
      )
      Column(
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 3.dp, vertical = 4.dp)
          .offset(y = 5.dp)
          .clickable { onSelect(item) },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        ThemedIconView(
          icon = themePageVisualIcon(theme, item),
          tint = if (active) colors.accent else colors.muted,
          size = 26.dp,
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = pageLabel(item, strings),
          fontSize = 13.sp,
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

@Composable
fun AmbientBackground() {
  val colors = LocalAppColors.current
  val transition = rememberInfiniteTransition(label = "ambient")
  val float1 by transition.animateFloat(
    initialValue = 0f,
    targetValue = 20f,
    animationSpec = infiniteRepeatable(tween(8000), RepeatMode.Reverse),
    label = "float1",
  )
  val float2 by transition.animateFloat(
    initialValue = 0f,
    targetValue = -20f,
    animationSpec = infiniteRepeatable(tween(9000), RepeatMode.Reverse),
    label = "float2",
  )
  val sparkle by transition.animateFloat(
    initialValue = 0.3f,
    targetValue = 0.8f,
    animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse),
    label = "sparkle",
  )

  Box(modifier = Modifier.fillMaxSize()) {
    Blob(
      modifier = Modifier
        .size(280.dp)
        .offset {
          IntOffset(
            x = (-60).dp.roundToPx(),
            y = (-80 + float1).dp.roundToPx(),
          )
        },
      brush = Brush.radialGradient(
        listOf(colors.accent, Color.Transparent),
      ),
    )
    Blob(
      modifier = Modifier
        .size(280.dp)
        .align(Alignment.BottomEnd)
        .offset {
          IntOffset(
            x = 80.dp.roundToPx(),
            y = (120 + float2).dp.roundToPx(),
          )
        },
      brush = Brush.radialGradient(
        listOf(colors.accent2, Color.Transparent),
      ),
    )
    Blob(
      modifier = Modifier
        .size(280.dp)
        .align(Alignment.CenterEnd)
        .offset {
          IntOffset(
            x = 120.dp.roundToPx(),
            y = float1.dp.roundToPx(),
          )
        },
      brush = Brush.radialGradient(
        listOf(Color(0xFF8AA7FF), Color.Transparent),
      ),
    )
    Text(
      text = "✦",
      modifier = Modifier
        .offset(x = 48.dp, y = 140.dp)
        .alpha(sparkle),
      color = Color.White.copy(alpha = 0.7f),
      fontSize = 18.sp,
    )
    Text(
      text = "✦",
      modifier = Modifier
        .offset(x = 260.dp, y = 360.dp)
        .alpha(sparkle),
      color = Color.White.copy(alpha = 0.7f),
      fontSize = 18.sp,
    )
    Text(
      text = "✦",
      modifier = Modifier
        .offset(x = 160.dp, y = 640.dp)
        .alpha(sparkle),
      color = Color.White.copy(alpha = 0.7f),
      fontSize = 18.sp,
    )
  }
}

@Composable
private fun Blob(modifier: Modifier, brush: Brush) {
  Box(
    modifier = modifier
      .clip(CircleShape)
      .blur(12.dp)
      .background(brush)
      .alpha(0.3f),
  )
}

fun pageLabel(page: Page, strings: AppStrings): String {
  return when (page) {
    Page.Dashboard -> strings["page_dashboard"]
    Page.Income -> strings["page_income"]
    Page.Expense -> strings["page_expense"]
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
