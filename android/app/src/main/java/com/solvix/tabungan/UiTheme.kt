package com.solvix.tabungan

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

enum class ThemeName(val label: String) {
  StandardLight("Standard Light"),
  StandardDark("Standard Dark"),
  CartoonFood("Kartun Makanan"),
  CartoonSpace("Kartun Luar Angkasa"),
  CartoonMonster("Kartun Monster"),
  CartoonHero("Kartun Superhero"),
  CartoonSea("Kartun Laut"),
  CartoonPlant("Kartun Tanaman"),
  CartoonPinky("Kartun Pinky"),
  CartoonColorful("Kartun Colorful"),
}

@Immutable
data class AppColors(
  val bg: Color,
  val bg2: Color,
  val text: Color,
  val muted: Color,
  val placeholder: Color,
  val card: Color,
  val cardBorder: Color,
  val fieldBorder: Color,
  val accent: Color,
  val accent2: Color,
  val danger: Color,
  val onGradient: Color,
  val onDanger: Color,
)

private val StandardLightPalette = AppColors(
  bg = Color(0xFFF8FAFD),
  bg2 = Color(0xFFEEF4FF),
  text = Color(0xFF0F172A),
  muted = Color(0xFF475569),
  placeholder = Color(0xFF94A3B8),
  card = Color(0xFFFFFFFF),
  cardBorder = Color(0xFFE2E8F0),
  fieldBorder = Color(0xFFCBD5E1),
  accent = Color(0xFF2563EB), // Electric Royal Blue
  accent2 = Color(0xFF06B6D4), // Vivid Electric Cyan
  danger = Color(0xFFF43F5E), // Vivid Rose Red
  onGradient = Color.White,
  onDanger = Color.White,
)

private val StandardDarkPalette = AppColors(
  bg = Color(0xFF0A0E1A), // Deep luminous cyber navy
  bg2 = Color(0xFF141C30),
  text = Color(0xFFF8FAFC),
  muted = Color(0xFF94A3B8),
  placeholder = Color(0xFF64748B),
  card = Color(0xFF101728),
  cardBorder = Color(0xFF1E293B),
  fieldBorder = Color(0xFF334155),
  accent = Color(0xFF6366F1), // Vivid Electric Indigo
  accent2 = Color(0xFF00E5FF), // Luminous Cyber Neon Cyan
  danger = Color(0xFFFF3366),
  onGradient = Color.White,
  onDanger = Color.White,
)

private val CartoonFoodPalette = AppColors(
  bg = Color(0xFFFFF9F2), // Warm, bright, sunny cream
  bg2 = Color(0xFFFFEEDB), // Fresh juicy peach tint
  text = Color(0xFF2D1808), // Rich deep espresso
  muted = Color(0xFF7C5035),
  placeholder = Color(0xFFA8826D),
  card = Color(0xFFFFFFFF),
  cardBorder = Color(0xFFFFDFC4),
  fieldBorder = Color(0xFFFFCE9F),
  accent = Color(0xFFFF6D00), // Juicy Neon Tangerine
  accent2 = Color(0xFFFF2A6D), // Fresh Strawberry Coral
  danger = Color(0xFFE11D48),
  onGradient = Color.White,
  onDanger = Color.White,
)

private val CartoonSpacePalette = AppColors(
  bg = Color(0xFF0C0E24), // Deep galactic space
  bg2 = Color(0xFF171B42),
  text = Color(0xFFFFFFFF),
  muted = Color(0xFFA5B4FC), // Luminous soft lavender
  placeholder = Color(0xFF818CF8),
  card = Color(0xFF13173A),
  cardBorder = Color(0x38A855F7), // Luminous purple nebula glow
  fieldBorder = Color(0x52818CF8),
  accent = Color(0xFFA855F7), // Vivid Cosmic Ultraviolet
  accent2 = Color(0xFF00F0FF), // Electric Neon Cyan
  danger = Color(0xFFFF2A6D),
  onGradient = Color.White,
  onDanger = Color.White,
)

