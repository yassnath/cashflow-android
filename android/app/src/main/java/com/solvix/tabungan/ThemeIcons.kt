package com.solvix.tabungan

data class ThemeIconSet(
  val app: String,
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
    income = "🍔",
    expense = "🍟",
    dreams = "🎂",
    history = "📒",
    insights = "🧠",
    loans = "🍪",
    aiChat = "🍳",
    calculator = "🍭",
    report = "📊",
    profile = "😊",
    settings = "⚙️",
    themes = "🧁",
  ),
  ThemeName.CartoonSpace to ThemeIconSet(
    app = "🌠",
    income = "🚀",
    expense = "🌌",
    dreams = "🧑‍🚀",
    history = "🛰️",
    insights = "🧠",
    loans = "🪐",
    aiChat = "🤖",
    calculator = "🧭",
    report = "🪂",
    profile = "👽",
    settings = "🔭",
    themes = "🌟",
  ),
  ThemeName.CartoonMonster to ThemeIconSet(
    app = "👾",
    income = "🧟",
    expense = "👹",
    dreams = "🎃",
    history = "📗",
    insights = "🧠",
    loans = "🧛",
    aiChat = "🧠",
    calculator = "🧮",
    report = "📊",
    profile = "😈",
    settings = "⚙️",
    themes = "🧬",
  ),
  ThemeName.CartoonHero to ThemeIconSet(
    app = "🦸",
    income = "🛡️",
    expense = "⚔️",
    dreams = "🏆",
    history = "📜",
    insights = "🧠",
    loans = "🏛️",
    aiChat = "🦾",
    calculator = "🧮",
    report = "📈",
    profile = "🦹",
    settings = "⚙️",
    themes = "🎯",
  ),
  ThemeName.CartoonSea to ThemeIconSet(
    app = "🐳",
    income = "🐠",
    expense = "🐙",
    dreams = "🐬",
    history = "⚓",
    insights = "🧠",
    loans = "🦀",
    aiChat = "🐳",
    calculator = "🧮",
    report = "📊",
    profile = "🐚",
    settings = "⚙️",
    themes = "🌊",
  ),
  ThemeName.CartoonPlant to ThemeIconSet(
    app = "🌿",
    income = "🌱",
    expense = "🍃",
    dreams = "🌼",
    history = "🌳",
    insights = "🧠",
    loans = "🌰",
    aiChat = "🧠",
    calculator = "🧮",
    report = "📊",
    profile = "😊",
    settings = "🪴",
    themes = "🌸",
  ),
  ThemeName.CartoonPinky to ThemeIconSet(
    app = "💖",
    income = "💗",
    expense = "💞",
    dreams = "💝",
    history = "🎀",
    insights = "🧠",
    loans = "💸",
    aiChat = "🧠",
    calculator = "🧮",
    report = "📊",
    profile = "😊",
    settings = "⚙️",
    themes = "💫",
  ),
  ThemeName.CartoonColorful to ThemeIconSet(
    app = "🌈",
    income = "🎈",
    expense = "🎨",
    dreams = "🎉",
    history = "🎯",
    insights = "🧠",
    loans = "🪙",
    aiChat = "🤖",
    calculator = "🧮",
    report = "📊",
    profile = "😊",
    settings = "⚙️",
    themes = "🎊",
  ),
)

fun themeAppIcon(theme: ThemeName): String {
  return ThemeIconMap[theme]?.app ?: "💰"
}

fun themePageIcon(theme: ThemeName, page: Page): String {
  val set = ThemeIconMap[theme]
  if (set == null) return "📌"
  return when (page) {
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
