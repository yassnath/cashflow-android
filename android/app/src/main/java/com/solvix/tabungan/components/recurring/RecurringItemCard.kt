package com.solvix.tabungan.components.recurring

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.RecurringTransaction
import com.solvix.tabungan.formatRupiah

@Composable
fun RecurringItemCard(
  item: RecurringTransaction,
  dayPrefix: String,
  deleteContentDescription: String,
  onToggleActive: (Boolean) -> Unit,
  onDelete: () -> Unit,
) {
  val colors = LocalAppColors.current
  val isItemExpense = item.type.equals("Expense", ignoreCase = true)

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
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
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
        onCheckedChange = onToggleActive,
      )
      IconButton(
        onClick = onDelete,
        modifier = Modifier.size(28.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.Delete,
          contentDescription = deleteContentDescription,
          tint = colors.danger.copy(alpha = 0.7f),
          modifier = Modifier.size(18.dp),
        )
      }
    }
  }
}
