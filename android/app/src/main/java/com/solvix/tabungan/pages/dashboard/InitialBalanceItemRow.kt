package com.solvix.tabungan.pages.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppDimens
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.LocalStrings
import com.solvix.tabungan.formatRupiah

@Composable
fun InitialBalanceItemRow(
  accName: String,
  bal: Long,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  val colors = LocalAppColors.current
  val strings = LocalStrings.current
  val isSmallScreen = LocalConfiguration.current.screenWidthDp < 360

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(AppDimens.radiusSm))
      .background(colors.bg2)
      .padding(horizontal = if (isSmallScreen) 8.dp else 12.dp, vertical = if (isSmallScreen) 7.dp else 9.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 8.dp else 10.dp),
      modifier = Modifier.weight(1f),
    ) {
      AccountBrandLogo(
        accountName = accName,
        size = if (isSmallScreen) 28.dp else 32.dp,
      )
      Column(modifier = Modifier.weight(1f, fill = false)) {
        Text(
          text = accName,
          fontWeight = FontWeight.SemiBold,
          fontSize = if (isSmallScreen) 12.sp else 13.sp,
          color = colors.text,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = formatRupiah(bal),
          fontSize = if (isSmallScreen) 11.sp else 12.sp,
          color = colors.accent,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
        )
      }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
      // Tombol Edit
      Box(
        modifier = Modifier
          .size(if (isSmallScreen) 28.dp else 30.dp)
          .clip(CircleShape)
          .background(colors.card)
          .clickable(onClick = onEdit),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Rounded.Edit,
          contentDescription = strings["edit"].ifBlank { "Edit" },
          tint = colors.muted,
          modifier = Modifier.size(if (isSmallScreen) 14.dp else 16.dp),
        )
      }

      // Tombol Hapus
      Box(
        modifier = Modifier
          .size(if (isSmallScreen) 28.dp else 30.dp)
          .clip(CircleShape)
          .background(colors.danger.copy(alpha = 0.12f))
          .clickable(onClick = onDelete),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Rounded.DeleteOutline,
          contentDescription = strings["delete"].ifBlank { "Hapus" },
          tint = colors.danger,
          modifier = Modifier.size(if (isSmallScreen) 14.dp else 16.dp),
        )
      }
    }
  }
}
