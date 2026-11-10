package com.solvix.tabungan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Klasifikasi kategori akun / dompet keuangan
 */
enum class AccountCategory(val displayName: String, val badgeIcon: String) {
  BANK("Bank", "🏦"),
  EWALLET("E-Wallet", "📱"),
  CRYPTO("Crypto", "🪙"),
  INVESTMENT("Investasi", "📈"),
  CASH("Tunai", "💵"),
  OTHER("Lainnya", "💳"),
}

/**
 * Data hasil riset otomatis identitas visual & logo akun
 */
data class ResearchedAccount(
  val rawQuery: String,
  val cleanName: String,
  val category: AccountCategory,
  val brandColor: Color,
  val localDrawableRes: Int? = null,
  val remoteIconUrl: String? = null,
  val ticker: String? = null,
  val domain: String? = null,
)

/**
 * Manager untuk riset otomatis icon/logo Bank, E-Wallet, dan Crypto.
 * Menghilangkan kebutuhan untuk menambahkan file manual ke drawable/img.
 * Menyediakan in-memory cache dan local persistent disk cache.
 */
object AccountIconManager {
  private val memoryCache = object : LruCache<String, Bitmap>(80) {
    override fun sizeOf(key: String, value: Bitmap): Int {
      return (value.byteCount / 1024).coerceAtLeast(1)
    }
  }

  // Registry nama resmi dan domain lembaga keuangan / ewallet / crypto
  private val BANK_DOMAINS = mapOf(
    "bca" to "bca.co.id",
    "mandiri" to "bankmandiri.co.id",
    "bri" to "bri.co.id",
    "bni" to "bni.co.id",
    "bsi" to "bankbsi.co.id",
    "cimb" to "cimbniaga.co.id",
    "niaga" to "cimbniaga.co.id",
    "permata" to "permatabank.com",
    "jago" to "jago.com",
    "seabank" to "seabank.co.id",
    "jenius" to "jenius.com",
    "btpn" to "btpn.com",
    "neo" to "bankneocommerce.co.id",
    "bnc" to "bankneocommerce.co.id",
    "aladin" to "aladinbank.id",
    "superbank" to "superbank.id",
    "krom" to "krom.id",
    "allo" to "allobank.com",
    "line bank" to "linebank.co.id",
    "linebank" to "linebank.co.id",
    "blu" to "blubybcadigital.id",
    "danamon" to "danamon.co.id",
    "mega" to "bankmega.com",
    "btn" to "btn.co.id",
    "panin" to "panin.co.id",
    "muamalat" to "bankmuamalat.co.id",
    "ocbc" to "ocbc.id",
    "nisp" to "ocbc.id",
    "maybank" to "maybank.co.id",
    "sinarmas" to "banksinarmas.com",
    "bjb" to "bankbjb.co.id",
    "jateng" to "bankjateng.co.id",
    "jatim" to "bankjatim.co.id",
    "dki" to "bankdki.co.id",
    "sumut" to "banksumut.co.id",
    "nagari" to "banknagari.co.id",
    "kalbar" to "bankkalbar.co.id",
    "kalsel" to "bankkalsel.co.id",
    "sulselbar" to "banksulselbar.co.id",
    "papua" to "bankpapua.co.id",
    "bengkulu" to "bankbengkulu.co.id",
    "aceh" to "bankaceh.co.id",
    "riau" to "bankriaukepri.co.id",
    "jambi" to "bankjambi.co.id",
    "sulutgo" to "banksulutgo.co.id",
    "kaltimtara" to "bankkaltimtara.co.id",
    "maluku" to "bankmaluku.co.id",
    "ntb" to "bankntb.co.id",
    "ntt" to "bpdntt.co.id",
    "bali" to "bpdbali.co.id",
    "lampung" to "banklampung.co.id",
    "commonwealth" to "commbank.co.id",
    "dbs" to "dbs.com",
    "digibank" to "dbs.com",
    "uob" to "uob.co.id",
    "tmrw" to "uob.co.id",
    "hsbc" to "hsbc.co.id",
    "citibank" to "citibank.co.id",
    "citi" to "citibank.co.id",
    "standard chartered" to "sc.com",
    "stanchart" to "sc.com",
    "chase" to "chase.com",
    "wells fargo" to "wellsfargo.com",
    "bank of america" to "bankofamerica.com",
    "bofa" to "bankofamerica.com",
    "barclays" to "barclays.co.uk",
  )

