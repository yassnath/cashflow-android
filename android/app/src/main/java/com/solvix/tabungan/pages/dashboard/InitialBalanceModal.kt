package com.solvix.tabungan.pages.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.solvix.tabungan.AppCard
import com.solvix.tabungan.AppDimens
import com.solvix.tabungan.AppDropdown
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.AppTextField
import com.solvix.tabungan.GhostButton
import com.solvix.tabungan.GradientButton
import com.solvix.tabungan.InitialBalanceManager
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.LocalStrings
import com.solvix.tabungan.components.AccountAutoResearchPreview
import com.solvix.tabungan.components.AppConfirmationDialog
import com.solvix.tabungan.formatRupiah
import android.widget.Toast

@Composable
fun InitialBalanceModal(
  onDismiss: () -> Unit,
  onBalancesChanged: () -> Unit,
  strings: AppStrings = LocalStrings.current,
  userId: String = "",
) {
  val context = LocalContext.current
  val colors = LocalAppColors.current
  val configuration = LocalConfiguration.current
  val screenWidthDp = configuration.screenWidthDp
  val screenHeightDp = configuration.screenHeightDp
  val isSmallScreen = screenWidthDp < 360

  val initialVersion by InitialBalanceManager.versionState
  val balances = remember(initialVersion) {
    InitialBalanceManager.balancesState.value.ifEmpty { InitialBalanceManager.getInitialBalances(context) }
  }

  LaunchedEffect(userId) {
    if (userId.isNotBlank()) {
      InitialBalanceManager.syncFromCloud(context, userId)
      onBalancesChanged()
    }
  }

  // State untuk form edit/tambah saldo awal
  var selectedAccount by remember { mutableStateOf(InitialBalanceManager.DEFAULT_ACCOUNTS.firstOrNull() ?: "Bank BCA") }
  var amountInput by remember { mutableStateOf("") }
  var isEditingCustom by remember { mutableStateOf(false) }
  var customAccountName by remember { mutableStateOf("") }
  var editingTargetAccount by remember { mutableStateOf<String?>(null) }
  var accountToDelete by remember { mutableStateOf<String?>(null) }

  val title = strings["initial_balance_title"].ifBlank { "Pengaturan Saldo Awal" }
  val subtitle = strings["initial_balance_subtitle"].ifBlank {
    "Tentukan saldo awal per rekening atau dompet agar total saldo dan ringkasan aset sesuai kondisi nyata."
  }

  val otherAccountLabel = "Lainnya"
  val accountOptions = remember(balances) {
    (InitialBalanceManager.DEFAULT_ACCOUNTS + balances.keys + listOf(otherAccountLabel)).distinct()
  }

  // Quick Chips Rekening Populer
  val quickAccountChips = remember(balances) {
    (listOf(
      "Bank BCA",
      "Bank Mandiri",
      "Bank BRI",
      "Bank BNI",
      "GoPay",
      "OVO",
      "DANA",
      "ShopeePay",
      "SeaBank",
      "Aset Kripto (Crypto)",
      "Uang Tunai (Cash)",
    ) + balances.keys + listOf(otherAccountLabel)).distinct()
  }

  // Quick Amount Presets
  val amountPresets = listOf(
    "+100rb" to 100_000L,
    "+500rb" to 500_000L,
    "+1jt" to 1_000_000L,
    "+5jt" to 5_000_000L,
    "+10jt" to 10_000_000L,
  )

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(if (isSmallScreen) 0.98f else 0.94f)
        .widthIn(max = 520.dp)
        .imePadding()
        .padding(vertical = if (isSmallScreen) 8.dp else 16.dp),
      contentAlignment = Alignment.Center,
    ) {
      AppCard(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = (screenHeightDp * 0.92f).dp),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 10.dp else 12.dp),
        ) {
          // Header Dialog Responsif
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 6.dp else 8.dp),
              modifier = Modifier.weight(1f),
            ) {
              Box(
                modifier = Modifier
                  .size(if (isSmallScreen) 30.dp else 34.dp)
                  .clip(CircleShape)
                  .background(colors.accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Rounded.AccountBalance,
                  contentDescription = null,
                  tint = colors.accent,
                  modifier = Modifier.size(if (isSmallScreen) 16.dp else 18.dp),
                )
              }
              Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                  text = title,
                  fontWeight = FontWeight.Bold,
                  fontSize = if (isSmallScreen) 14.sp else 15.sp,
                  color = colors.text,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = subtitle,
                  fontSize = 10.sp,
                  color = colors.muted,
                  maxLines = if (isSmallScreen) 1 else 2,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }

            Box(
              modifier = Modifier
                .size(if (isSmallScreen) 28.dp else 30.dp)
                .clip(CircleShape)
                .background(colors.bg2)
                .clickable { onDismiss() },
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Tutup",
                tint = colors.muted,
                modifier = Modifier.size(16.dp),
              )
            }
          }

          // Form Input / Edit Saldo Awal
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(AppDimens.radiusSm))
              .background(colors.bg2.copy(alpha = 0.5f))
              .border(1.dp, colors.cardBorder, RoundedCornerShape(AppDimens.radiusSm))
              .padding(if (isSmallScreen) 10.dp else 12.dp),
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = if (editingTargetAccount != null) "✏️ Edit Saldo $editingTargetAccount" else (strings["initial_balance_form_title"].ifBlank { "Atur Saldo Akun" }),
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = if (editingTargetAccount != null) colors.accent else colors.text,
                )
                if (editingTargetAccount != null) {
                  Text(
                    text = "Batal",
                    fontSize = 11.sp,
                    color = colors.danger,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                      editingTargetAccount = null
                      amountInput = ""
                      if (isEditingCustom) {
                        isEditingCustom = false
                        customAccountName = ""
                      }
                    },
                  )
                }
              }

              // Quick Chips Rekening Populer
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                quickAccountChips.forEach { chipName ->
                  val isChipSelected = if (chipName == otherAccountLabel) isEditingCustom else (!isEditingCustom && selectedAccount == chipName)
                  val hasBalance = balances.containsKey(chipName)
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .background(
                        if (isChipSelected) colors.accent.copy(alpha = 0.18f)
                        else if (hasBalance) colors.card
                        else colors.bg2
                      )
                      .border(
                        1.dp,
                        if (isChipSelected) colors.accent
                        else if (hasBalance) colors.accent.copy(alpha = 0.4f)
                        else colors.cardBorder,
                        RoundedCornerShape(8.dp)
                      )
                      .clickable {
                        if (chipName == otherAccountLabel) {
                          isEditingCustom = true
                          amountInput = ""
                        } else {
                          isEditingCustom = false
                          selectedAccount = chipName
                          val existing = balances[chipName] ?: 0L
                          amountInput = if (existing > 0L) existing.toString() else ""
                        }
                      }
                      .padding(horizontal = 8.dp, vertical = 5.dp),
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                      AccountBrandLogo(
                        accountName = chipName,
                        size = 14.dp,
                      )
                      Text(
                        text = chipName,
                        fontSize = 11.sp,
                        fontWeight = if (isChipSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isChipSelected) colors.accent else colors.text,
                      )
                      if (hasBalance) {
                        Box(
                          modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(colors.accent)
                        )
                      }
                    }
                  }
                }
              }

              // Dropdown Rekening Lengkap
              AppDropdown(
                label = strings["initial_balance_choose_account"].ifBlank { "Pilih Rekening / Dompet" },
                placeholder = strings["initial_balance_account_placeholder"].ifBlank { "Pilih akun atau bank..." },
                options = accountOptions,
                selected = if (isEditingCustom) otherAccountLabel else selectedAccount,
                onSelected = { chosen ->
                  if (chosen == otherAccountLabel) {
                    isEditingCustom = true
                    amountInput = ""
                  } else {
                    isEditingCustom = false
                    selectedAccount = chosen
                    val existing = balances[chosen] ?: 0L
                    amountInput = if (existing > 0L) existing.toString() else ""
                  }
                },
              )

              // Jika memilih Lainnya, tampilkan textfield nama kustom + Auto Research Preview
              if (isEditingCustom) {
                AppTextField(
                  label = strings["initial_balance_custom_name"].ifBlank { "Nama Rekening / Dompet Baru" },
                  value = customAccountName,
                  onValueChange = { customAccountName = it },
                  placeholder = strings["initial_balance_custom_placeholder"].ifBlank { "Misal: Bank Neo, Solana, LinkAja, BTPN" },
                )
                AccountAutoResearchPreview(
                  query = customAccountName,
                  modifier = Modifier.padding(top = 2.dp),
                )
              }

              // Input Nominal Saldo Awal
              val currentAccTarget = if (isEditingCustom) customAccountName.trim() else selectedAccount
              val amountLabel = if (currentAccTarget.isNotBlank()) {
                "Nominal Saldo Awal ($currentAccTarget)"
              } else {
                strings["initial_balance_amount_label"].ifBlank { "Nominal Saldo Awal" }
              }
              AppTextField(
                label = amountLabel,
                value = amountInput,
                onValueChange = { input ->
                  amountInput = input.filter { it.isDigit() }
                },
                placeholder = strings["initial_balance_amount_placeholder"].ifBlank { "Contoh: 5000000" },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              )

              // Ergonomic Amount Increment Presets
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                amountPresets.forEach { (label, inc) ->
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .background(colors.card)
                      .border(0.8.dp, colors.cardBorder, RoundedCornerShape(6.dp))
                      .clickable {
                        val current = amountInput.toLongOrNull() ?: 0L
                        val next = current + inc
                        amountInput = next.toString()
                      }
                      .padding(horizontal = 7.dp, vertical = 4.dp),
                  ) {
                    Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colors.accent)
                  }
                }

                if (amountInput.isNotBlank()) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .background(colors.danger.copy(alpha = 0.1f))
                      .clickable { amountInput = "" }
                      .padding(horizontal = 7.dp, vertical = 4.dp),
                  ) {
                    Text(text = "Reset", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.danger)
                  }
                }
              }

              // Preview Nominal Terformat
              if (amountInput.isNotBlank()) {
                val parsedAmount = amountInput.toLongOrNull() ?: 0L
                Text(
                  text = "Preview: ${formatRupiah(parsedAmount)}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = colors.accent,
                )
              }

              // Tombol Simpan / Perbarui
              GradientButton(
                text = if (editingTargetAccount != null) "Perbarui Saldo $currentAccTarget" else (strings["initial_balance_save"].ifBlank { "Simpan Saldo Awal" }),
                modifier = Modifier.fillMaxWidth(),
                enabled = currentAccTarget.isNotBlank() && (amountInput.toLongOrNull() ?: 0L) >= 0L,
                onClick = {
                  val amount = amountInput.toLongOrNull() ?: 0L
                  InitialBalanceManager.setInitialBalance(context, currentAccTarget, amount, userId = userId)
                  amountInput = ""
                  editingTargetAccount = null
                  if (isEditingCustom) {
                    customAccountName = ""
                    isEditingCustom = false
                  }
                  Toast.makeText(context, "Saldo awal berhasil disimpan!", Toast.LENGTH_SHORT).show()
                  onBalancesChanged()
                },
              )
            }
          }

          // Ringkasan & Total Saldo Awal Tersimpan
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = strings["initial_balance_list_title"].ifBlank { "Daftar Saldo Awal" },
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = colors.text,
            )
            if (balances.isNotEmpty()) {
              Text(
                text = "Total: ${formatRupiah(balances.values.sum())}",
                fontSize = if (isSmallScreen) 11.sp else 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.accent,
              )
            }
          }

          if (balances.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(colors.bg2.copy(alpha = 0.5f))
                .padding(vertical = 12.dp, horizontal = 8.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = strings["initial_balance_empty"].ifBlank { "Belum ada saldo awal yang diatur. Semua rekening memulai dari Rp 0." },
                fontSize = 11.sp,
                color = colors.muted,
              )
            }
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              balances.forEach { (accName, bal) ->
                InitialBalanceItemRow(
                  accName = accName,
                  bal = bal,
                  onEdit = {
                    editingTargetAccount = accName
                    val isCustom = !InitialBalanceManager.DEFAULT_ACCOUNTS.contains(accName)
                    isEditingCustom = isCustom
                    if (isCustom) {
                      customAccountName = accName
                    } else {
                      selectedAccount = accName
                    }
                    amountInput = bal.toString()
                  },
                  onDelete = {
                    accountToDelete = accName
                  },
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(2.dp))

          GhostButton(
            text = strings["close"].ifBlank { "Tutup" },
            modifier = Modifier.fillMaxWidth(),
            onClick = onDismiss,
          )
        }
      }
    }
  }

  // Dialog Validasi Hapus Saldo Awal (di Tengah Layar)
  accountToDelete?.let { targetAcc ->
    AppConfirmationDialog(
      title = "Hapus Saldo Awal?",
      message = "Apakah Anda yakin ingin menghapus saldo awal untuk rekening $targetAcc?",
      confirmText = "Hapus",
      cancelText = "Batal",
      isDestructive = true,
      onConfirm = {
        InitialBalanceManager.removeInitialBalance(context, targetAcc, userId = userId)
        if (editingTargetAccount == targetAcc) {
          editingTargetAccount = null
          amountInput = ""
        }
        accountToDelete = null
        Toast.makeText(context, "Saldo awal berhasil dihapus!", Toast.LENGTH_SHORT).show()
        onBalancesChanged()
      },
      onDismiss = { accountToDelete = null },
    )
  }
}
