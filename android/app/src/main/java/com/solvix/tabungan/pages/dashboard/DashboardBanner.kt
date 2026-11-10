package com.solvix.tabungan

import android.content.Context
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun DashboardBanner(
  displayName: String,
  todayFormatted: String,
  netBalance: Int,
  totalIncome: Int,
  totalExpense: Int,
  strings: AppStrings,
  locale: Locale,
) {
  val context = LocalContext.current
  val colors = LocalAppColors.current
  val theme = LocalThemeName.current
  val isDark = isDarkTheme(theme)
  val isStandardTheme = theme == ThemeName.StandardLight || theme == ThemeName.StandardDark

  val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
  var isMasked by remember { mutableStateOf(prefs.getBoolean("privacy_eye_masked", false)) }

  val incomeColor = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
  val expenseColor = if (isDark) Color(0xFFFF8A80) else Color(0xFFC62828)

  AppCard {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
      // User Greeting & Date (without emoji, prioritized username)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "${strings["dashboard_greeting"]}, $displayName!",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = colors.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = todayFormatted,
            fontSize = 12.sp,
            color = colors.muted,
            modifier = Modifier.padding(top = 2.dp),
          )
        }

        // Health Status Badge
        val isHealthy = netBalance >= 0
        val badgeBg = if (isHealthy) incomeColor.copy(alpha = 0.15f) else expenseColor.copy(alpha = 0.15f)
        val badgeColor = if (isHealthy) incomeColor else expenseColor
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(badgeBg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        ) {
          Text(
            text = if (isHealthy) "✓ " + strings["dashboard_net_positive"] else "! " + strings["dashboard_net_negative"],
            color = badgeColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      // Net Balance Visual Card with Animated Counter & Formal Privacy Eye
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(AppDimens.radiusMd))
          .background(
            Brush.linearGradient(
              colors = listOf(
                colors.accent.copy(alpha = 0.22f),
                colors.accent2.copy(alpha = 0.12f),
              )
            )
          )
          .border(
            width = 1.dp,
            color = colors.accent.copy(alpha = 0.35f),
            shape = RoundedCornerShape(AppDimens.radiusMd),
          )
          .padding(16.dp),
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          // Balance Label with Formal AccountBalance Icon
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            if (isStandardTheme) {
              Icon(
                imageVector = Icons.Rounded.AccountBalance,
                contentDescription = null,
                tint = colors.muted,
                modifier = Modifier.size(14.dp),
              )
            }
            Text(
              text = strings["dashboard_balance"].uppercase(locale),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = colors.muted,
              letterSpacing = 1.sp,
            )
          }

          // Balance Amount and Privacy Eye (positioned to the right of the amount)
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            AnimatedAmountText(
              amount = netBalance,
              fontSize = 26.sp,
              fontWeight = FontWeight.ExtraBold,
              color = colors.text,
              isMasked = isMasked,
            )

            // Formal Privacy Eye Toggle (placed right beside the number)
            Box(
              modifier = Modifier
                .clip(CircleShape)
                .background(colors.card.copy(alpha = 0.7f))
                .clickable {
                  val next = !isMasked
                  isMasked = next
                  prefs.edit().putBoolean("privacy_eye_masked", next).apply()
                }
                .padding(6.dp),
              contentAlignment = Alignment.Center,
            ) {
              if (isStandardTheme) {
                Icon(
                  imageVector = if (isMasked) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                  contentDescription = "Toggle Privacy",
                  tint = colors.muted,
                  modifier = Modifier.size(18.dp),
                )
              } else {
                Text(
                  text = if (isMasked) "👁️‍🗨️" else "👁️",
                  fontSize = 14.sp,
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          // Income & Expense Quick Summary Pill with Formal Vector Icons & Animated Counters
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            // Income mini-card
            Row(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.card.copy(alpha = 0.85f))
                .padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              if (isStandardTheme) {
                Box(
                  modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(incomeColor.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    imageVector = Icons.Rounded.ArrowUpward,
                    contentDescription = null,
                    tint = incomeColor,
                    modifier = Modifier.size(15.dp),
                  )
                }
              } else {
                Text(text = "↑", fontSize = 16.sp, color = incomeColor, fontWeight = FontWeight.Bold)
              }
              Spacer(modifier = Modifier.width(6.dp))
              Column {
                Text(
                  text = strings["dashboard_income_short"],
                  fontSize = 10.sp,
                  color = colors.muted,
                )
                AnimatedAmountText(
                  amount = totalIncome,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = incomeColor,
                  showPlusSign = true,
                  isMasked = isMasked,
                )
              }
            }

            // Expense mini-card
            Row(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.card.copy(alpha = 0.85f))
                .padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              if (isStandardTheme) {
                Box(
                  modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(expenseColor.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    imageVector = Icons.Rounded.ArrowDownward,
                    contentDescription = null,
                    tint = expenseColor,
                    modifier = Modifier.size(15.dp),
                  )
                }
              } else {
                Text(text = "↓", fontSize = 16.sp, color = expenseColor, fontWeight = FontWeight.Bold)
              }
              Spacer(modifier = Modifier.width(6.dp))
              Column {
                Text(
                  text = strings["dashboard_expense_short"],
                  fontSize = 10.sp,
                  color = colors.muted,
                )
                AnimatedAmountText(
                  amount = totalExpense,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = expenseColor,
                  showMinusSign = true,
                  isMasked = isMasked,
                )
              }
            }
          }
        }
      }
    }
  }
}
