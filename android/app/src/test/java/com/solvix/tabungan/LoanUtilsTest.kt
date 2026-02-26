package com.solvix.tabungan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoanUtilsTest {

  @Test
  fun remainingBalanceNeverNegative() {
    val entry = loan(principal = 500_000, paid = 650_000, monthly = 50_000, rate = 0.0)
    assertEquals(0, remainingLoanBalance(entry))
  }

  @Test
  fun payoffMonthsWithoutInterestIsDeterministic() {
    val entry = loan(principal = 1_200_000, paid = 0, monthly = 100_000, rate = 0.0)
    assertEquals(12, simulateLoanPayoffMonths(entry))
  }

  @Test
  fun payoffMonthsReturnsNullWhenPaymentCannotCoverInterest() {
    val entry = loan(principal = 1_000_000, paid = 0, monthly = 10_000, rate = 24.0)
    assertNull(simulateLoanPayoffMonths(entry))
  }

  @Test
  fun balanceAfterYearGetsLowerWhenPaymentIsHealthy() {
    val entry = loan(principal = 1_200_000, paid = 0, monthly = 80_000, rate = 12.0)
    val initial = remainingLoanBalance(entry)
    val nextYear = simulateLoanBalanceAfterYear(entry)
    assertTrue(nextYear in 1 until initial)
  }

  private fun loan(principal: Int, paid: Int, monthly: Int, rate: Double): LoanEntry {
    return LoanEntry(
      id = "loan-1",
      type = "credit_card",
      title = "Test Loan",
      principal = principal,
      paid = paid,
      monthlyPayment = monthly,
      annualInterestRate = rate,
      dueDate = "31-12-2026",
      note = "",
    )
  }
}
