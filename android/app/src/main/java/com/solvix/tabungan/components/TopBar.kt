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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
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
  val configuration = LocalConfiguration.current
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

    Box {
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(999.dp))
          .background(colors.accent.copy(alpha = 0.12f))
          .border(1.dp, colors.accent.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
          .clickable(onClick = onProfileClick)
          .heightIn(min = 44.dp)
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
        horizontalArrangement = Arrangement.spacedBy(6.dp),
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
          modifier = Modifier.widthIn(max = 95.dp),
        )
        Icon(
          imageVector = Icons.Rounded.KeyboardArrowDown,
          contentDescription = null,
          tint = colors.accent,
          modifier = Modifier.size(16.dp),
        )
      }

      val maxMenuHeight = (configuration.screenHeightDp * 0.76f).dp
      DropDownMenuCard(
        expanded = showMenu,
        onDismiss = onDismissMenu,
        modifier = Modifier
          .width(260.dp)
          .heightIn(max = maxMenuHeight),
        anchorBounds = menuAnchorBounds,
        alignRight = true,
        xOffsetDp = (-2).dp,
        yOffsetDp = 10.dp,
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          // Profile Header
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f, fill = false),
            ) {
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(colors.accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Rounded.Person,
                  contentDescription = null,
                  tint = colors.accent,
                  modifier = Modifier.size(18.dp),
                )
              }
              Column {
                Text(
                  text = displayName.ifBlank { strings["guest"] },
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = colors.text,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = "@${displayUsername.ifBlank { "user" }}",
                  color = colors.muted,
                  fontSize = 11.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(colors.bg2)
                .clickable { onDismissMenu() },
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = strings["close"].ifBlank { "Tutup" },
                tint = colors.muted,
                modifier = Modifier.size(16.dp),
              )
            }
          }

          HorizontalDivider(color = colors.cardBorder, thickness = 0.8.dp)

          if (isAdmin) {
            MenuItem(
              text = strings["admin_logout"],
              icon = themeSectionVisualIcon(theme, "logout", "🚪"),
              color = colors.danger,
            ) { onLogout() }
          } else {
            // Section: Transaksi & Target
            Text(
              text = "AKTIVITAS FINANSIAL",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = colors.muted,
              letterSpacing = 0.8.sp,
              modifier = Modifier.padding(start = 4.dp, top = 4.dp),
            )

            MenuItem(
              text = "Transfer Saldo",
              icon = themePageVisualIcon(theme, Page.Transfer),
              active = currentPage == Page.Transfer,
            ) { onNavigate(Page.Transfer) }

            MenuItem(
              text = strings["page_dreams"].ifBlank { "Target Impian" },
              icon = themePageVisualIcon(theme, Page.Dreams),
              active = currentPage == Page.Dreams,
            ) { onNavigate(Page.Dreams) }

            MenuItem(
              text = strings["menu_history"].ifBlank { "Riwayat Transaksi" },
              icon = themePageVisualIcon(theme, Page.History),
              active = currentPage == Page.History,
            ) { onNavigate(Page.History) }

            MenuItem(
              text = strings["menu_loans"].ifBlank { "Catatan Hutang" },
              icon = themePageVisualIcon(theme, Page.Loans),
              active = currentPage == Page.Loans,
            ) { onNavigate(Page.Loans) }

            HorizontalDivider(color = colors.cardBorder, thickness = 0.8.dp, modifier = Modifier.padding(vertical = 2.dp))

            // Section: Fitur & Alat
            Text(
              text = "FITUR & ALAT",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = colors.muted,
              letterSpacing = 0.8.sp,
              modifier = Modifier.padding(start = 4.dp, top = 2.dp),
            )

            MenuItem(
              text = strings["menu_ai_chat"].ifBlank { "AI Finansial Chat" },
              icon = themePageVisualIcon(theme, Page.AIChat),
              active = currentPage == Page.AIChat,
            ) { onNavigate(Page.AIChat) }

            MenuItem(
              text = strings["menu_calculator"].ifBlank { "Kalkulator Finansial" },
              icon = themePageVisualIcon(theme, Page.Calculator),
              active = currentPage == Page.Calculator,
            ) { onNavigate(Page.Calculator) }

            MenuItem(
              text = strings["menu_insights"].ifBlank { "Analisis & Grafik" },
              icon = themePageVisualIcon(theme, Page.Insights),
              active = currentPage == Page.Insights,
            ) { onNavigate(Page.Insights) }

            MenuItem(
              text = strings["menu_report"].ifBlank { "Ekspor Laporan" },
              icon = themePageVisualIcon(theme, Page.Report),
              active = currentPage == Page.Report,
            ) { onNavigate(Page.Report) }

            HorizontalDivider(color = colors.cardBorder, thickness = 0.8.dp, modifier = Modifier.padding(vertical = 2.dp))

            // Section: Pengaturan & Akun
            Text(
              text = "PENGATURAN & AKUN",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = colors.muted,
              letterSpacing = 0.8.sp,
              modifier = Modifier.padding(start = 4.dp, top = 2.dp),
            )

            MenuItem(
              text = strings["menu_theme"].ifBlank { "Pilihan Tema" },
              icon = themePageVisualIcon(theme, Page.Themes),
              active = currentPage == Page.Themes,
            ) { onNavigate(Page.Themes) }

            MenuItem(
              text = strings["menu_profile"].ifBlank { "Profil Saya" },
              icon = themePageVisualIcon(theme, Page.Profile),
              active = currentPage == Page.Profile,
            ) { onNavigate(Page.Profile) }

            MenuItem(
              text = strings["menu_settings"].ifBlank { "Pengaturan Sistem" },
              icon = themePageVisualIcon(theme, Page.Settings),
              active = currentPage == Page.Settings,
            ) { onNavigate(Page.Settings) }

            HorizontalDivider(color = colors.cardBorder, thickness = 0.8.dp, modifier = Modifier.padding(vertical = 2.dp))

            MenuItem(
              text = strings["menu_logout"].ifBlank { "Keluar Akun" },
              icon = themeSectionVisualIcon(theme, "logout", "🚪"),
              color = colors.danger,
            ) { onLogout() }
          }
        }
      }
    }
  }
}
