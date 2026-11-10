package com.solvix.tabungan.pages.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppCard
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.MoneyEntry
import com.solvix.tabungan.formatRupiah
import java.util.Calendar

/**
 * Kartu analisis Burn Rate dan Proyeksi Ketahanan Arus Kas Bulanan.
 * Membantu pengguna memantau laju pengeluaran dan mencegah saldo habis sebelum akhir bulan.
 */
@Composable
fun DashboardBurnRateCard(
  thisMonthExpenses: List<MoneyEntry>,
  netBalance: Long,
  modifier: Modifier = Modifier,
) {
  val colors = LocalAppColors.current
  val isSmallScreen = LocalConfiguration.current.screenWidthDp < 360

  val calendar = remember { Calendar.getInstance() }
  val daysElapsed = remember { calendar.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1) }
  val totalDaysInMonth = remember { calendar.getActualMaximum(Calendar.DAY_OF_MONTH) }
  val daysLeftInMonth = remember { (totalDaysInMonth - daysElapsed).coerceAtLeast(0) }

  val monthlySpent = remember(thisMonthExpenses) { thisMonthExpenses.sumOf { it.amount.toLong() } }
  val avgDailyBurn = remember(monthlySpent, daysElapsed) { monthlySpent / daysElapsed }

  val runwayDays = remember(netBalance, avgDailyBurn) {
    if (avgDailyBurn > 0 && netBalance > 0) (netBalance / avgDailyBurn).toInt()
    else if (netBalance <= 0) 0
    else 99
  }

  val projectedEndMonthBalance = remember(netBalance, avgDailyBurn, daysLeftInMonth) {
    netBalance - (avgDailyBurn * daysLeftInMonth)
  }

  // Tentukan status risiko finansial
  val (statusTitle, statusColor, statusBadge, statusMessage) = when {
    netBalance <= 0L -> BurnRateStatusInfo(
      "Saldo Menipis",
      Color(0xFFEF4444),
      "🔴 RISIKO TINGGI",
      "Saldo bersih Anda saat ini nol atau minus. Segera kurangi pengeluaran non-primer.",
    )
    runwayDays < 10 || projectedEndMonthBalance < 0L -> BurnRateStatusInfo(
      "Burn Rate Tinggi",
      Color(0xFFF59E0B),
      "⚠️ PERHATIAN",
      "Dengan laju pengeluaran saat ini, saldo diproyeksikan bertahan sekitar $runwayDays hari lagi.",
    )
    runwayDays in 10..25 -> BurnRateStatusInfo(
      "Arus Kas Cukup",
      Color(0xFF3B82F6),
      "🟡 WASPADA",
      "Laju pengeluaran terkendali. Estimasi sisa saldo akhir bulan: ${formatRupiah(projectedEndMonthBalance)}.",
    )
    else -> BurnRateStatusInfo(
      "Arus Kas Sehat",
      Color(0xFF10B981),
      "🟢 AMAN",
      "Ketahanan saldo mencukupi lebih dari $runwayDays hari. Proyeksi akhir bulan sangat aman.",
    )
  }

  AppCard(modifier = modifier) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(statusColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
          ) {
            Text(text = "🔥", fontSize = 14.sp)
          }
          Column {
            Text(
              text = "Proyeksi Arus Kas & Burn Rate",
              fontWeight = FontWeight.Bold,
              fontSize = if (isSmallScreen) 13.sp else 14.sp,
              color = colors.text,
            )
            Text(
              text = "Berdasarkan $daysElapsed hari di bulan ini",
              fontSize = 11.sp,
              color = colors.muted,
            )
          }
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(statusColor.copy(alpha = 0.12f))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        ) {
          Text(
            text = statusBadge,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = statusColor,
          )
        }
      }

      // Baris Metrik Kunci
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(colors.bg2.copy(alpha = 0.6f))
          .padding(vertical = 8.dp, horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(text = "Laju Harian", fontSize = 10.sp, color = colors.muted)
          Text(
            text = formatRupiah(avgDailyBurn),
            fontSize = if (isSmallScreen) 11.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            color = colors.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

        Column(modifier = Modifier.weight(1f)) {
          Text(text = "Ketahanan", fontSize = 10.sp, color = colors.muted)
          Text(
            text = if (runwayDays >= 90) "> 3 Bulan" else "$runwayDays Hari",
            fontSize = if (isSmallScreen) 11.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            color = statusColor,
            maxLines = 1,
          )
        }

        Column(modifier = Modifier.weight(1f)) {
          Text(text = "Estimasi Akhir Bulan", fontSize = 10.sp, color = colors.muted)
          Text(
            text = formatRupiah(projectedEndMonthBalance.coerceAtLeast(0L)),
            fontSize = if (isSmallScreen) 11.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (projectedEndMonthBalance >= 0) colors.accent else Color(0xFFEF4444),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      // Pesan Rekomendasi Finansial
      Text(
        text = statusMessage,
        fontSize = 11.sp,
        color = colors.muted,
        lineHeight = 15.sp,
      )
    }
  }
}

private data class BurnRateStatusInfo(val title: String, val color: Color, val badge: String, val message: String)
