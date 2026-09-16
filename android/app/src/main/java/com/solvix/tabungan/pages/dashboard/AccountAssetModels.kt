package com.solvix.tabungan.pages.dashboard

import androidx.compose.ui.graphics.Color
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
): List<AccountAssetItem> {
  val totalIncome = incomeEntries.sumOf { it.amount.toLong() }
  val totalExpense = expenseEntries.sumOf { it.amount.toLong() }
  val net = targetNetBalance ?: (totalIncome - totalExpense)

  // Jika net balance tidak positif (0 atau minus), tidak ada aset bersih positif yang ditampilkan
  if (net <= 0L) {
    return emptyList()
  }

  val balances = mutableMapOf<String, Long>()

  fun normalizeAccount(raw: String): String {
    val trimmed = raw.trim()
    val lower = trimmed.lowercase()
    return when {
      trimmed.isBlank() || trimmed == "-" ||
        lower == "lainnya" || lower == "other" ||
        lower == "e-wallet" || lower == "ewallet" ||
        lower == "transfer" || lower == "transfer bank" ||
        lower == "qris" || lower == "debit" || lower == "kredit" || lower == "credit" ||
        lower == "gaji" || lower == "salary" || lower == "bisnis" || lower == "business" ||
        lower == "freelance" || lower == "bonus" || lower == "tabungan" || lower == "savings" ||
        lower == "investasi" || lower == "investment" ||
        "seabank" in lower || "sea bank" in lower -> "SeaBank"
      lower == "stocks" || lower == "stock" -> "Saham"
      lower == "dompet tunai" || lower == "tunai" || lower == "kas" || lower == "dompet" -> "Cash"
      lower == "go-pay" || lower == "gojek" -> "GoPay"
      lower == "shopee pay" || lower == "spay" -> "ShopeePay"
      lower == "link aja" -> "LinkAja"
      lower == "kripto" || lower == "bitcoin" || lower == "btc" -> "Crypto"
      lower == "bca" -> "BCA"
      lower == "mandiri" -> "Mandiri"
      lower == "bri" -> "BRI"
      lower == "bni" -> "BNI"
      lower == "bsi" -> "BSI"
      lower == "jago" || lower == "bank jago" -> "Jago"
      lower == "jenius" -> "Jenius"
      lower == "cimb" || lower == "cimb niaga" -> "CIMB"
      lower == "permata" || lower == "bank permata" -> "Permata"
      lower == "dana" -> "DANA"
      lower == "ovo" -> "OVO"
      else -> trimmed
    }
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

  // 1. Serap defisit dari akun sekunder ke SeaBank (akun likuiditas utama).
  // Akun sekunder yang bernilai negatif (karena pengeluaran dicatat tanpa saldo awal di akun tsb)
  // hakikatnya diambil dari dana likuiditas utama pengguna di SeaBank.
  val secondaryDeficit = balances
    .filter { it.key != "SeaBank" && it.value < 0L }
    .values
    .sum()

  balances["SeaBank"] = (balances["SeaBank"] ?: 0L) + secondaryDeficit
  // Hapus semua akun sekunder yang bersaldo 0 atau negatif (jangan tampilkan akun kosong/minus)
  balances.entries.removeIf { it.key != "SeaBank" && it.value <= 0L }

  // 2. Jika SeaBank sendiri bersaldo <= 0 namun net > 0 (karena aset tersimpan di Crypto/Saham/dll)
  if ((balances["SeaBank"] ?: 0L) <= 0L) {
    val seaBankDeficit = balances.remove("SeaBank") ?: 0L
    if (seaBankDeficit < 0L && balances.isNotEmpty()) {
      val maxKey = balances.maxByOrNull { it.value }?.key
      if (maxKey != null) {
        balances[maxKey] = (balances[maxKey] ?: 0L) + seaBankDeficit
      }
    }
  }

  // 3. Jika setelah pembersihan belum ada akun tersisa namun net > 0, tempatkan ke SeaBank
  if (balances.isEmpty()) {
    balances["SeaBank"] = net
  }

  // 4. Bangun daftar akun positif
  val result = balances
    .filter {
      it.key.isNotBlank() &&
      !it.key.equals("Lainnya", ignoreCase = true) &&
      !it.key.equals("Other", ignoreCase = true) &&
      it.value > 0L
    }
    .map { (name, bal) ->
      val brand = detectBrand(name)
      AccountAssetItem(
        name = name,
        balance = bal,
        brand = brand,
        brandColor = getBrandColor(brand),
      )
    }
    .toMutableList()

  // 5. Rekonsiliasi ketat agar total nominal aset PERSIS sama dengan net balance
  val currentSum = result.sumOf { it.balance }
  val discrepancy = net - currentSum
  if (discrepancy != 0L) {
    val seaBankIndex = result.indexOfFirst { it.brand == BankWalletBrand.SEABANK }
    if (seaBankIndex >= 0) {
      val newBal = result[seaBankIndex].balance + discrepancy
      if (newBal > 0L) {
        result[seaBankIndex] = result[seaBankIndex].copy(balance = newBal)
      } else {
        result.removeAt(seaBankIndex)
        if (result.isNotEmpty()) {
          val maxIdx = result.indices.maxByOrNull { result[it].balance } ?: 0
          val adjBal = result[maxIdx].balance + (net - result.sumOf { it.balance })
          if (adjBal > 0L) {
            result[maxIdx] = result[maxIdx].copy(balance = adjBal)
          }
        } else {
          result.add(
            AccountAssetItem(
              name = "SeaBank",
              balance = net,
              brand = BankWalletBrand.SEABANK,
              brandColor = getBrandColor(BankWalletBrand.SEABANK),
            )
          )
        }
      }
    } else if (discrepancy > 0L) {
      result.add(
        AccountAssetItem(
          name = "SeaBank",
          balance = discrepancy,
          brand = BankWalletBrand.SEABANK,
          brandColor = getBrandColor(BankWalletBrand.SEABANK),
        )
      )
    } else {
      if (result.isNotEmpty()) {
        val maxIdx = result.indices.maxByOrNull { result[it].balance } ?: 0
        val adjBal = result[maxIdx].balance + discrepancy
        if (adjBal > 0L) {
          result[maxIdx] = result[maxIdx].copy(balance = adjBal)
        }
      }
    }
  }

  return result
    .filter { it.balance > 0L }
    .sortedByDescending { it.balance }
}
