package com.solvix.tabungan.pages.dashboard

import androidx.compose.ui.graphics.Color
import com.solvix.tabungan.AccountCategory
import com.solvix.tabungan.AccountIconManager
import com.solvix.tabungan.MoneyEntry

enum class BankWalletBrand {
  SEABANK,
  CRYPTO,
  SAHAM,
  BCA,
  MANDIRI,
  BRI,
  BNI,
  BSI,
  JAGO,
  JENIUS,
  CIMB,
  PERMATA,
  GOPAY,
  OVO,
  DANA,
  SHOPEEPAY,
  LINKAJA,
  CASH,
  OTHER,
}

data class AccountAssetItem(
  val name: String,
  val balance: Long,
  val brand: BankWalletBrand,
  val brandColor: Color,
)

fun detectBrand(name: String): BankWalletBrand {
  val lower = name.lowercase().trim()
  return when {
    "seabank" in lower || "sea bank" in lower -> BankWalletBrand.SEABANK
    "crypto" in lower || "kripto" in lower || "btc" in lower || "bitcoin" in lower ||
      "eth" in lower || "binance" in lower || "indodax" in lower || "tokocrypto" in lower ||
      "pintu" in lower || "reku" in lower -> BankWalletBrand.CRYPTO
    "saham" in lower || "stock" in lower || "idx" in lower || "bibit" in lower ||
      "ajaib" in lower || "bareksa" in lower || "pluang" in lower || "ipot" in lower ||
      "stockbit" in lower || "reksadana" in lower || "reksa dana" in lower -> BankWalletBrand.SAHAM
    "gopay" in lower || "go-pay" in lower -> BankWalletBrand.GOPAY
    "ovo" in lower -> BankWalletBrand.OVO
    "dana" in lower -> BankWalletBrand.DANA
    "shopee" in lower || "spay" in lower -> BankWalletBrand.SHOPEEPAY
    "linkaja" in lower || "link aja" in lower -> BankWalletBrand.LINKAJA
    "bca" in lower -> BankWalletBrand.BCA
    "mandiri" in lower -> BankWalletBrand.MANDIRI
    "bri" in lower -> BankWalletBrand.BRI
    "bni" in lower -> BankWalletBrand.BNI
    "bsi" in lower -> BankWalletBrand.BSI
    "jago" in lower -> BankWalletBrand.JAGO
    "jenius" in lower -> BankWalletBrand.JENIUS
    "cimb" in lower || "niaga" in lower -> BankWalletBrand.CIMB
    "permata" in lower -> BankWalletBrand.PERMATA
    "cash" in lower || "tunai" in lower || "dompet" in lower || "kas" in lower -> BankWalletBrand.CASH
    else -> BankWalletBrand.OTHER
  }
}

fun getBrandColor(brand: BankWalletBrand): Color {
  return when (brand) {
    BankWalletBrand.SEABANK -> Color(0xFF0077CC)
    BankWalletBrand.CRYPTO -> Color(0xFFF7931A)
    BankWalletBrand.SAHAM -> Color(0xFF10B981)
    BankWalletBrand.BCA -> Color(0xFF005EAA)
    BankWalletBrand.MANDIRI -> Color(0xFF003D79)
    BankWalletBrand.BRI -> Color(0xFF00529C)
    BankWalletBrand.BNI -> Color(0xFF006666)
    BankWalletBrand.BSI -> Color(0xFF00A39D)
    BankWalletBrand.JAGO -> Color(0xFFFF9900)
    BankWalletBrand.JENIUS -> Color(0xFF00A4E4)
    BankWalletBrand.CIMB -> Color(0xFFED1B2F)
    BankWalletBrand.PERMATA -> Color(0xFF007833)
    BankWalletBrand.GOPAY -> Color(0xFF00AED6)
    BankWalletBrand.OVO -> Color(0xFF4C3494)
    BankWalletBrand.DANA -> Color(0xFF118EEA)
    BankWalletBrand.SHOPEEPAY -> Color(0xFFEE4D2D)
    BankWalletBrand.LINKAJA -> Color(0xFFED1C24)
    BankWalletBrand.CASH -> Color(0xFF2E7D32)
    BankWalletBrand.OTHER -> Color(0xFF475569)
  }
}

