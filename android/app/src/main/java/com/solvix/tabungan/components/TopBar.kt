package com.solvix.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

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
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val configuration = androidx.compose.ui.platform.LocalConfiguration.current
  val isSmallScreen = configuration.screenWidthDp < 360
  var menuAnchorBounds by remember { mutableStateOf(IntRect.Zero) }
  val isDark = isDarkTheme(theme)
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 20.dp, bottom = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.weight(1f, fill = false),
    ) {
      Box(
        modifier = Modifier
          .size(if (isSmallScreen) 40.dp else 48.dp)
          .clip(RoundedCornerShape(if (isSmallScreen) 12.dp else 16.dp))
          .background(if (isDark) colors.card else Color.White)
          .border(
            width = 1.dp,
            color = if (isDark) colors.cardBorder else colors.accent.copy(alpha = 0.25f),
            shape = RoundedCornerShape(if (isSmallScreen) 12.dp else 16.dp),
          ),
        contentAlignment = Alignment.Center,
      ) {
        if (theme == ThemeName.StandardLight || theme == ThemeName.StandardDark) {
          Icon(
            imageVector = Icons.Rounded.AccountBalanceWallet,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(if (isSmallScreen) 22.dp else 26.dp),
          )
        } else {
          Text(text = themeAppIcon(theme), fontSize = if (isSmallScreen) 22.sp else 26.sp)
        }
      }
      Spacer(modifier = Modifier.width(if (isSmallScreen) 8.dp else 12.dp))
      Column(modifier = Modifier.weight(1f, fill = false)) {
        Text(
          text = strings["app_name"],
          fontWeight = FontWeight.Bold,
          fontSize = if (isSmallScreen) 16.sp else 18.sp,
          color = colors.text,
          maxLines = 1,
          overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
        Text(
          text = strings["app_tagline"],
          color = colors.muted,
          fontSize = if (isSmallScreen) 10.sp else 11.sp,
          maxLines = 1,
          overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
      }
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      SyncStatusPill(
        onRetryClick = {
          scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            SyncQueueManager.processQueue(context)
          }
        },
      )

      Box {
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
          .background(colors.accent.copy(alpha = 0.12f))
          .border(1.dp, colors.accent.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
          .clickable(onClick = onProfileClick)
          .padding(horizontal = 10.dp, vertical = 6.dp)
          .onGloballyPositioned { coordinates ->
            val rect = coordinates.boundsInWindow()
            menuAnchorBounds = IntRect(
              rect.left.roundToInt(),
              rect.top.roundToInt(),
              rect.right.roundToInt(),
              rect.bottom.roundToInt(),
            )
          },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        if (theme == ThemeName.StandardLight || theme == ThemeName.StandardDark) {
          Icon(
            imageVector = Icons.Rounded.Person,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(16.dp),
          )
        }
        Text(
          text = if (displayUsername.isBlank()) strings["guest"] else displayUsername,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = colors.text,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.widthIn(max = 85.dp),
        )
        Icon(
          imageVector = Icons.Rounded.KeyboardArrowDown,
          contentDescription = null,
          tint = colors.accent,
          modifier = Modifier.size(16.dp),
        )
      }
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
            MenuItem(
              text = strings["admin_logout"],
              icon = themeSectionVisualIcon(theme, "logout", "🚪"),
              color = colors.danger,
            ) { onLogout() }
          } else {
            MenuItem(
              text = "Transfer Saldo",
              icon = themePageVisualIcon(theme, Page.Transfer),
              active = currentPage == Page.Transfer,
            ) { onNavigate(Page.Transfer) }
            MenuItem(
              text = strings["page_dreams"].ifBlank { "Target" },
              icon = themePageVisualIcon(theme, Page.Dreams),
              active = currentPage == Page.Dreams,
            ) { onNavigate(Page.Dreams) }
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
            MenuItem(
              text = strings["menu_logout"],
              icon = themeSectionVisualIcon(theme, "logout", "🚪"),
              color = colors.danger,
            ) { onLogout() }
          }
        }
      }
    }
  }
}
}