  private val EWALLET_DOMAINS = mapOf(
    "gopay" to "gopay.co.id",
    "gojek" to "gopay.co.id",
    "go-pay" to "gopay.co.id",
    "ovo" to "ovo.id",
    "dana" to "dana.id",
    "shopeepay" to "shopee.co.id",
    "spay" to "shopee.co.id",
    "shopee" to "shopee.co.id",
    "linkaja" to "linkaja.id",
    "link aja" to "linkaja.id",
    "doku" to "doku.promo",
    "i.saku" to "isaku.co.id",
    "isaku" to "isaku.co.id",
    "astrapay" to "astrapay.com",
    "sakuku" to "bca.co.id",
    "flip" to "flip.id",
    "wise" to "wise.com",
    "transferwise" to "wise.com",
    "paypal" to "paypal.com",
    "revolut" to "revolut.com",
    "skrill" to "skrill.com",
    "payoneer" to "payoneer.com",
    "venmo" to "venmo.com",
    "cash app" to "cash.app",
    "cashapp" to "cash.app",
  )

  private val CRYPTO_EXCHANGES = mapOf(
    "indodax" to "indodax.com",
    "tokocrypto" to "tokocrypto.com",
    "pintu" to "pintu.co.id",
    "reku" to "reku.id",
    "binance" to "binance.com",
    "bybit" to "bybit.com",
    "okx" to "okx.com",
    "coinbase" to "coinbase.com",
    "kucoin" to "kucoin.com",
    "bitget" to "bitget.com",
    "kraken" to "kraken.com",
    "upbit" to "upbit.com",
    "luno" to "luno.com",
    "metamask" to "metamask.io",
    "trust wallet" to "trustwallet.com",
    "phantom" to "phantom.app",
  )

  private val CRYPTO_TICKERS = mapOf(
    "btc" to "btc",
    "bitcoin" to "btc",
    "eth" to "eth",
    "ethereum" to "eth",
    "sol" to "sol",
    "solana" to "sol",
    "usdt" to "usdt",
    "tether" to "usdt",
    "usdc" to "usdc",
    "bnb" to "bnb",
    "xrp" to "xrp",
    "ripple" to "xrp",
    "ada" to "ada",
    "cardano" to "ada",
    "doge" to "doge",
    "dogecoin" to "doge",
    "trx" to "trx",
    "tron" to "trx",
    "avax" to "avax",
    "avalanche" to "avax",
    "shib" to "shib",
    "shiba" to "shib",
    "ton" to "ton",
    "toncoin" to "ton",
    "sui" to "sui",
    "apt" to "apt",
    "aptos" to "apt",
    "near" to "near",
    "link" to "link",
    "chainlink" to "link",
    "pepe" to "pepe",
    "matic" to "matic",
    "pol" to "matic",
    "polygon" to "matic",
    "dot" to "dot",
    "polkadot" to "dot",
    "ltc" to "ltc",
    "litecoin" to "ltc",
    "xlm" to "xlm",
    "stellar" to "xlm",
    "xmr" to "xmr",
    "monero" to "xmr",
    "atom" to "atom",
    "cosmos" to "atom",
    "arb" to "arb",
    "arbitrum" to "arb",
    "op" to "op",
    "optimism" to "op",
    "ftm" to "ftm",
    "fantom" to "ftm",
    "kas" to "kas",
    "kaspa" to "kas",
    "uni" to "uni",
    "uniswap" to "uni",
  )

  private val INVESTMENT_DOMAINS = mapOf(
    "bibit" to "bibit.id",
    "ajaib" to "ajaib.co.id",
    "bareksa" to "bareksa.com",
    "stockbit" to "stockbit.com",
    "pluang" to "pluang.com",
    "ipot" to "indopremier.com",
    "mirae" to "miraeasset.co.id",
  )

