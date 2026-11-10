package com.solvix.tabungan.pages.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AnimatedAmountText
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.formatRupiah

@Composable
fun DashboardAssetRow(
  item: AccountAssetItem,
  netBalance: Long,
  pctTotalText: String = "dari total aset",
  onClick: (() -> Unit)? = null,
) {
  val colors = LocalAppColors.current
  val ratio = if (netBalance > 0) (item.balance.toDouble() / netBalance).coerceIn(0.0, 1.0) else 0.0
  val percentInt = Math.round(ratio * 100).toInt()

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .background(colors.bg2.copy(alpha = 0.65f))
      .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
      .padding(horizontal = 12.dp, vertical = 9.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.weight(1f),
    ) {
      AccountBrandLogo(
        accountName = item.name,
        brand = item.brand,
        size = 36.dp,
      )

      Column(modifier = Modifier.weight(1f, fill = false)) {
        Text(
          text = item.name,
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp,
          color = colors.text,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = "$percentInt% $pctTotalText",
          fontSize = 11.sp,
          color = colors.muted,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }

    AnimatedAmountText(
      amount = item.balance,
      fontWeight = FontWeight.Bold,
      fontSize = 13.sp,
      color = if (item.balance >= 0) colors.text else colors.danger,
    )
  }
}
