package com.solvix.tabungan

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Abstraksi icon visual untuk mendukung Vector Drawable (Material 3)
 * pada tema Standard dan Themed Badge pada tema Kartun.
 */
sealed class ThemedVisualIcon {
  data class Vector(val imageVector: ImageVector) : ThemedVisualIcon()
  data class ThemedBadge(val symbol: String) : ThemedVisualIcon()
}

data class ThemeIconSet(
  val app: String,
  val dashboard: String,
  val income: String,
  val expense: String,
  val dreams: String,
  val history: String,
  val insights: String,
  val loans: String,
  val aiChat: String,
  val calculator: String,
  val report: String,
  val profile: String,
  val settings: String,
  val themes: String,
)

private val ThemeIconMap = mapOf(
  ThemeName.StandardLight to ThemeIconSet(
    app = "💰",
    dashboard = "🏠",
    income = "📥",
    expense = "🧾",
    dreams = "🌟",
    history = "📒",
    insights = "🧠",
    loans = "💳",
    aiChat = "🤖",
    calculator = "🧮",
    report = "📈",
    profile = "👤",
    settings = "⚙️",
    themes = "🎨",
  ),
  ThemeName.StandardDark to ThemeIconSet(
    app = "🌕",
    dashboard = "🌐",
    income = "💹",
    expense = "💸",
    dreams = "🔮",
    history = "🗃️",
    insights = "🧠",
    loans = "🏦",
    aiChat = "🛰️",
    calculator = "🧩",
    report = "📉",
    profile = "🧑‍💻",
    settings = "🛠️",
    themes = "🌑",
  ),
  ThemeName.CartoonFood to ThemeIconSet(
    app = "🍩",
    dashboard = "🍽️",
    income = "🍔",
    expense = "🍟",
    dreams = "🎂",
    history = "📜",
    insights = "☕",
    loans = "🍪",
    aiChat = "🍳",
    calculator = "🍭",
    report = "📊",
    profile = "👨‍🍳",
    settings = "🍴",
    themes = "🧁",
  ),
  ThemeName.CartoonSpace to ThemeIconSet(
    app = "🌠",
    dashboard = "🛸",
    income = "🚀",
    expense = "🌌",
    dreams = "🧑‍🚀",
    history = "🛰️",
    insights = "🪐",
    loans = "☄️",
    aiChat = "🤖",
    calculator = "🧭",
    report = "📡",
    profile = "👽",
    settings = "🔭",
    themes = "🌟",
  ),
  ThemeName.CartoonMonster to ThemeIconSet(
    app = "👾",
    dashboard = "🏰",
    income = "💰",
    expense = "👹",
    dreams = "🎃",
    history = "📜",
    insights = "🔮",
    loans = "🧛",
    aiChat = "🧠",
    calculator = "🧮",
    report = "📊",
    profile = "😈",
    settings = "⚙️",
    themes = "🧪",
  ),
  ThemeName.CartoonHero to ThemeIconSet(
    app = "🦸",
    dashboard = "🏟️",
    income = "⚡",
    expense = "💥",
    dreams = "🏆",
    history = "🗃️",
    insights = "💡",
    loans = "⛓️",
    aiChat = "🦾",
    calculator = "⏱️",
    report = "📈",
    profile = "🦹",
    settings = "🛡️",
    themes = "🎯",
  ),
  ThemeName.CartoonSea to ThemeIconSet(
    app = "🐠",
    dashboard = "🌊",
    income = "🪙",
    expense = "⚓",
    dreams = "🐋",
    history = "📜",
    insights = "🐚",
    loans = "🐙",
    aiChat = "🐬",
    calculator = "🦀",
    report = "🌊",
    profile = "🤿",
    settings = "☸️",
    themes = "⭐",
  ),
  ThemeName.CartoonPlant to ThemeIconSet(
    app = "🌻",
    dashboard = "🌿",
    income = "🌱",
    expense = "🍂",
    dreams = "🌸",
    history = "🪵",
    insights = "🌳",
    loans = "🌵",
    aiChat = "🐝",
    calculator = "🌰",
    report = "📈",
    profile = "🧑‍🌾",
    settings = "🪴",
    themes = "💐",
  ),
  ThemeName.CartoonPinky to ThemeIconSet(
    app = "🎀",
    dashboard = "💖",
    income = "💎",
    expense = "🛍️",
    dreams = "👑",
    history = "📖",
    insights = "🔮",
    loans = "💘",
    aiChat = "🧸",
    calculator = "🪞",
    report = "✨",
    profile = "👗",
    settings = "🪄",
    themes = "💄",
  ),
  ThemeName.CartoonColorful to ThemeIconSet(
    app = "🤹",
    dashboard = "🎪",
    income = "🎈",
    expense = "🎆",
    dreams = "🎡",
    history = "🎟️",
    insights = "🎯",
    loans = "🎭",
    aiChat = "🤡",
    calculator = "🎲",
    report = "🎢",
    profile = "🤠",
    settings = "🎡",
    themes = "🎩",
  ),
)