  /**
   * Menganalisis dan meriset identitas visual sebuah akun perbankan, dompet digital, atau kripto.
   */
  fun researchAccount(raw: String): ResearchedAccount {
    val query = raw.trim()
    if (query.isBlank()) {
      return ResearchedAccount(
        rawQuery = "",
        cleanName = "Lainnya",
        category = AccountCategory.OTHER,
        brandColor = Color(0xFF64748B),
      )
    }

    val lower = query.lowercase()
    val words = lower.split(Regex("[^a-z0-9]")).filter { it.isNotBlank() }

    // 1. Cek drawable lokal (jika sudah tersedia di APK)
    val localRes: Int? = when {
      "seabank" in lower || "sea bank" in lower -> R.drawable.seabank
      "bca" in lower -> R.drawable.bca
      "bni" in lower -> R.drawable.bni
      "bsi" in lower -> R.drawable.bsi
      "dana" in lower -> R.drawable.dana
      "gopay" in lower || "go-pay" in lower || "gojek" in lower -> R.drawable.gopay
      "mandiri" in lower -> R.drawable.mandiri
      "ovo" in lower -> R.drawable.ovo
      "shopeepay" in lower || "spay" in lower || "shopee" in lower -> R.drawable.shopeepay
      "sinarmas" in lower -> R.drawable.sinarmas
      else -> null
    }

    // 2. Riset Kategori Kripto (Koin / Token / Exchange)
    for ((tokenKey, ticker) in CRYPTO_TICKERS) {
      val isMatch = if (tokenKey.length <= 4) {
        lower == tokenKey || words.contains(tokenKey)
      } else {
        lower == tokenKey || lower.contains(tokenKey)
      }
      if (isMatch) {
        val displayName = formatCryptoName(tokenKey, ticker, query)
        val iconUrl = "https://assets.coincap.io/assets/icons/${ticker}@2x.png"
        return ResearchedAccount(
          rawQuery = query,
          cleanName = displayName,
          category = AccountCategory.CRYPTO,
          brandColor = Color(0xFFF7931A),
          localDrawableRes = localRes,
          remoteIconUrl = iconUrl,
          ticker = ticker.uppercase(),
        )
      }
    }

    for ((exchangeKey, domain) in CRYPTO_EXCHANGES) {
      if (lower.contains(exchangeKey)) {
        val displayName = query.capitalizeWords()
        val iconUrl = "https://www.google.com/s2/favicons?domain=$domain&sz=128"
        return ResearchedAccount(
          rawQuery = query,
          cleanName = displayName,
          category = AccountCategory.CRYPTO,
          brandColor = Color(0xFFF0B90B),
          localDrawableRes = localRes,
          remoteIconUrl = iconUrl,
          domain = domain,
        )
      }
    }

    if ("crypto" in lower || "kripto" in lower || "koin" in lower || "token" in lower) {
      val clean = query.replace("crypto", "", ignoreCase = true)
        .replace("kripto", "", ignoreCase = true)
        .trim()
      val tickerCandidate = if (clean.length in 2..6) clean.lowercase() else "btc"
      return ResearchedAccount(
        rawQuery = query,
        cleanName = query.capitalizeWords(),
        category = AccountCategory.CRYPTO,
        brandColor = Color(0xFFF7931A),
        localDrawableRes = localRes,
        remoteIconUrl = "https://assets.coincap.io/assets/icons/${tickerCandidate}@2x.png",
        ticker = tickerCandidate.uppercase(),
      )
    }

    // 3. Riset Kategori Bank
    for ((bankKey, domain) in BANK_DOMAINS) {
      val isMatch = if (bankKey.length <= 4) {
        lower == bankKey || words.contains(bankKey) || lower.contains("bank $bankKey") || lower.contains("bank$bankKey")
      } else {
        lower == bankKey || lower.contains(bankKey)
      }
      if (isMatch) {
        val displayName = formatBankName(bankKey, query)
        val brandColor = getBankColor(bankKey)
        val iconUrl = "https://www.google.com/s2/favicons?domain=$domain&sz=128"
        return ResearchedAccount(
          rawQuery = query,
          cleanName = displayName,
          category = AccountCategory.BANK,
          brandColor = brandColor,
          localDrawableRes = localRes,
          remoteIconUrl = iconUrl,
          domain = domain,
        )
      }
    }

    if ("bank" in lower) {
      val clean = query.replace("bank", "", ignoreCase = true)
        .replace("pt", "", ignoreCase = true)
        .replace("tbk", "", ignoreCase = true)
        .trim()
        .replace(" ", "")
        .lowercase()
      val domain = if (clean.isNotBlank()) "bank$clean.co.id" else "bi.go.id"
      val iconUrl = "https://www.google.com/s2/favicons?domain=$domain&sz=128"
      return ResearchedAccount(
        rawQuery = query,
        cleanName = query.capitalizeWords(),
        category = AccountCategory.BANK,
        brandColor = Color(0xFF00529C),
        localDrawableRes = localRes,
        remoteIconUrl = iconUrl,
        domain = domain,
      )
    }

    // 4. Riset Kategori E-Wallet
    for ((walletKey, domain) in EWALLET_DOMAINS) {
      if (lower.contains(walletKey)) {
        val displayName = formatWalletName(walletKey, query)
        val brandColor = getWalletColor(walletKey)
        val iconUrl = "https://www.google.com/s2/favicons?domain=$domain&sz=128"
        return ResearchedAccount(
          rawQuery = query,
          cleanName = displayName,
          category = AccountCategory.EWALLET,
          brandColor = brandColor,
          localDrawableRes = localRes,
          remoteIconUrl = iconUrl,
          domain = domain,
        )
      }
    }

    if ("wallet" in lower || "dompet" in lower || "pay" in lower) {
      val clean = query.replace("wallet", "", ignoreCase = true)
        .replace("dompet", "", ignoreCase = true)
        .trim()
        .replace(" ", "")
        .lowercase()
      val domain = if (clean.isNotBlank()) "$clean.com" else "gopay.co.id"
      val iconUrl = "https://www.google.com/s2/favicons?domain=$domain&sz=128"
      return ResearchedAccount(
        rawQuery = query,
        cleanName = query.capitalizeWords(),
        category = AccountCategory.EWALLET,
        brandColor = Color(0xFF00AED6),
        localDrawableRes = localRes,
        remoteIconUrl = iconUrl,
        domain = domain,
      )
    }

    // 5. Riset Kategori Investasi / Sekuritas
    for ((investKey, domain) in INVESTMENT_DOMAINS) {
      if (lower.contains(investKey)) {
        val iconUrl = "https://www.google.com/s2/favicons?domain=$domain&sz=128"
        return ResearchedAccount(
          rawQuery = query,
          cleanName = query.capitalizeWords(),
          category = AccountCategory.INVESTMENT,
          brandColor = Color(0xFF10B981),
          localDrawableRes = localRes,
          remoteIconUrl = iconUrl,
          domain = domain,
        )
      }
    }

    if ("saham" in lower || "reksadana" in lower || "investasi" in lower || "stock" in lower) {
      return ResearchedAccount(
        rawQuery = query,
        cleanName = query.capitalizeWords(),
        category = AccountCategory.INVESTMENT,
        brandColor = Color(0xFF10B981),
        localDrawableRes = localRes,
        remoteIconUrl = "https://www.google.com/s2/favicons?domain=idx.co.id&sz=128",
        domain = "idx.co.id",
      )
    }

    // 6. Uang Tunai / Cash
    if ("cash" in lower || "tunai" in lower || "kas" in lower) {
      return ResearchedAccount(
        rawQuery = query,
        cleanName = "Uang Tunai (Cash)",
        category = AccountCategory.CASH,
        brandColor = Color(0xFF2E7D32),
        localDrawableRes = localRes,
      )
    }

    // 7. General Dynamic Search untuk brand lainnya
    val candidate = query.replace(" ", "").lowercase()
    val dynamicDomain = "$candidate.com"
    return ResearchedAccount(
      rawQuery = query,
      cleanName = query.capitalizeWords(),
      category = AccountCategory.OTHER,
      brandColor = Color(0xFF475569),
      localDrawableRes = localRes,
      remoteIconUrl = "https://www.google.com/s2/favicons?domain=$dynamicDomain&sz=128",
      domain = dynamicDomain,
    )
  }

