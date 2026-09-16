package com.solvix.tabungan

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun DashboardPaymentMethods(
  expenseEntries: List<MoneyEntry>,
  strings: AppStrings,
) {
  val colors = LocalAppColors.current
  val theme = LocalThemeName.current

  val paymentStats = remember(expenseEntries.size, expenseEntries.sumOf { it.amount }) {
    val totalExpense = expenseEntries.sumOf { it.amount }.coerceAtLeast(1)
    expenseEntries
      .groupBy {
        it.channelOrBank.trim().ifBlank {
          it.sourceOrMethod.trim().ifBlank { "Lainnya" }
        }
      }
      .mapValues { (_, entries) ->
        val sum = entries.sumOf { it.amount }
        val count = entries.size
        Triple(sum, count, ((sum.toFloat() / totalExpense) * 100).roundToInt())
      }
      .entries
      .sortedByDescending { it.value.first }
      .take(4)
  }

  // Jika belum ada data pengeluaran, tidak perlu render kartu kosong berlebih
  if (paymentStats.isEmpty()) return

  val maxAmount = remember(paymentStats) {
    (paymentStats.firstOrNull()?.value?.first ?: 1).coerceAtLeast(1)
  }

  AppCard {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SectionTitle(
          icon = themeSectionVisualIcon(theme, "wallet", "💳"),
          title = strings["dashboard_wallet_distribution"],
          modifier = Modifier.weight(1f, fill = false),
          compact = true,
        )
      }

      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        paymentStats.forEachIndexed { index, entry ->
          val channelName = entry.key
          val (amount, count, percentage) = entry.value
          val targetRatio = (amount.toFloat() / maxAmount).coerceIn(0.05f, 1f)
          val animatedRatio by animateFloatAsState(
            targetValue = targetRatio,
            animationSpec = tween(durationMillis = 600 + index * 80, easing = FastOutSlowInEasing),
            label = "channel_ratio_$index",
          )

          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false),
              ) {
                Text(
                  text = channelName,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = colors.text,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = "  ($count)",
                  fontSize = 11.sp,
                  color = colors.muted,
                )
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "$percentage%  ",
                  fontSize = 11.sp,
                  color = colors.muted,
                )
                AnimatedAmountText(
                  amount = amount,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = colors.text,
                )
              }
            }

            // Distribution bar
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(colors.bg2),
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth(animatedRatio)
                  .height(7.dp)
                  .clip(RoundedCornerShape(999.dp))
                  .background(
                    Brush.horizontalGradient(
                      listOf(colors.accent2, colors.accent)
                    )
                  ),
              )
            }
          }
        }
      }
    }
  }
}
