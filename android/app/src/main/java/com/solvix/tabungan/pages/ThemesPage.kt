package com.solvix.tabungan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.solvix.tabungan.LocalStrings
import com.solvix.tabungan.LocalThemeName
import com.solvix.tabungan.Page
import com.solvix.tabungan.SectionTitle
import com.solvix.tabungan.ThemeCard
import com.solvix.tabungan.ThemeName
import com.solvix.tabungan.themeAppIcon
import com.solvix.tabungan.themePageIcon

@Composable
fun ThemesPage(currentTheme: ThemeName, onThemeSelected: (ThemeName) -> Unit) {
  val strings = LocalStrings.current
  Column {
    SectionTitle(icon = themePageIcon(LocalThemeName.current, Page.Themes), title = strings["section_themes_title"], subtitle = strings["section_themes_subtitle"])
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      ThemeCard(themeAppIcon(ThemeName.StandardLight), strings["theme_standard_light"], currentTheme == ThemeName.StandardLight, Color(0xFFFFF6E7)) {
        onThemeSelected(ThemeName.StandardLight)
      }
      ThemeCard(themeAppIcon(ThemeName.StandardDark), strings["theme_standard_dark"], currentTheme == ThemeName.StandardDark, Color(0xFFE6EBFF)) {
        onThemeSelected(ThemeName.StandardDark)
      }
      ThemeCard(themeAppIcon(ThemeName.CartoonFood), strings["theme_food"], currentTheme == ThemeName.CartoonFood, Color(0xFFFFF0DC)) {
        onThemeSelected(ThemeName.CartoonFood)
      }
      ThemeCard(themeAppIcon(ThemeName.CartoonSpace), strings["theme_space"], currentTheme == ThemeName.CartoonSpace, Color(0xFFE3E9FF)) {
        onThemeSelected(ThemeName.CartoonSpace)
      }
      ThemeCard(themeAppIcon(ThemeName.CartoonMonster), strings["theme_monster"], currentTheme == ThemeName.CartoonMonster, Color(0xFFE9FFF2)) {
        onThemeSelected(ThemeName.CartoonMonster)
      }
      ThemeCard(themeAppIcon(ThemeName.CartoonHero), strings["theme_hero"], currentTheme == ThemeName.CartoonHero, Color(0xFFFFE9EE)) {
        onThemeSelected(ThemeName.CartoonHero)
      }
      ThemeCard(themeAppIcon(ThemeName.CartoonSea), strings["theme_sea"], currentTheme == ThemeName.CartoonSea, Color(0xFFE6F6FF)) {
        onThemeSelected(ThemeName.CartoonSea)
      }
      ThemeCard(themeAppIcon(ThemeName.CartoonPlant), strings["theme_plant"], currentTheme == ThemeName.CartoonPlant, Color(0xFFEFFFE5)) {
        onThemeSelected(ThemeName.CartoonPlant)
      }
      ThemeCard(themeAppIcon(ThemeName.CartoonPinky), strings["theme_pinky"], currentTheme == ThemeName.CartoonPinky, Color(0xFFFFE6F2)) {
        onThemeSelected(ThemeName.CartoonPinky)
      }
      ThemeCard(themeAppIcon(ThemeName.CartoonColorful), strings["theme_colorful"], currentTheme == ThemeName.CartoonColorful, Color(0xFFEEF3FF)) {
        onThemeSelected(ThemeName.CartoonColorful)
      }
    }
  }
}