private val CartoonMonsterPalette = AppColors(
  bg = Color(0xFFF2FDF5), // Bright, lively electric mint cream
  bg2 = Color(0xFFD8FBE5), // Vibrant lime-mint tint
  text = Color(0xFF064E3B), // Deep rich emerald
  muted = Color(0xFF059669),
  placeholder = Color(0xFF6EE7B7),
  card = Color(0xFFFFFFFF),
  cardBorder = Color(0xFFBBF7D0),
  fieldBorder = Color(0xFF86EFAC),
  accent = Color(0xFF10B981), // Electric Emerald Green
  accent2 = Color(0xFF84CC16), // Neon Slime Lime
  danger = Color(0xFFF43F5E),
  onGradient = Color.White,
  onDanger = Color.White,
)

private val CartoonHeroPalette = AppColors(
  bg = Color(0xFFF5F8FF), // Bright heroic energetic comic white
  bg2 = Color(0xFFE2ECFF), // Dynamic comic blue tint
  text = Color(0xFF0A184A), // Deep heroic navy
  muted = Color(0xFF2563EB),
  placeholder = Color(0xFF93C5FD),
  card = Color(0xFFFFFFFF),
  cardBorder = Color(0xFFBFDBFE),
  fieldBorder = Color(0xFF93C5FD),
  accent = Color(0xFF1D4ED8), // Electric Superhero Cobalt
  accent2 = Color(0xFFEF4444), // Crimson Superhero Scarlet
  danger = Color(0xFFDC2626),
  onGradient = Color.White,
  onDanger = Color.White,
)

private val CartoonSeaPalette = AppColors(
  bg = Color(0xFFF0FBFF), // Sunlit lagoon radiant turquoise water
  bg2 = Color(0xFFD7F4FE), // Bright clear coral sea blue
  text = Color(0xFF082F49), // Deep ocean abyss
  muted = Color(0xFF0284C7),
  placeholder = Color(0xFF7DD3FC),
  card = Color(0xFFFFFFFF),
  cardBorder = Color(0xFFBAE6FD),
  fieldBorder = Color(0xFF7DD3FC),
  accent = Color(0xFF0284C7), // Vivid Azure Ocean Blue
  accent2 = Color(0xFF06B6D4), // Tropical Neon Turquoise
  danger = Color(0xFFF43F5E),
  onGradient = Color.White,
  onDanger = Color.White,
)

private val CartoonPlantPalette = AppColors(
  bg = Color(0xFFF4FDF3), // Sunny greenhouse bright leaf white
  bg2 = Color(0xFFE0FBDC), // Fresh sprout green
  text = Color(0xFF143812), // Deep lush botanical evergreen
  muted = Color(0xFF16A34A),
  placeholder = Color(0xFF86EFAC),
  card = Color(0xFFFFFFFF),
  cardBorder = Color(0xFFC7F5BD),
  fieldBorder = Color(0xFFA3E694),
  accent = Color(0xFF16A34A), // Vibrant Spring Garden Green
  accent2 = Color(0xFF22C55E), // Lush Radiant Emerald
  danger = Color(0xFFE11D48),
  onGradient = Color.White,
  onDanger = Color.White,
)

private val CartoonPinkyPalette = AppColors(
  bg = Color(0xFFFFF5FA), // Luminous bright sakura blossom white
  bg2 = Color(0xFFFFE0EE), // Sweet bubblegum pink
  text = Color(0xFF4A0E2E), // Rich deep berry
  muted = Color(0xFFDB2777), // Vivid magenta pink
  placeholder = Color(0xFFF472B6),
  card = Color(0xFFFFFFFF),
  cardBorder = Color(0xFFFFC5DF),
  fieldBorder = Color(0xFFF9A8D4),
  accent = Color(0xFFF43F5E), // Vivid Rose Berry / Neon Pink
  accent2 = Color(0xFFEC4899), // Bright Sweet Bubblegum
  danger = Color(0xFFBE123C),
  onGradient = Color.White,
  onDanger = Color.White,
)

