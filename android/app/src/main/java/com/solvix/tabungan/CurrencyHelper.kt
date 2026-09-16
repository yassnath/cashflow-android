package com.solvix.tabungan

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

enum class AppCurrency(
  val code: String,
  val symbol: String,
  val label: String,
  val defaultRateToIdr: Double,
  val decimals: Int,
) {
  IDR("IDR", "Rp", "IDR - Rupiah Indonesia (Rp)", 1.0, 0),
  USD("USD", "$", "USD - US Dollar ($)", 17688.0, 2),
  EUR("EUR", "€", "EUR - Euro (€)", 20408.0, 2),
  GBP("GBP", "£", "GBP - British Pound Sterling (£)", 23600.0, 2),
  SGD("SGD", "S$", "SGD - Singapore Dollar (S$)", 13898.0, 2),
  MYR("MYR", "RM", "MYR - Ringgit Malaysia (RM)", 4330.0, 2),
  JPY("JPY", "¥", "JPY - Japanese Yen (¥)", 114.0, 0),
  AUD("AUD", "A$", "AUD - Australian Dollar (A$)", 11700.0, 2),
  CNY("CNY", "¥", "CNY - Chinese Yuan (¥)", 2630.0, 2),
  KRW("KRW", "₩", "KRW - South Korean Won (₩)", 13.0, 0),
  SAR("SAR", "﷼", "SAR - Saudi Riyal (﷼)", 4716.0, 2),
  THB("THB", "฿", "THB - Thai Baht (฿)", 520.0, 2),
  TWD("TWD", "NT$", "TWD - New Taiwan Dollar (NT$)", 550.0, 0),
  HKD("HKD", "HK$", "HKD - Hong Kong Dollar (HK$)", 2260.0, 2),
  CAD("CAD", "C$", "CAD - Canadian Dollar (C$)", 13100.0, 2),
  CHF("CHF", "CHF", "CHF - Swiss Franc (CHF)", 21500.0, 2),
  AED("AED", "AED", "AED - UAE Dirham (AED)", 4816.0, 2),
  INR("INR", "₹", "INR - Indian Rupee (₹)", 210.0, 2),
  BRL("BRL", "R$", "BRL - Brazilian Real (R$)", 3150.0, 2),
  PHP("PHP", "₱", "PHP - Philippine Peso (₱)", 310.0, 2),
  VND("VND", "₫", "VND - Vietnamese Dong (₫)", 0.72, 0),
}

object CurrencyHelper {
  private const val PREFS_KEY = "app_currency"
  private const val PREFS_LAST_UPDATE = "rates_last_updated"
  private const val PREFS_LAST_MILLIS = "rates_last_millis"

  var currentCurrency by mutableStateOf(AppCurrency.IDR)
  val liveRates = mutableStateMapOf<String, Double>()
  var lastUpdatedText by mutableStateOf<String?>(null)
  var isUpdatingRates by mutableStateOf(false)
  private var lastFetchMillis: Long = 0L

  fun init(context: Context) {
    val prefs = context.getSharedPreferences("tabungan_prefs", Context.MODE_PRIVATE)
    val savedCode = prefs.getString(PREFS_KEY, "IDR") ?: "IDR"
    currentCurrency = AppCurrency.values().find { it.code == savedCode } ?: AppCurrency.IDR
    lastFetchMillis = prefs.getLong(PREFS_LAST_MILLIS, 0L)

    // Muat kurs tersimpan sebelumnya dari cache
    AppCurrency.values().forEach { c ->
      if (c == AppCurrency.IDR) {
        liveRates["IDR"] = 1.0
      } else {
        val cached = prefs.getFloat("rate_${c.code}", 0f)
        if (cached > 0f) {
          liveRates[c.code] = cached.toDouble()
        } else {
          liveRates[c.code] = c.defaultRateToIdr
        }
      }
    }
    lastUpdatedText = prefs.getString(PREFS_LAST_UPDATE, null)

    // Ambil kurs online live terbaru
    fetchOnlineRates(context)
  }

  fun checkAndAutoRefresh(context: Context) {
    val now = System.currentTimeMillis()
    if (now - lastFetchMillis > 15 * 60 * 1000L && !isUpdatingRates) {
      fetchOnlineRates(context)
    }
  }

  fun setCurrency(context: Context, currency: AppCurrency) {
    currentCurrency = currency
    context.getSharedPreferences("tabungan_prefs", Context.MODE_PRIVATE)
      .edit()
      .putString(PREFS_KEY, currency.code)
      .apply()
  }

  fun getRate(currency: AppCurrency = currentCurrency): Double {
    if (currency == AppCurrency.IDR) return 1.0
    return liveRates[currency.code] ?: currency.defaultRateToIdr
  }

