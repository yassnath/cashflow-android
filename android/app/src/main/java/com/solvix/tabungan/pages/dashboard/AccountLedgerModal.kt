package com.solvix.tabungan.pages.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.solvix.tabungan.EntryType
import com.solvix.tabungan.GhostButton
import com.solvix.tabungan.GradientButton
import com.solvix.tabungan.InitialBalanceManager
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.MoneyEntry
import com.solvix.tabungan.formatRupiah

private enum class LedgerFilter { ALL, IN, OUT }

data class LedgerMutationItem(
  val entry: MoneyEntry,
  val isMoneyIn: Boolean,
  val label: String,
  val counterpartAccount: String,
)

/**
 * Halaman / Modal Detail Mutasi Per Akun (Account Ledger).
 * Menampilkan rincian saldo awal, total arus kas masuk/keluar,
 * dan seluruh transaksi yang menyangkut akun/rekening tertentu (layaknya rekening koran m-banking).
 */
@Composable
fun AccountLedgerModal(
  account: AccountAssetItem,
  incomeEntries: List<MoneyEntry>,
  expenseEntries: List<MoneyEntry>,
  transferEntries: List<MoneyEntry>,
  onDismiss: () -> Unit,
  onOpenTransferFromThisAccount: (String) -> Unit = {},
  onPreviewReceipt: (String) -> Unit = {},
) {
  val colors = LocalAppColors.current
  val context = LocalContext.current
  val successGreen = Color(0xFF4CAF50)
  var selectedFilter by remember { mutableStateOf(LedgerFilter.ALL) }

  // Ambil saldo awal akun ini
  val initialBal = remember(account.name, InitialBalanceManager.versionState.intValue) {
    InitialBalanceManager.getInitialBalances(context)[account.name] ?: 0L
  }

  // Saring transaksi yang berkaitan dengan akun ini
  val mutations = remember(account.name, incomeEntries, expenseEntries, transferEntries) {
    val accName = account.name.trim()
    val list = mutableListOf<LedgerMutationItem>()

    // 1. Pemasukan ke rekening ini
    incomeEntries.filter { it.sourceOrMethod.trim().equals(accName, ignoreCase = true) }.forEach {
      list.add(
        LedgerMutationItem(
          entry = it,
          isMoneyIn = true,
          label = it.category.ifBlank { "Pemasukan" },
          counterpartAccount = "Pemasukan Langsung",
        )
      )
    }

    // 2. Pengeluaran dari rekening ini
    expenseEntries.filter { it.sourceOrMethod.trim().equals(accName, ignoreCase = true) }.forEach {
      list.add(
        LedgerMutationItem(
          entry = it,
          isMoneyIn = false,
          label = it.category.ifBlank { "Pengeluaran" },
          counterpartAccount = "Pengeluaran",
        )
      )
    }

    // 3. Transfer yang melibatkan rekening ini
    transferEntries.forEach { transfer ->
      val from = transfer.sourceOrMethod.trim()
      val to = transfer.channelOrBank.trim()

      if (from.equals(accName, ignoreCase = true)) {
        // Transfer Keluar dari akun ini
        list.add(
          LedgerMutationItem(
            entry = transfer,
            isMoneyIn = false,
            label = "Transfer Keluar ke $to",
            counterpartAccount = to,
          )
        )
      } else if (to.equals(accName, ignoreCase = true)) {
        // Transfer Masuk ke akun ini
        list.add(
          LedgerMutationItem(
            entry = transfer,
            isMoneyIn = true,
            label = "Transfer Masuk dari $from",
            counterpartAccount = from,
          )
        )
      }
    }

    // Urutkan dari transaksi paling baru
    list.sortedByDescending { it.entry.date }
  }

  // Hitung total masuk & total keluar
  val totalIn = remember(mutations) {
    mutations.filter { it.isMoneyIn }.sumOf { it.entry.amount.toLong() }
  }
  val totalOut = remember(mutations) {
    mutations.filter { !it.isMoneyIn }.sumOf { it.entry.amount.toLong() }
  }

  val displayedMutations = remember(mutations, selectedFilter) {
    when (selectedFilter) {
      LedgerFilter.ALL -> mutations
      LedgerFilter.IN -> mutations.filter { it.isMoneyIn }
      LedgerFilter.OUT -> mutations.filter { !it.isMoneyIn }
    }
  }

  val configuration = LocalConfiguration.current
  val isSmallScreen = configuration.screenWidthDp < 360
  val maxModalHeight = (configuration.screenHeightDp * 0.90f).dp

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(if (isSmallScreen) 0.98f else 0.95f)
        .widthIn(max = 520.dp)
        .imePadding()
        .padding(vertical = if (isSmallScreen) 8.dp else 16.dp),
      contentAlignment = Alignment.Center,
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = maxModalHeight)
          .clip(RoundedCornerShape(22.dp))
          .background(colors.card)
          .border(1.dp, colors.cardBorder, RoundedCornerShape(22.dp))
          .padding(18.dp),
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
        // Header Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            AccountBrandLogo(
              accountName = account.name,
              brand = account.brand,
              size = 40.dp,
            )
            Column {
              Text(
                text = account.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = "Buku Rekening & Mutasi",
                fontSize = 11.sp,
                color = colors.muted,
              )
            }
          }

          Box(
            modifier = Modifier
            .size(44.dp)
              .clip(CircleShape)
              .background(colors.bg2)
              .clickable { onDismiss() },
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Rounded.Close,
              contentDescription = "Tutup",
              tint = colors.text,
              modifier = Modifier.size(18.dp),
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Saldo Berjalan Card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(colors.accent.copy(alpha = 0.12f), colors.accent2.copy(alpha = 0.08f))))
            .border(1.dp, colors.accent.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        ) {
          Column {
            Text(
              text = "SALDO BERJALAN SAAT INI",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = colors.accent,
              letterSpacing = 0.8.sp,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = formatRupiah(account.balance),
              fontSize = 22.sp,
              fontWeight = FontWeight.ExtraBold,
              color = if (account.balance >= 0) colors.text else colors.danger,
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Rincian Arus Kas Rekening
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Column {
                Text(text = "Saldo Awal", fontSize = 10.sp, color = colors.muted)
                Text(
                  text = formatRupiah(initialBal),
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = colors.text,
                )
              }
              Column {
                Text(text = "Total Masuk", fontSize = 10.sp, color = colors.muted)
                Text(
                  text = "+${formatRupiah(totalIn)}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = successGreen,
                )
              }
              Column {
                Text(text = "Total Keluar", fontSize = 10.sp, color = colors.muted)
                Text(
                  text = "-${formatRupiah(totalOut)}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = colors.danger,
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Tabs (Semua, Masuk, Keluar)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          listOf(
            LedgerFilter.ALL to "Semua (${mutations.size})",
            LedgerFilter.IN to "Masuk",
            LedgerFilter.OUT to "Keluar",
          ).forEach { (filter, label) ->
            val isSelected = selectedFilter == filter
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) colors.accent else colors.bg2)
                .clickable { selectedFilter = filter }
                .heightIn(min = 44.dp)
                .padding(vertical = 7.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) colors.onGradient else colors.text,
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Daftar Riwayat Mutasi
        if (displayedMutations.isEmpty()) {
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth(),
            contentAlignment = Alignment.Center,
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                contentDescription = null,
                tint = colors.muted.copy(alpha = 0.5f),
                modifier = Modifier.size(36.dp),
              )
              Text(
                text = "Belum ada transaksi pada rekening ini",
                fontSize = 12.sp,
                color = colors.muted,
              )
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            items(displayedMutations, key = { it.entry.id }) { item ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(colors.bg2.copy(alpha = 0.6f))
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                  modifier = Modifier.weight(1f),
                ) {
                  // Ikon Arah Transaksi
                  Box(
                    modifier = Modifier
                      .size(34.dp)
                      .clip(CircleShape)
                      .background(
                        if (item.isMoneyIn) successGreen.copy(alpha = 0.15f) else colors.danger.copy(alpha = 0.15f)
                      ),
                    contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                      imageVector = if (item.isMoneyIn) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                      contentDescription = null,
                      tint = if (item.isMoneyIn) successGreen else colors.danger,
                      modifier = Modifier.size(18.dp),
                    )
                  }

                  Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                      text = item.label,
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 12.sp,
                      color = colors.text,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                      horizontalArrangement = Arrangement.spacedBy(6.dp),
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Text(
                        text = item.entry.date,
                        fontSize = 10.sp,
                        color = colors.muted,
                      )
                      if (item.entry.receiptUri.isNotBlank()) {
                        Text(
                          text = "• Struk 📎",
                          fontSize = 10.sp,
                          color = colors.accent,
                          fontWeight = FontWeight.Bold,
                          modifier = Modifier.clickable { onPreviewReceipt(item.entry.receiptUri) },
                        )
                      }
                    }
                  }
                }

                // Nominal Mutasi
                Text(
                  text = "${if (item.isMoneyIn) "+" else "-"}${formatRupiah(item.entry.amount.toLong())}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = if (item.isMoneyIn) successGreen else colors.danger,
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tombol Aksi Bawah
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          GhostButton(
            text = "Tutup",
            modifier = Modifier.weight(1f),
            onClick = onDismiss,
          )
          GradientButton(
            text = "🔁 Transfer",
            modifier = Modifier.weight(1f),
            onClick = {
              onDismiss()
              onOpenTransferFromThisAccount(account.name)
            },
          )
        }
      }
    }
  }
}
}
