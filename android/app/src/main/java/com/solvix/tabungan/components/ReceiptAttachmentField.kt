package com.solvix.tabungan.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.LocalAppColors

@Composable
fun ReceiptAttachmentField(
  receiptUri: String,
  onPickReceipt: () -> Unit,
  onRemoveReceipt: () -> Unit,
  pickLabel: String,
  attachmentTitle: String = "Lampiran Bukti / Struk (Opsional)",
  attachedLabel: String = "Foto Bukti Terlampir",
  deleteLabel: String = "Hapus",
) {
  val colors = LocalAppColors.current

  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Text(
      text = attachmentTitle,
      fontSize = 12.sp,
      color = colors.muted,
      fontWeight = FontWeight.SemiBold,
    )
    if (receiptUri.isBlank()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(colors.bg2)
          .border(1.dp, colors.cardBorder, RoundedCornerShape(10.dp))
          .clickable(onClick = onPickReceipt)
          .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(text = "📷", fontSize = 14.sp)
          Text(
            text = pickLabel,
            fontSize = 12.sp,
            color = colors.accent,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }
    } else {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(colors.accent.copy(alpha = 0.1f))
          .border(1.dp, colors.accent.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
          .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(text = "📎", fontSize = 14.sp)
          Text(
            text = attachedLabel,
            fontSize = 12.sp,
            color = colors.text,
            fontWeight = FontWeight.Bold,
          )
        }
        Box(
          modifier = Modifier
            .clip(CircleShape)
            .background(colors.danger.copy(alpha = 0.15f))
            .clickable(onClick = onRemoveReceipt)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
          Text(
            text = "$deleteLabel ✕",
            fontSize = 11.sp,
            color = colors.danger,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }
  }
}
