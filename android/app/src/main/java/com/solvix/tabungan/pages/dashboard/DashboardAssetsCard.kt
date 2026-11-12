package com.solvix.tabungan.pages.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AnimatedAmountText
import com.solvix.tabungan.AppCard
import com.solvix.tabungan.AppDimens
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.InitialBalanceManager
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.LocalStrings
import com.solvix.tabungan.MoneyEntry

@Composable
fun DashboardAssetsCard(
  incomeEntries: List<MoneyEntry>,
  expenseEntries: List<MoneyEntry>,
  transferEntries: List<MoneyEntry>,
  netBalance: Long,
  strings: AppStrings = LocalStrings.current,
  onTransferClick: () -> Unit = {},
  userId: String = "",
) {
  val context = LocalContext.current
  val colors = LocalAppColors.current
  val configuration = LocalConfiguration.current
  val isSmallScreen = configuration.screenWidthDp < 360

  var expanded by remember { mutableStateOf(false) }
  var showInitialBalanceModal by remember { mutableStateOf(false) }
  var selectedLedgerAccount by remember { mutableStateOf<AccountAssetItem?>(null) }
  val initialVersion by InitialBalanceManager.versionState

  val initialBalances = remember(initialVersion) {
    InitialBalanceManager.balancesState.value.ifEmpty { InitialBalanceManager.getInitialBalances(context) }
  }

  val assets = remember(incomeEntries, expenseEntries, transferEntries, netBalance, initialBalances, initialVersion) {
    calculateAccountAssets(
      incomeEntries = incomeEntries,
      expenseEntries = expenseEntries,
      transferEntries = transferEntries,
      targetNetBalance = netBalance,
      initialBalances = initialBalances,
    )
  }

  val title = strings["assets_title"].ifBlank { "Aset" }
  val netBalanceLabel = strings["assets_net_balance"].ifBlank { "Saldo Bersih" }
  val transferLabel = strings["assets_transfer"].ifBlank { "Transfer" }
  val emptyLabel = strings["assets_empty"].ifBlank { "Belum ada transaksi aset. Mulai catat atau atur saldo awal!" }
  val pctText = strings["assets_pct_total"].ifBlank { "dari total aset" }
  val showLessLabel = strings["assets_show_less"].ifBlank { "▲ Tampilkan Lebih Sedikit" }
  val showAllLabel = strings["assets_show_all"].ifBlank { "▼ Lihat Semua" }
  val initialBalanceBtnLabel = strings["initial_balance_btn"].ifBlank { "Saldo Awal" }

  AppCard {
    Column(verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 10.dp else 12.dp)) {
      // 1. Header: Judul Aset & Saldo Bersih (Lega & Tidak Berdesakan)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
          Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = if (isSmallScreen) 14.sp else 15.sp,
            color = colors.text,
          )
          Text(
            text = "Total aset riil per rekening & dompet",
            fontSize = 11.sp,
            color = colors.muted,
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.accent.copy(alpha = 0.1f))
            .padding(horizontal = 9.dp, vertical = 5.dp),
          contentAlignment = Alignment.Center,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              text = "$netBalanceLabel:",
              fontSize = 11.sp,
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
      }

      // 2. Baris Tombol Aksi: Atur Saldo Awal & Transfer (Lega & Ergonomis Penuh)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Tombol 1: Saldo Awal
        Box(
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(AppDimens.radiusSm))
            .background(colors.bg2)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(AppDimens.radiusSm))
            .clickable { showInitialBalanceModal = true }
            .padding(horizontal = 10.dp, vertical = 8.dp),
          contentAlignment = Alignment.Center,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.Tune,
              contentDescription = null,
              tint = colors.text,
              modifier = Modifier.size(14.dp),
            )
            Text(
              text = initialBalanceBtnLabel,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = colors.text,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            if (initialBalances.isNotEmpty()) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(colors.accent)
              )
            }
          }
        }

        // Tombol 2: Transfer Saldo
        Box(
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(AppDimens.radiusSm))
            .background(colors.accent.copy(alpha = 0.12f))
            .border(1.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(AppDimens.radiusSm))
            .clickable { onTransferClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
          contentAlignment = Alignment.Center,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.SwapHoriz,
              contentDescription = null,
              tint = colors.accent,
              modifier = Modifier.size(16.dp),
            )
            Text(
              text = transferLabel,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = colors.accent,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }

      // Banner onboarding ergonomis jika belum ada saldo awal
      if (initialBalances.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.accent.copy(alpha = 0.08f))
            .border(1.dp, colors.accent.copy(alpha = 0.22f), RoundedCornerShape(8.dp))
            .clickable { showInitialBalanceModal = true }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.Lightbulb,
              contentDescription = null,
              tint = colors.accent,
              modifier = Modifier.size(16.dp),
            )
            Text(
              text = "Atur saldo awal rekening & dompetmu agar aset riil sesuai.",
              fontSize = 11.sp,
              color = colors.text,
              modifier = Modifier.weight(1f),
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(colors.accent)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
              Text(
                text = "Atur",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onGradient,
              )
            }
          }
        }
      }

      if (assets.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.bg2)
            .padding(vertical = 16.dp, horizontal = 10.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = emptyLabel,
            color = colors.muted,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
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
              onClick = { selectedLedgerAccount = item },
            )
          }
        }

        if (assets.size > 4) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { expanded = !expanded }
              .heightIn(min = 44.dp)
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

  if (showInitialBalanceModal) {
    InitialBalanceModal(
      onDismiss = { showInitialBalanceModal = false },
      onBalancesChanged = {
        InitialBalanceManager.versionState.intValue++
      },
      strings = strings,
      userId = userId,
    )
  }

  selectedLedgerAccount?.let { acc ->
    AccountLedgerModal(
      account = acc,
      incomeEntries = incomeEntries,
      expenseEntries = expenseEntries,
      transferEntries = transferEntries,
      onDismiss = { selectedLedgerAccount = null },
      onOpenTransferFromThisAccount = {
        onTransferClick()
      },
    )
  }
}