  fun fetchOnlineRates(context: Context? = null, onComplete: ((Boolean) -> Unit)? = null) {
    CoroutineScope(Dispatchers.IO).launch {
      isUpdatingRates = true
      var success = false

      // Daftar endpoint online kurs live (primary & backup fallback)
      val endpoints = listOf(
        "https://open.er-api.com/v6/latest/USD",
        "https://api.exchangerate-api.com/v4/latest/USD",
      )

      for (endpoint in endpoints) {
        try {
          val url = URL(endpoint)
          val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 7000
            readTimeout = 7000
            requestMethod = "GET"
            setRequestProperty("User-Agent", "CashFlow-Android/1.0")
          }

          if (conn.responseCode == 200) {
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(text)
            val ratesObj = json.optJSONObject("rates")
            if (ratesObj != null) {
              val idrPerUsd = ratesObj.optDouble("IDR", 17688.0)

              withContext(Dispatchers.Main) {
                AppCurrency.values().forEach { c ->
                  if (c == AppCurrency.IDR) {
                    liveRates["IDR"] = 1.0
                  } else {
                    val rateAgainstUsd = ratesObj.optDouble(c.code, 0.0)
                    if (rateAgainstUsd > 0) {
                      val rateToIdr = idrPerUsd / rateAgainstUsd
                      liveRates[c.code] = rateToIdr
                    }
                  }
                }

                val nowFmt = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(Date())
                lastUpdatedText = nowFmt
                lastFetchMillis = System.currentTimeMillis()

                if (context != null) {
                  val prefs = context.getSharedPreferences("tabungan_prefs", Context.MODE_PRIVATE)
                  val editor = prefs.edit()
                  liveRates.forEach { (k, v) ->
                    editor.putFloat("rate_$k", v.toFloat())
                  }
                  editor.putString(PREFS_LAST_UPDATE, nowFmt)
                  editor.putLong(PREFS_LAST_MILLIS, lastFetchMillis)
                  editor.apply()
                }
              }
              success = true
              break
            }
          }
        } catch (_: Exception) {
          // Coba endpoint fallback berikutnya
        }
      }

      withContext(Dispatchers.Main) {
        isUpdatingRates = false
        onComplete?.invoke(success)
      }
    }
  }

  fun getExchangeRateNote(currency: AppCurrency, strings: AppStrings): String {
    return if (currency == AppCurrency.IDR) {
      strings["currency_base_note"].ifBlank { "Mata uang dasar (Rupiah Indonesia - IDR)" }
    } else {
      val rateValue = getRate(currency).toLong()
      val rateFormatted = format(rateValue, AppCurrency.IDR)
      val liveBadge = strings["currency_live_badge"].ifBlank { "Kurs Real-Time Online" }
      "1 ${currency.code} ≈ $rateFormatted ($liveBadge)"
    }
  }

  fun format(value: Long, currency: AppCurrency = currentCurrency): String {
    val isNegative = value < 0
    val absValue = abs(value)
    val prefix = if (isNegative) "-${currency.symbol} " else "${currency.symbol} "

    if (currency == AppCurrency.IDR) {
      val text = absValue.toString()
      val builder = StringBuilder()
      var count = 0
      for (i in text.length - 1 downTo 0) {
        builder.append(text[i])
        count++
        if (count == 3 && i != 0) {
          builder.append('.')
          count = 0
        }
      }
      return "$prefix${builder.reverse()}"
    }

    val rate = getRate(currency)
    val converted = if (rate > 0) absValue.toDouble() / rate else absValue.toDouble()
    if (currency.decimals == 0) {
      val rounded = converted.roundToLong()
      val text = rounded.toString()
      val builder = StringBuilder()
      var count = 0
      for (i in text.length - 1 downTo 0) {
        builder.append(text[i])
        count++
        if (count == 3 && i != 0) {
          builder.append(',')
          count = 0
        }
      }
      return "$prefix${builder.reverse()}"
    } else {
      val wholePart = converted.toLong()
      val fractionPart = ((converted - wholePart) * 100).roundToLong().coerceIn(0, 99)
      val wholeText = wholePart.toString()
      val builder = StringBuilder()
      var count = 0
      for (i in wholeText.length - 1 downTo 0) {
        builder.append(wholeText[i])
        count++
        if (count == 3 && i != 0) {
          builder.append(',')
          count = 0
        }
      }
      val formattedWhole = builder.reverse().toString()
      val formattedFraction = String.format(Locale.US, "%02d", fractionPart)
      return "$prefix$formattedWhole.$formattedFraction"
    }
  }
}