  /**
   * Mengambil gambar icon/logo dalam bentuk Bitmap (dengan memori cache & disk cache).
   */
  suspend fun loadIconBitmap(context: Context, accountName: String): Bitmap? = withContext(Dispatchers.IO) {
    val trimmed = accountName.trim()
    if (trimmed.isBlank()) return@withContext null

    val researched = researchAccount(trimmed)
    val cacheKey = researched.remoteIconUrl ?: researched.cleanName
    if (cacheKey.isBlank()) return@withContext null

    // 1. Cek memori cache
    memoryCache.get(cacheKey)?.let { return@withContext it }

    // 2. Cek disk cache
    val diskDir = File(context.cacheDir, "account_icons").apply {
      if (!exists()) mkdirs()
    }
    val diskFileName = md5(cacheKey) + ".png"
    val diskFile = File(diskDir, diskFileName)
    if (diskFile.exists() && diskFile.length() > 64) {
      try {
        val bitmap = BitmapFactory.decodeFile(diskFile.absolutePath)
        if (bitmap != null) {
          memoryCache.put(cacheKey, bitmap)
          return@withContext bitmap
        }
      } catch (_: Exception) {}
    }

    // 3. Download via HTTP jika remoteIconUrl tersedia
    val remoteUrl = researched.remoteIconUrl ?: return@withContext null
    try {
      val url = URL(remoteUrl)
      val connection = (url.openConnection() as HttpURLConnection).apply {
        connectTimeout = 3200
        readTimeout = 4000
        instanceFollowRedirects = true
        setRequestProperty("User-Agent", "Mozilla/5.0 TabunganApp/1.0")
      }
      if (connection.responseCode in 200..299) {
        val bytes = connection.inputStream.use { it.readBytes() }
        if (bytes.size > 80) {
          val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
          if (bitmap != null) {
            try {
              FileOutputStream(diskFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, fos)
              }
            } catch (_: Exception) {}
            memoryCache.put(cacheKey, bitmap)
            return@withContext bitmap
          }
        }
      }
    } catch (_: Exception) {}