fun calculateAccountAssets(
  incomeEntries: List<MoneyEntry>,
  expenseEntries: List<MoneyEntry>,
  transferEntries: List<MoneyEntry>,
  targetNetBalance: Long? = null,
  initialBalances: Map<String, Long> = emptyMap(),
): List<AccountAssetItem> {
  val totalIncome = incomeEntries.sumOf { it.amount.toLong() }
  val totalExpense = expenseEntries.sumOf { it.amount.toLong() }
  val totalInitial = initialBalances.values.sum()
  val net = targetNetBalance ?: (totalInitial + totalIncome - totalExpense)

  // Jika net balance tidak positif (0 atau minus), tidak ada aset bersih positif yang ditampilkan
  if (net <= 0L) {
    return emptyList()
  }

  val balances = mutableMapOf<String, Long>()

  fun normalizeAccount(raw: String): String {
    val trimmed = raw.trim()
    val lower = trimmed.lowercase()
    return when {
      trimmed.isBlank() || trimmed == "-" || lower == "lainnya" || lower == "other" -> "Lainnya"
      lower == "e-wallet" || lower == "ewallet" -> "E-Wallet"
      "seabank" in lower || "sea bank" in lower -> "SeaBank"
      "bca" in lower -> "Bank BCA"
      "mandiri" in lower -> "Bank Mandiri"
      "bri" in lower -> "Bank BRI"
      "bni" in lower -> "Bank BNI"
      "bsi" in lower -> "Bank BSI"
      "jago" in lower -> "Bank Jago"
      "jenius" in lower -> "Jenius"
      "cimb" in lower || "niaga" in lower -> "CIMB Niaga"
      "permata" in lower -> "Bank Permata"
      "dana" in lower -> "DANA"
      "ovo" in lower -> "OVO"
      "gopay" in lower || "go-pay" in lower || "gojek" in lower -> "GoPay"
      "shopee" in lower || "spay" in lower -> "ShopeePay"
      "linkaja" in lower || "link aja" in lower -> "LinkAja"
      "saham" in lower || "stock" in lower || "reksadana" in lower || "investasi" in lower -> "Investasi / Saham"
      "kripto" in lower || "crypto" in lower || "bitcoin" in lower || "btc" in lower -> "Aset Kripto (Crypto)"
      "cash" in lower || "tunai" in lower || "dompet" in lower || "kas" in lower -> "Uang Tunai (Cash)"
      else -> trimmed
    }
  }

  // Muat saldo awal per akun
  initialBalances.forEach { (rawAcc, amount) ->
    val acc = normalizeAccount(rawAcc)
    balances[acc] = (balances[acc] ?: 0L) + amount
  }

  incomeEntries.forEach { entry ->
    val raw = entry.channelOrBank.trim().ifBlank { entry.sourceOrMethod.trim() }
    val acc = normalizeAccount(raw)
    balances[acc] = (balances[acc] ?: 0L) + entry.amount
  }

  expenseEntries.forEach { entry ->
    val raw = entry.channelOrBank.trim().ifBlank { entry.sourceOrMethod.trim() }
    val acc = normalizeAccount(raw)
    balances[acc] = (balances[acc] ?: 0L) - entry.amount
  }

  transferEntries.forEach { entry ->
    val fromAcc = normalizeAccount(entry.sourceOrMethod)
    val toAcc = normalizeAccount(entry.channelOrBank)
    balances[fromAcc] = (balances[fromAcc] ?: 0L) - entry.amount
    balances[toAcc] = (balances[toAcc] ?: 0L) + entry.amount
  }

  // 1. Bangun daftar akun yang memiliki saldo positif nyata
  val result = balances
    .filter {
      it.key.isNotBlank() &&
      !it.key.equals("Lainnya", ignoreCase = true) &&
      !it.key.equals("Other", ignoreCase = true) &&
      it.value > 0L
    }
    .map { (name, bal) ->
      val brand = detectBrand(name)
      val researched = AccountIconManager.researchAccount(name)
      val color = if (researched.category != AccountCategory.OTHER) researched.brandColor else getBrandColor(brand)
      AccountAssetItem(
        name = name,
        balance = bal,
        brand = brand,
        brandColor = color,
      )
    }
    .toMutableList()

  // 2. Jika belum ada akun positif namun net total > 0 (misal transaksi belum ditentukan akunnya)
  if (result.isEmpty() && net > 0L) {
    // Cari akun default dari initial balance yang diatur user atau fallback ke Uang Tunai
    val fallbackName = initialBalances.keys.firstOrNull { it.isNotBlank() }
      ?: incomeEntries.firstOrNull { it.channelOrBank.isNotBlank() }?.channelOrBank
      ?: "Uang Tunai (Cash)"
    val brand = detectBrand(fallbackName)
    val researched = AccountIconManager.researchAccount(fallbackName)
    val color = if (researched.category != AccountCategory.OTHER) researched.brandColor else getBrandColor(brand)
    result.add(
      AccountAssetItem(
        name = fallbackName,
        balance = net,
        brand = brand,
        brandColor = color,
      )
    )
  }

  // 3. Rekonsiliasi ketat agar total nominal aset PERSIS sama dengan net balance
  // Jika ada selisih, sesuaikan ke akun terbesar yang sudah ada (tanpa menambah akun random baru)
  val currentSum = result.sumOf { it.balance }
  val discrepancy = net - currentSum
  if (discrepancy != 0L && result.isNotEmpty()) {
    val maxIdx = result.indices.maxByOrNull { result[it].balance } ?: 0
    val adjBal = result[maxIdx].balance + discrepancy
    if (adjBal > 0L) {
      result[maxIdx] = result[maxIdx].copy(balance = adjBal)
    }
  }

  return result
    .filter { it.balance > 0L }
    .sortedByDescending { it.balance }
}
