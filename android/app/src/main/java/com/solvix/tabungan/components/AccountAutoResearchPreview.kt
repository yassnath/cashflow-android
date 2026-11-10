package com.solvix.tabungan.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AccountCategory
import com.solvix.tabungan.AccountIconManager
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.pages.dashboard.AccountBrandLogo

/**
 * Komponen pratinjau riset icon otomatis saat pengguna mengetik nama Bank, E-Wallet, atau Crypto.
 * Memberikan feedback visual instan bahwa icon berhasil dicari dan dihubungkan secara otomatis.
 */
@Composable
fun AccountAutoResearchPreview(
  query: String,
  modifier: Modifier = Modifier,
) {
  val colors = LocalAppColors.current
  val trimmed = query.trim()

  if (trimmed.isBlank()) {
    Row(
      modifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(colors.bg2.copy(alpha = 0.5f))
        .border(0.8.dp, colors.cardBorder, RoundedCornerShape(8.dp))
        .padding(horizontal = 10.dp, vertical = 7.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Icon(
        imageVector = Icons.Rounded.Search,
        contentDescription = null,
        tint = colors.accent,
        modifier = Modifier.size(16.dp),
      )
      Text(
        text = "Ketik nama Bank, E-Wallet, atau Crypto — icon otomatis dicari tanpa file gambar manual.",
        fontSize = 11.sp,
        color = colors.muted,
        lineHeight = 15.sp,
      )
    }
    return
  }

  val researched = remember(trimmed) { AccountIconManager.researchAccount(trimmed) }

  AnimatedVisibility(
    visible = true,
    enter = fadeIn() + slideInVertically { it / 2 },
    exit = fadeOut(),
  ) {
    Box(
      modifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .background(colors.card)
        .border(1.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
        .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.weight(1f),
        ) {
          AccountBrandLogo(
            accountName = trimmed,
            size = 32.dp,
          )

          Column(modifier = Modifier.weight(1f, fill = false)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                text = researched.cleanName,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              // Kategori badge
              val categoryBadgeBg = when (researched.category) {
                AccountCategory.BANK -> Color(0xFF00529C).copy(alpha = 0.15f)
                AccountCategory.EWALLET -> Color(0xFF00AED6).copy(alpha = 0.15f)
                AccountCategory.CRYPTO -> Color(0xFFF7931A).copy(alpha = 0.15f)
                AccountCategory.INVESTMENT -> Color(0xFF10B981).copy(alpha = 0.15f)
                AccountCategory.CASH -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                AccountCategory.OTHER -> colors.bg2
              }
              val categoryTextColor = when (researched.category) {
                AccountCategory.BANK -> Color(0xFF0284C7)
                AccountCategory.EWALLET -> Color(0xFF0284C7)
                AccountCategory.CRYPTO -> Color(0xFFD97706)
                AccountCategory.INVESTMENT -> Color(0xFF059669)
                AccountCategory.CASH -> Color(0xFF16A34A)
                AccountCategory.OTHER -> colors.muted
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(categoryBadgeBg)
                  .padding(horizontal = 5.dp, vertical = 2.dp),
              ) {
                Text(
                  text = "${researched.category.badgeIcon} ${researched.category.displayName.uppercase()}",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = categoryTextColor,
                )
              }
            }

            Text(
              text = if (researched.category == AccountCategory.CRYPTO && researched.ticker != null) {
                "Ticker: ${researched.ticker} • Logo kripto online"
              } else if (researched.domain != null) {
                "${researched.domain} • Logo resmi otomatis"
              } else {
                "Ikon visual otomatis terhubung"
              },
              fontSize = 10.sp,
              color = colors.muted,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        Spacer(modifier = Modifier.width(6.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF10B981),
            modifier = Modifier.size(14.dp),
          )
          Text(
            text = "Auto ✓",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF10B981),
          )
        }
      }
    }
  }
}