private val CartoonColorfulPalette = AppColors(
  bg = Color(0xFFF8F5FF), // Luminous bright violet-white with joyful energy
  bg2 = Color(0xFFEEE7FE), // Bright cheerful lavender
  text = Color(0xFF1E1B4B), // Deep cosmic indigo
  muted = Color(0xFF6366F1), // Electric indigo
  placeholder = Color(0xFFA5B4FC),
  card = Color(0xFFFFFFFF),
  cardBorder = Color(0xFFDDD6FE),
  fieldBorder = Color(0xFFC4B5FD),
  accent = Color(0xFF7C3AED), // Vivid Electric Ultraviolet
  accent2 = Color(0xFFFF5722), // Radiant Sunset Neon Orange
  danger = Color(0xFFF43F5E),
  onGradient = Color.White,
  onDanger = Color.White,
)

private val ThemePalettes = mapOf(
  ThemeName.StandardLight to StandardLightPalette,
  ThemeName.StandardDark to StandardDarkPalette,
  ThemeName.CartoonFood to CartoonFoodPalette,
  ThemeName.CartoonSpace to CartoonSpacePalette,
  ThemeName.CartoonMonster to CartoonMonsterPalette,
  ThemeName.CartoonHero to CartoonHeroPalette,
  ThemeName.CartoonSea to CartoonSeaPalette,
  ThemeName.CartoonPlant to CartoonPlantPalette,
  ThemeName.CartoonPinky to CartoonPinkyPalette,
  ThemeName.CartoonColorful to CartoonColorfulPalette,
)

val LocalAppColors = staticCompositionLocalOf { StandardLightPalette }
val LocalThemeName = staticCompositionLocalOf { ThemeName.StandardLight }

private val AppFontFamily = FontFamily(
  Font(R.font.fredoka_regular, FontWeight.Normal),
  Font(R.font.fredoka_medium, FontWeight.Medium),
  Font(R.font.fredoka_semibold, FontWeight.SemiBold),
  Font(R.font.fredoka_bold, FontWeight.Bold),
)

private val AppTypography = Typography(
  bodyLarge = TextStyle(fontFamily = AppFontFamily, fontSize = 16.sp, fontWeight = FontWeight.Normal),
  bodyMedium = TextStyle(fontFamily = AppFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal),
  bodySmall = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal),
  titleLarge = TextStyle(fontFamily = AppFontFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold),
  titleMedium = TextStyle(fontFamily = AppFontFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold),
  titleSmall = TextStyle(fontFamily = AppFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold),
)

@Composable
fun TabunganTheme(theme: ThemeName, content: @Composable () -> Unit) {
  val palette = ThemePalettes[theme] ?: StandardLightPalette
  val colorScheme = colorSchemeFor(theme, palette)
  CompositionLocalProvider(
    LocalAppColors provides palette,
    LocalThemeName provides theme,
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = AppTypography,
      content = content,
    )
  }
}

fun isDarkTheme(theme: ThemeName): Boolean {
  return theme == ThemeName.StandardDark || theme == ThemeName.CartoonSpace
}

private fun colorSchemeFor(theme: ThemeName, palette: AppColors): ColorScheme {
  val base = if (theme == ThemeName.StandardDark || theme == ThemeName.CartoonSpace) {
    darkColorScheme()
  } else {
    lightColorScheme()
  }
  return base.copy(
    primary = palette.accent,
    secondary = palette.accent2,
    error = palette.danger,
    background = palette.bg,
    surface = palette.card,
    onPrimary = palette.onGradient,
    onSecondary = palette.onGradient,
    onBackground = palette.text,
    onSurface = palette.text,
    onError = palette.onDanger,
  )
}
