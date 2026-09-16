package com.solvix.tabungan.pages.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppCard
import com.solvix.tabungan.AppCurrency
import com.solvix.tabungan.AppDropdown
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.CurrencyHelper
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.LocalStrings

@Composable
fun CurrencySettingsCard(
  strings: AppStrings = LocalStrings.current,
) {
  val context = LocalContext.current
  val colors = LocalAppColors.current
  val activeCurrency = CurrencyHelper.currentCurrency

  val currencyList = AppCurrency.values().toList()
  val options = currencyList.map { it.label }

  AppCard {
    Text(
      text = strings["currency_settings_title"].ifBlank { "Format Mata Uang & Konversi" },
      fontWeight = FontWeight.Bold,
      fontSize = 16.sp,
      color = colors.text,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = strings["currency_settings_subtitle"].ifBlank {
        "Pilih mata uang yang diinginkan. Seluruh nominal transaksi di aplikasi akan langsung dikonversi otomatis sesuai kurs acuan terkini."
      },
      color = colors.muted,
      fontSize = 12.sp,
    )
    Spacer(modifier = Modifier.height(12.dp))

    AppDropdown(
      label = strings["currency_active_label"].ifBlank { "Mata Uang Aktif" },
      placeholder = strings["currency_dropdown_placeholder"].ifBlank { "Pilih Mata Uang" },
      options = options,
      selected = activeCurrency.label,
      onSelected = { selectedLabel ->
        val chosen = currencyList.find { it.label == selectedLabel } ?: AppCurrency.IDR
        CurrencyHelper.setCurrency(context, chosen)
      },
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Info Kurs & Contoh Konversi
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .background(colors.accent.copy(alpha = 0.1f))
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(colors.accent),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = activeCurrency.symbol,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = androidx.compose.ui.graphics.Color.White,
        )
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = CurrencyHelper.getExchangeRateNote(activeCurrency, strings),
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = colors.text,
        )
        val sampleText = strings["currency_sample_label"].ifBlank { "Contoh" }
        Text(
          text = "$sampleText: Rp 1.000.000 = ${CurrencyHelper.format(1_000_000L)}",
          fontSize = 11.sp,
          color = colors.muted,
        )
        if (CurrencyHelper.lastUpdatedText != null) {
          val lastUpdateLabel = strings["currency_last_updated"].ifBlank { "Terakhir diperbarui" }
          Text(
            text = "🕒 $lastUpdateLabel: ${CurrencyHelper.lastUpdatedText}",
            fontSize = 10.sp,
            color = colors.muted,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Tombol refresh kurs online secara real-time
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(colors.bg2)
        .clickable(enabled = !CurrencyHelper.isUpdatingRates) {
          CurrencyHelper.fetchOnlineRates(context)
        }
        .padding(vertical = 8.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = if (CurrencyHelper.isUpdatingRates) {
          "⏳ " + (strings["currency_updating"].ifBlank { "Mengambil Kurs Online..." })
        } else {
          "🔄 " + (strings["currency_refresh_btn"].ifBlank { "Perbarui Kurs Sekarang" })
        },
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = colors.accent,
      )
    }
  }
}
