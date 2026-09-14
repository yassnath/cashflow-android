package com.solvix.tabungan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Halaman utama Dashboard CashFlow Android.
 * Komponen visual telah dipecah modular ke folder `pages/dashboard/` untuk maintainability:
 * - [DashboardBanner]: Sapaan pengguna, tanggal, status finansial, kartu saldo animasi, & pill masuk/keluar
 * - [DashboardQuickActions]: Tombol aksi cepat (Masuk, Keluar, Target, AI)
 * - [DashboardFlowDonutChart]: Donut chart perbandingan Pemasukkan vs Pengeluaran
 * - [DashboardTrendLineChart]: Grafik garis tren bulanan 12 bulan
 * - [DashboardTopExpenseBars]: Grafik batang horizontal top 5 kategori pengeluaran
 * - [DashboardGoalCards]: Kartu progres target tabungan impian
 * - [DashboardLoanSummary]: Ringkasan pinjaman & hutang aktif
 */
@Composable
fun DashboardPage(
  incomeEntries: List<MoneyEntry>,
  expenseEntries: List<MoneyEntry>,
  dreamEntries: List<DreamEntry>,
  loanEntries: List<LoanEntry>,
  currentUser: UserProfile?,
  currentLang: AppLanguage,
  strings: AppStrings,
  onNavigateTo: (Page) -> Unit,
) {
  // -- Ringkasan Finansial Utama --
  val totalIncome = incomeEntries.sumOf { it.amount }
  val totalExpense = expenseEntries.sumOf { it.amount }
  val netBalance = totalIncome - totalExpense

  // -- Filter Transaksi Bulan Ini --
  val now = Calendar.getInstance()
  val currentYear = now.get(Calendar.YEAR)
  val currentMonth = now.get(Calendar.MONTH)

  val thisMonthExpenses = expenseEntries.filter { entry ->
    val date = parseDate(entry.date) ?: return@filter false
    val cal = Calendar.getInstance().apply { timeInMillis = date }
    cal.get(Calendar.YEAR) == currentYear && cal.get(Calendar.MONTH) == currentMonth
  }

  // -- Format Tanggal & Nama Pengguna --
  val locale = if (currentLang == AppLanguage.ID) {
    Locale.Builder().setLanguage("id").setRegion("ID").build()
  } else {
    Locale.US
  }
  val dateFmt = remember(currentLang) { SimpleDateFormat("EEEE, d MMMM yyyy", locale) }
  val todayFormatted = remember(currentLang) { dateFmt.format(Date()) }

  val displayName = currentUser?.name?.takeIf { it.isNotBlank() }
    ?: currentUser?.username?.takeIf { it.isNotBlank() }
    ?: strings["guest"]

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // 1. Sapaan & Saldo Bersih Beranimasi
    DashboardBanner(
      displayName = displayName,
      todayFormatted = todayFormatted,
      netBalance = netBalance,
      totalIncome = totalIncome,
      totalExpense = totalExpense,
      strings = strings,
      locale = locale,
    )

    // 2. Baris Aksi Cepat
    DashboardQuickActions(
      strings = strings,
      onNavigateTo = onNavigateTo,
    )

    // 3. Donut Chart - Pemasukkan vs Pengeluaran
    DashboardFlowDonutChart(
      totalIncome = totalIncome,
      totalExpense = totalExpense,
      strings = strings,
    )

    // 4. Line Chart - Tren Bulanan
    DashboardTrendLineChart(
      incomeEntries = incomeEntries,
      expenseEntries = expenseEntries,
      currentLang = currentLang,
      strings = strings,
    )

    // 5. Bar Chart Horizontal - Top 5 Kategori Pengeluaran
    DashboardTopExpenseBars(
      thisMonthExpenses = thisMonthExpenses,
      allExpenseEntries = expenseEntries,
      strings = strings,
    )

    // 6. Progres Target Impian
    DashboardGoalCards(
      dreamEntries = dreamEntries,
      strings = strings,
      onNavigateTo = onNavigateTo,
    )

    // 7. Ringkasan Hutang & Pinjaman
    DashboardLoanSummary(
      loanEntries = loanEntries,
      strings = strings,
      onNavigateTo = onNavigateTo,
    )

    Spacer(modifier = Modifier.height(16.dp))
  }
}
