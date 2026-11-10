package com.solvix.tabungan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.solvix.tabungan.pages.dashboard.DashboardAssetsCard
import com.solvix.tabungan.pages.dashboard.DashboardBurnRateCard
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Halaman utama Dashboard CashFlow Android yang kaya visual, interaktif, dan modular:
 * 1. [DashboardBanner]: Sapaan pengguna, tanggal, status finansial, kartu saldo counter animasi, & pill masuk/keluar
 * 2. [DashboardQuickActions]: Tombol aksi cepat (Masuk, Keluar, Target, AI)
 * 3. [DashboardFinancialHealthCard]: Skor kesehatan finansial, rasio tabungan, & rata-rata pengeluaran harian
 * 4. [DashboardFlowDonutChart]: Donut chart perbandingan Pemasukkan vs Pengeluaran beranimasi
 * 5. [DashboardTrendLineChart]: Grafik garis tren bulanan reaktif dengan pemilih tahun & animasi
 * 6. [DashboardRecentTransactions]: Daftar transaksi terkini dengan ikon & navigasi cepat ke riwayat
 * 7. [DashboardTopExpenseBars]: Grafik batang horizontal top 5 kategori pengeluaran beranimasi
 * 8. [DashboardPaymentMethods]: Distribusi pengeluaran per metode pembayaran / dompet
 * 9. [DashboardUpcomingBills]: Pengingat tagihan pinjaman & target jatuh tempo
 * 10. [DashboardGoalCards]: Kartu progres target tabungan impian
 * 11. [DashboardLoanSummary]: Ringkasan pinjaman & hutang aktif
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
  transferEntries: List<MoneyEntry> = emptyList(),
) {
  val context = LocalContext.current
  val initialVersion by InitialBalanceManager.versionState
  val totalIncome = incomeEntries.sumOf { it.amount }
  val totalExpense = expenseEntries.sumOf { it.amount }
  val netBalance = totalIncome - totalExpense
  val totalInitial = remember(initialVersion) { InitialBalanceManager.getTotalInitialBalance(context) }
  val effectiveNetBalance = (netBalance.toLong() + totalInitial).toInt()

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

  Box(modifier = Modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // 1. Sapaan & Saldo Bersih Beranimasi
      DashboardBanner(
        displayName = displayName,
        todayFormatted = todayFormatted,
        netBalance = effectiveNetBalance,
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

      // 3. Aset & Saldo Akun / Dompet
      DashboardAssetsCard(
        incomeEntries = incomeEntries,
        expenseEntries = expenseEntries,
        transferEntries = transferEntries,
        netBalance = netBalance.toLong() + totalInitial,
        strings = strings,
        onTransferClick = { onNavigateTo(Page.Transfer) },
        userId = currentUser?.id.orEmpty(),
      )

      // 4. Skor Kesehatan Finansial & Rasio Tabungan
      DashboardFinancialHealthCard(
        totalIncome = totalIncome,
        totalExpense = totalExpense,
        thisMonthExpenses = thisMonthExpenses,
        strings = strings,
      )

      // 4b. Analisis Burn Rate & Proyeksi Arus Kas
      DashboardBurnRateCard(
        thisMonthExpenses = thisMonthExpenses,
        netBalance = netBalance.toLong() + totalInitial,
      )

      // 5. Donut Chart - Pemasukan vs Pengeluaran
    DashboardFlowDonutChart(
      totalIncome = totalIncome,
      totalExpense = totalExpense,
      strings = strings,
    )

    // 5. Line Chart - Tren Bulanan (Reaktif & Beranimasi)
    DashboardTrendLineChart(
      incomeEntries = incomeEntries,
      expenseEntries = expenseEntries,
      currentLang = currentLang,
      strings = strings,
    )

    // 6. Transaksi Terakhir (Recent Transactions)
    DashboardRecentTransactions(
      incomeEntries = incomeEntries,
      expenseEntries = expenseEntries,
      transferEntries = transferEntries,
      strings = strings,
      onNavigateTo = onNavigateTo,
    )

    // 7. Bar Chart Horizontal - Top 5 Kategori Pengeluaran
    DashboardTopExpenseBars(
      thisMonthExpenses = thisMonthExpenses,
      allExpenseEntries = expenseEntries,
      strings = strings,
    )

    // 8. Anggaran per Kategori (Category Budgeting)
    DashboardCategoryBudgets(
      thisMonthExpenses = thisMonthExpenses,
      allExpenseEntries = expenseEntries,
      strings = strings,
    )

    // 9. Distribusi Metode Pembayaran / Dompet
    DashboardPaymentMethods(
      expenseEntries = expenseEntries,
      strings = strings,
    )

    // 9. Pengingat Tagihan & Jatuh Tempo
    DashboardUpcomingBills(
      loanEntries = loanEntries,
      dreamEntries = dreamEntries,
      strings = strings,
      onNavigateTo = onNavigateTo,
    )

    // 10. Progres Target Impian
    DashboardGoalCards(
      dreamEntries = dreamEntries,
      strings = strings,
      onNavigateTo = onNavigateTo,
    )

    // 11. Ringkasan Hutang & Pinjaman
    DashboardLoanSummary(
      loanEntries = loanEntries,
      strings = strings,
      onNavigateTo = onNavigateTo,
    )

    Spacer(modifier = Modifier.height(16.dp))
  }
}
}
