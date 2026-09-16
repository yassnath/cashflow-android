package com.solvix.tabungan.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.solvix.tabungan.AppCard
import com.solvix.tabungan.AppTextField
import com.solvix.tabungan.EntryType
import com.solvix.tabungan.GhostButton
import com.solvix.tabungan.GradientButton
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.LocalStrings
import com.solvix.tabungan.MoneyEntry
import com.solvix.tabungan.RecurringManager
import com.solvix.tabungan.RecurringTransaction
import com.solvix.tabungan.formatRupiah

@Composable
fun RecurringSubscriptionsModal(
  onDismiss: () -> Unit,
  onEntriesGenerated: (List<MoneyEntry>) -> Unit,
) {
  val context = LocalContext.current
  val colors = LocalAppColors.current
  val strings = LocalStrings.current

  var items by remember { mutableStateOf(RecurringManager.getAll(context)) }
  var showAddForm by remember { mutableStateOf(false) }

  var title by remember { mutableStateOf("") }
  var amountStr by remember { mutableStateOf("") }
  var isExpense by remember { mutableStateOf(true) }
  var category by remember { mutableStateOf(strings["category_subscription"] ?: "Langganan") }
  var dueDayStr by remember { mutableStateOf("1") }
  var note by remember { mutableStateOf("") }
  var toastMessage by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = onDismiss) {
    Box(
      modifier = Modifier
        .fillMaxWidth(0.96f)
        .clip(RoundedCornerShape(20.dp))
        .background(colors.card)
        .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
        .padding(18.dp),
    ) {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = strings["recurring_modal_title"].ifBlank { "Transaksi Rutin & Langganan" },
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = colors.text,
            )
            Text(
              text = strings["recurring_modal_subtitle"].ifBlank { "Otomatisasi pengeluaran berkala" },
              fontSize = 11.sp,
              color = colors.muted,
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(imageVector = Icons.Rounded.Close, contentDescription = strings["close"], tint = colors.muted)
          }
        }

        if (toastMessage != null) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(colors.accent.copy(alpha = 0.15f))
              .padding(8.dp),
          ) {
            Text(text = toastMessage!!, fontSize = 11.sp, color = colors.accent, fontWeight = FontWeight.Bold)
          }
        }

        // Tombol Trigger Cek Sekarang
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.bg2)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(10.dp))
            .clickable {
              val generated = mutableListOf<MoneyEntry>()
              val count = RecurringManager.checkAndGenerateDueTransactions(context) { entry ->
                generated.add(entry)
              }
              if (count > 0) {
                onEntriesGenerated(generated)
                val succMsg = strings["recurring_success_gen"].ifBlank { "transaksi rutin berhasil dicatat otomatis! ✨" }
                toastMessage = "$count $succMsg"
              } else {
                toastMessage = strings["recurring_already_gen"].ifBlank { "Semua transaksi rutin bulan ini sudah tercatat." }
              }
              items = RecurringManager.getAll(context)
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = strings["recurring_check_auto"].ifBlank { "Cek & Catat Otomatis" },
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = colors.text,
            )
            Text(
              text = strings["recurring_check_auto_desc"].ifBlank { "Catat tagihan bulan ini yang sudah jatuh tempo" },
              fontSize = 10.sp,
              color = colors.muted,
            )
          }
          Text(
            text = strings["recurring_run_now"].ifBlank { "Jalankan ⚡" },
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = colors.accent,
          )
        }

        // Form Tambah
        if (showAddForm) {
          AppCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text(
                text = strings["recurring_add_title"].ifBlank { "Tambah Langganan Rutin Baru" },
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = colors.text,
              )

              AppTextField(
                label = strings["recurring_label_title"].ifBlank { "Judul Langganan" },
                value = title,
                onValueChange = { title = it },
                placeholder = strings["recurring_placeholder_title"].ifBlank { "Misal: Netflix, Listrik, Kos" },
              )

              AppTextField(
                label = strings["amount"].ifBlank { "Nominal" },
                value = amountStr,
                onValueChange = { amountStr = it },
                placeholder = "150000",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              )

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isExpense) colors.danger.copy(alpha = 0.2f) else colors.bg2)
                    .clickable { isExpense = true }
                    .padding(vertical = 8.dp),
                  contentAlignment = Alignment.Center,
                ) {
                  Text(
                    text = strings["entry_type_expense"].ifBlank { "Pengeluaran" },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpense) colors.danger else colors.muted,
                  )
                }
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (!isExpense) colors.accent.copy(alpha = 0.2f) else colors.bg2)
                    .clickable { isExpense = false }
                    .padding(vertical = 8.dp),
                  contentAlignment = Alignment.Center,
                ) {
                  Text(
                    text = strings["entry_type_income"].ifBlank { "Pemasukan" },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (!isExpense) colors.accent else colors.muted,
                  )
                }
              }

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                  AppTextField(
                    label = strings["recurring_due_day"].ifBlank { "Tanggal Tagihan" },
                    value = dueDayStr,
                    onValueChange = { dueDayStr = it },
                    placeholder = "1 - 31",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  )
                }
                Box(modifier = Modifier.weight(1f)) {
                  AppTextField(
                    label = strings["category"].ifBlank { "Kategori" },
                    value = category,
                    onValueChange = { category = it },
                    placeholder = "Hiburan",
                  )
                }
              }

              AppTextField(
                label = strings["recurring_note_optional"].ifBlank { "Catatan Tambahan (Opsional)" },
                value = note,
                onValueChange = { note = it },
                placeholder = strings["note_optional"].ifBlank { "Catatan tambahan..." },
              )

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                GhostButton(
                  text = strings["confirm_cancel"].ifBlank { "Batal" },
                  modifier = Modifier.weight(1f),
                  onClick = { showAddForm = false },
                )
                GradientButton(
                  text = strings["save"].ifBlank { "Simpan" },
                  modifier = Modifier.weight(1f),
                  enabled = title.isNotBlank() && (amountStr.toIntOrNull() ?: 0) > 0,
                  onClick = {
                    val newObj = RecurringTransaction(
                      title = title.trim(),
                      amount = amountStr.toIntOrNull() ?: 0,
                      type = if (isExpense) "Expense" else "Income",
                      category = category.trim().ifBlank { "Rutin" },
                      dueDay = (dueDayStr.toIntOrNull() ?: 1).coerceIn(1, 31),
                      note = note.trim(),
                    )
                    RecurringManager.add(context, newObj)
                    items = RecurringManager.getAll(context)
                    title = ""
                    amountStr = ""
                    note = ""
                    showAddForm = false
                    toastMessage = strings["recurring_added_success"].ifBlank { "Langganan rutin berhasil ditambahkan!" }
                  },
                )
              }
            }
          }
        } else {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(colors.accent.copy(alpha = 0.12f))
              .border(1.dp, colors.accent.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
              .clickable { showAddForm = true }
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(imageVector = Icons.Rounded.Add, contentDescription = strings["recurring_add_title"], tint = colors.accent, modifier = Modifier.size(16.dp))
              Text(
                text = strings["recurring_add_btn"].ifBlank { "Tambah Transaksi Rutin Baru" },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.accent,
              )
            }
          }
        }

        // Daftar Langganan
        Text(
          text = "${strings["recurring_active_list"].ifBlank { "Daftar Transaksi Aktif" }} (${items.size})",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = colors.muted,
        )

        if (items.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = strings["recurring_empty"].ifBlank { "Belum ada transaksi rutin." },
              fontSize = 12.sp,
              color = colors.muted,
            )
          }
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { item ->
              val isItemExpense = item.type.equals("Expense", ignoreCase = true)
              val dayPrefix = strings["recurring_every_day"].ifBlank { "Tiap tgl" }
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(colors.bg2)
                  .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                  .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                      text = item.title,
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = colors.text,
                    )
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isItemExpense) colors.danger.copy(alpha = 0.15f) else colors.accent.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                      Text(
                        text = "$dayPrefix ${item.dueDay}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isItemExpense) colors.danger else colors.accent,
                      )
                    }
                  }
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = "${if (isItemExpense) "-" else "+"} ${formatRupiah(item.amount)} • ${item.category}",
                    fontSize = 11.sp,
                    color = if (isItemExpense) colors.danger else colors.accent,
                    fontWeight = FontWeight.SemiBold,
                  )
                  if (item.note.isNotBlank()) {
                    Text(text = item.note, fontSize = 10.sp, color = colors.muted)
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Switch(
                    checked = item.isActive,
                    onCheckedChange = {
                      RecurringManager.toggleActive(context, item.id)
                      items = RecurringManager.getAll(context)
                    },
                  )
                  IconButton(
                    onClick = {
                      RecurringManager.delete(context, item.id)
                      items = RecurringManager.getAll(context)
                    },
                    modifier = Modifier.size(28.dp),
                  ) {
                    Icon(imageVector = Icons.Rounded.Delete, contentDescription = strings["delete"], tint = colors.danger.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.linearGradient(listOf(colors.accent, colors.accent2)))
            .clickable { onDismiss() },
          contentAlignment = Alignment.Center,
        ) {
          Text(text = strings["close"].ifBlank { "Tutup" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
      }
    }
  }
}
