package com.solvix.tabungan

import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

enum class AppLanguage { ID, EN }

data class AppStrings(val map: Map<String, String>) {
  operator fun get(key: String): String = map[key] ?: ""
}

val LocalStrings = staticCompositionLocalOf { stringsFor(AppLanguage.ID) }
val LocalLanguage = staticCompositionLocalOf { AppLanguage.ID }

enum class OptionList {
  Countries,
  IncomeSources,
  IncomeChannels,
  ExpenseMethods,
  ExpenseBanks,
}

fun stringsFor(language: AppLanguage): AppStrings {
  return AppStrings(if (language == AppLanguage.ID) idStrings else enStrings)
}

fun optionList(language: AppLanguage, list: OptionList): List<String> {
  return when (list) {
    OptionList.Countries -> if (language == AppLanguage.ID) {
      allCountries(Locale.Builder().setLanguage("id").setRegion("ID").build())
    } else {
      allCountries(Locale.ENGLISH)
    }
    OptionList.IncomeSources -> if (language == AppLanguage.ID) {
      listOf("Kerja", "Bisnis", "Freelance", "Bonus", "Tabungan", "Investasi", "Lainnya")
    } else {
      listOf("Salary", "Business", "Freelance", "Bonus", "Savings", "Investment", "Other")
    }
    OptionList.IncomeChannels -> if (language == AppLanguage.ID) {
      listOf("SeaBank", "Crypto", "Saham", "BCA", "Mandiri", "BNI", "BRI", "Cash", "E-Wallet", "Lainnya")
    } else {
      listOf("SeaBank", "Crypto", "Stocks", "BCA", "Mandiri", "BNI", "BRI", "Cash", "E-Wallet", "Other")
    }
    OptionList.ExpenseMethods -> if (language == AppLanguage.ID) {
      listOf("Transfer", "QRIS", "Cash", "Debit", "Kredit")
    } else {
      listOf("Transfer", "QRIS", "Cash", "Debit", "Credit")
    }
    OptionList.ExpenseBanks -> if (language == AppLanguage.ID) {
      listOf("SeaBank", "Crypto", "Saham", "BCA", "Mandiri", "BNI", "BRI", "DANA", "OVO", "GoPay", "ShopeePay", "Cash", "Lainnya")
    } else {
      listOf("SeaBank", "Crypto", "Stocks", "BCA", "Mandiri", "BNI", "BRI", "DANA", "OVO", "GoPay", "ShopeePay", "Cash", "Other")
    }
  }
}

private fun allCountries(locale: Locale): List<String> {
  return Locale.getISOCountries()
    .map { code -> Locale.Builder().setRegion(code).build().getDisplayCountry(locale) }
    .filter { it.isNotBlank() }
    .distinct()
    .sorted()
}