fun themeAppIcon(theme: ThemeName): String {
  return ThemeIconMap[theme]?.app ?: "💰"
}

fun themePageIcon(theme: ThemeName, page: Page): String {
  val set = ThemeIconMap[theme]
  if (set == null) return "📌"
  return when (page) {
    Page.Dashboard -> set.dashboard
    Page.Income -> set.income
    Page.Expense -> set.expense
    Page.Dreams -> set.dreams
    Page.History -> set.history
    Page.Insights -> set.insights
    Page.Loans -> set.loans
    Page.AIChat -> set.aiChat
    Page.Calculator -> set.calculator
    Page.Report -> set.report
    Page.Profile -> set.profile
    Page.Settings -> set.settings
    Page.Themes -> set.themes
  }
}

/**
 * Mengembalikan [ThemedVisualIcon] modern:
 * - Pada StandardLight & StandardDark: Menggunakan Material 3 Rounded Vector Icon
 * - Pada 8 tema Kartun: Menggunakan Themed Badge sesuai karakter tematik tema tersebut
 */
fun themePageVisualIcon(theme: ThemeName, page: Page): ThemedVisualIcon {
  return when (theme) {
    ThemeName.StandardLight, ThemeName.StandardDark -> {
      val vector = when (page) {
        Page.Dashboard -> Icons.Rounded.Dashboard
        Page.Income -> Icons.Rounded.AccountBalanceWallet
        Page.Expense -> Icons.AutoMirrored.Rounded.ReceiptLong
        Page.Dreams -> Icons.Rounded.Stars
        Page.History -> Icons.Rounded.History
        Page.Insights -> Icons.Rounded.Psychology
        Page.Loans -> Icons.Rounded.CreditCard
        Page.AIChat -> Icons.Rounded.SmartToy
        Page.Calculator -> Icons.Rounded.Calculate
        Page.Report -> Icons.Rounded.Assessment
        Page.Profile -> Icons.Rounded.Person
        Page.Settings -> Icons.Rounded.Settings
        Page.Themes -> Icons.Rounded.Palette
      }
      ThemedVisualIcon.Vector(vector)
    }
    else -> {
      ThemedVisualIcon.ThemedBadge(themePageIcon(theme, page))
    }
  }
}

/**
 * Mengembalikan visual icon untuk section umum (chart, trend, kategori).
 */
fun themeSectionVisualIcon(theme: ThemeName, iconType: String, cartoonEmoji: String): ThemedVisualIcon {
  return when (theme) {
    ThemeName.StandardLight, ThemeName.StandardDark -> {
      when (iconType) {
        "pie" -> ThemedVisualIcon.Vector(Icons.Rounded.PieChart)
        "trend" -> ThemedVisualIcon.Vector(Icons.AutoMirrored.Rounded.TrendingUp)
        "bar" -> ThemedVisualIcon.Vector(Icons.Rounded.BarChart)
        else -> ThemedVisualIcon.ThemedBadge(cartoonEmoji)
      }
    }
    else -> ThemedVisualIcon.ThemedBadge(cartoonEmoji)
  }
}

