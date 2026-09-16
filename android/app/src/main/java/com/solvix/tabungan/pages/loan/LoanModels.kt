package com.solvix.tabungan

enum class LoanTab { Debt, Receivable }

fun parseRate(text: String): Double {
  val normalized = text.trim().replace(',', '.')
  return normalized.toDoubleOrNull() ?: 0.0
}

fun formatRateInput(value: Double): String {
  return if (value % 1.0 == 0.0) {
    value.toInt().toString()
  } else {
    value.toString()
  }
}

fun loanTypeLabel(type: String, strings: AppStrings): String {
  return when (type) {
    "friend_debt" -> strings["loan_type_friend_debt"]
    "installment" -> strings["loan_type_installment"]
    "credit_card" -> strings["loan_type_credit_card"]
    "paylater" -> strings["loan_type_paylater"]
    else -> strings["loan_type_other"]
  }
}

fun loanPayoffEstimateLabel(months: Int?, strings: AppStrings): String {
  return when {
    months == null -> strings["loan_no_estimate"]
    months == 0 -> strings["loan_paid_off"]
    else -> "$months ${strings["loan_month_unit"]}"
  }
}