    // Fallback sekunder untuk crypto jika coincap CDN tidak tersedia
    if (researched.category == AccountCategory.CRYPTO && researched.ticker != null) {
      try {
        val fallbackUrl = "https://cdn.jsdelivr.net/gh/spothq/cryptocurrency-icons@master/128/color/${researched.ticker.lowercase()}.png"
        val url = URL(fallbackUrl)
        val connection = (url.openConnection() as HttpURLConnection).apply {
          connectTimeout = 3000
          readTimeout = 3500
          instanceFollowRedirects = true
        }
        if (connection.responseCode in 200..299) {
          val bytes = connection.inputStream.use { it.readBytes() }
          if (bytes.size > 80) {
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bitmap != null) {
              try {
                FileOutputStream(diskFile).use { fos ->
                  bitmap.compress(Bitmap.CompressFormat.PNG, 95, fos)
                }
              } catch (_: Exception) {}
              memoryCache.put(cacheKey, bitmap)
              return@withContext bitmap
            }
          }
        }
      } catch (_: Exception) {}
    }

    null
  }

  private fun md5(input: String): String {
    val md = MessageDigest.getInstance("MD5")
    return java.math.BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
  }

  private fun formatCryptoName(tokenKey: String, ticker: String, raw: String): String {
    return when (ticker.lowercase()) {
      "btc" -> "Bitcoin (BTC)"
      "eth" -> "Ethereum (ETH)"
      "sol" -> "Solana (SOL)"
      "usdt" -> "Tether (USDT)"
      "usdc" -> "USD Coin (USDC)"
      "bnb" -> "Binance Coin (BNB)"
      "xrp" -> "XRP / Ripple"
      "ada" -> "Cardano (ADA)"
      "doge" -> "Dogecoin (DOGE)"
      "trx" -> "Tron (TRX)"
      "avax" -> "Avalanche (AVAX)"
      "shib" -> "Shiba Inu (SHIB)"
      "ton" -> "Toncoin (TON)"
      "sui" -> "Sui (SUI)"
      "near" -> "NEAR Protocol"
      "link" -> "Chainlink (LINK)"
      "matic" -> "Polygon (POL/MATIC)"
      "dot" -> "Polkadot (DOT)"
      else -> raw.capitalizeWords()
    }
  }

  private fun formatBankName(bankKey: String, raw: String): String {
    return when (bankKey) {
      "bca" -> "Bank BCA"
      "mandiri" -> "Bank Mandiri"
      "bri" -> "Bank BRI"
      "bni" -> "Bank BNI"
      "bsi" -> "Bank BSI"
      "cimb", "niaga" -> "CIMB Niaga"
      "permata" -> "Bank Permata"
      "jago" -> "Bank Jago"
      "seabank" -> "SeaBank"
      "jenius", "btpn" -> "Jenius (BTPN)"
      "neo", "bnc" -> "Bank Neo Commerce (BNC)"
      "aladin" -> "Bank Aladin Syariah"
      "superbank" -> "Superbank"
      "krom" -> "Krom Bank"
      "allo" -> "Allo Bank"
      "line bank", "linebank" -> "LINE Bank"
      "blu" -> "blu by BCA Digital"
      "danamon" -> "Bank Danamon"
      "mega" -> "Bank Mega"
      "btn" -> "Bank BTN"
      "panin" -> "Bank Panin"
      "muamalat" -> "Bank Muamalat"
      "ocbc", "nisp" -> "OCBC"
      "maybank" -> "Maybank"
      "sinarmas" -> "Bank Sinarmas"
      "bjb" -> "Bank BJB"
      "jateng" -> "Bank Jateng"
      "jatim" -> "Bank Jatim"
      else -> raw.capitalizeWords()
    }
  }

  private fun formatWalletName(walletKey: String, raw: String): String {
    return when (walletKey) {
      "gopay", "gojek", "go-pay" -> "GoPay"
      "ovo" -> "OVO"
      "dana" -> "DANA"
      "shopeepay", "spay", "shopee" -> "ShopeePay"
      "linkaja", "link aja" -> "LinkAja"
      "doku" -> "DOKU"
      "i.saku", "isaku" -> "i.saku"
      "astrapay" -> "AstraPay"
      "flip" -> "Flip"
      "wise", "transferwise" -> "Wise"
      "paypal" -> "PayPal"
      "revolut" -> "Revolut"
      else -> raw.capitalizeWords()
    }
  }

  private fun getBankColor(bankKey: String): Color {
    return when (bankKey) {
      "bca" -> Color(0xFF005EAA)
      "mandiri" -> Color(0xFF003D79)
      "bri" -> Color(0xFF00529C)
      "bni" -> Color(0xFF006666)
      "bsi" -> Color(0xFF00A39D)
      "cimb", "niaga" -> Color(0xFFED1B2F)
      "permata" -> Color(0xFF007833)
      "jago" -> Color(0xFFFF9900)
      "seabank" -> Color(0xFF0077CC)
      "jenius", "btpn" -> Color(0xFF00A4E4)
      "neo", "bnc" -> Color(0xFFFFC000)
      "aladin" -> Color(0xFF00B4D8)
      "superbank" -> Color(0xFF00E5FF)
      "krom" -> Color(0xFF7928CA)
      "allo" -> Color(0xFF7F56D9)
      "line bank", "linebank" -> Color(0xFF06C755)
      "blu" -> Color(0xFF0066FF)
      "danamon" -> Color(0xFFFF6600)
      "mega" -> Color(0xFFFF8800)
      "btn" -> Color(0xFF003B70)
      "panin" -> Color(0xFF005596)
      "muamalat" -> Color(0xFF5B2C6F)
      "ocbc", "nisp" -> Color(0xFFED1C24)
      "maybank" -> Color(0xFFFFB800)
      "sinarmas" -> Color(0xFFD32F2F)
      "bjb" -> Color(0xFF005BAA)
      "jateng" -> Color(0xFFC2185B)
      "jatim" -> Color(0xFFD32F2F)
      else -> Color(0xFF00529C)
    }
  }

  private fun getWalletColor(walletKey: String): Color {
    return when (walletKey) {
      "gopay", "gojek", "go-pay" -> Color(0xFF00AED6)
      "ovo" -> Color(0xFF4C3494)
      "dana" -> Color(0xFF118EEA)
      "shopeepay", "spay", "shopee" -> Color(0xFFEE4D2D)
      "linkaja", "link aja" -> Color(0xFFED1C24)
      "doku" -> Color(0xFFE53935)
      "i.saku", "isaku" -> Color(0xFF005BAA)
      "astrapay" -> Color(0xFF003399)
      "flip" -> Color(0xFFFA541C)
      "wise", "transferwise" -> Color(0xFF2ED06E)
      "paypal" -> Color(0xFF003087)
      "revolut" -> Color(0xFF0075EB)
      else -> Color(0xFF00AED6)
    }
  }
}

private fun String.capitalizeWords(): String {
  return split(" ").joinToString(" ") { word ->
    if (word.length <= 4 && word.all { it.isUpperCase() }) word
    else word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
  }
}

