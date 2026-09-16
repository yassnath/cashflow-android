package com.solvix.tabungan.pages.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AnimatedAmountText
import com.solvix.tabungan.AppCard
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.LocalStrings
import com.solvix.tabungan.MoneyEntry
import com.solvix.tabungan.formatRupiah

@Composable
fun DashboardAssetsCard(
  incomeEntries: List<MoneyEntry>,
  expenseEntries: List<MoneyEntry>,
  transferEntries: List<MoneyEntry>,
  netBalance: Long,
  strings: AppStrings = LocalStrings.current,
  onTransferClick: () -> Unit = {},
) {
  val colors = LocalAppColors.current
  var expanded by remember { mutableStateOf(false) }

  val assets = remember(incomeEntries, expenseEntries, transferEntries, netBalance) {
    calculateAccountAssets(incomeEntries, expenseEntries, transferEntries, netBalance)
  }

  val title = strings["assets_title"].ifBlank { "Aset" }
  val netBalanceLabel = strings["assets_net_balance"].ifBlank { "Saldo Bersih" }
  val transferLabel = strings["assets_transfer"].ifBlank { "Transfer" }
  val emptyLabel = strings["assets_empty"].ifBlank { "Belum ada transaksi aset. Mulai catat atau transfer dana!" }
  val pctText = strings["assets_pct_total"].ifBlank { "dari total aset" }
  val showLessLabel = strings["assets_show_less"].ifBlank { "▲ Tampilkan Lebih Sedikit" }
  val showAllLabel = strings["assets_show_all"].ifBlank { "▼ Lihat Semua" }

  AppCard {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = colors.text,
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              text = "$netBalanceLabel:",
              fontSize = 12.sp,
              color = colors.muted,
              fontWeight = FontWeight.Medium,
            )
            AnimatedAmountText(
              amount = netBalance,
              fontSize = 12.sp,
              color = if (netBalance >= 0) colors.accent else colors.danger,
              fontWeight = FontWeight.Bold,
            )
          }
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.accent.copy(alpha = 0.12f))
            .clickable { onTransferClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        ) {
          Text(text = "🔁 $transferLabel", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.accent)
        }
      }

      if (assets.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.bg2)
            .padding(vertical = 16.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = emptyLabel,
            color = colors.muted,
            fontSize = 12.sp,
          )
        }
      } else {
        val displayedAssets = if (expanded) assets else assets.take(4)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          displayedAssets.forEach { item ->
            DashboardAssetRow(
              item = item,
              netBalance = netBalance,
              pctTotalText = pctText,
            )
          }
        }

        if (assets.size > 4) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { expanded = !expanded }
              .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = if (expanded) showLessLabel else "$showAllLabel (${assets.size})",
              fontSize = 12.sp,
              color = colors.accent,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }
      }
    }
  }
}
