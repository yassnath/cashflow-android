package com.solvix.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppCard
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.MiniStat
import com.solvix.tabungan.SectionTitle
import com.solvix.tabungan.SupabaseUser
import com.solvix.tabungan.formatCreatedAt
import com.solvix.tabungan.parseCreatedAtMillis

@Composable
fun AdminDashboard(
  users: List<SupabaseUser>,
  strings: AppStrings,
) {
  val colors = LocalAppColors.current
  val totalUsers = users.size
  val palette = listOf(
    colors.accent,
    colors.accent2,
    colors.danger,
    androidx.compose.ui.graphics.Color(0xFF4DABF7),
    androidx.compose.ui.graphics.Color(0xFFFFC857),
    androidx.compose.ui.graphics.Color(0xFF9D4EDD),
    androidx.compose.ui.graphics.Color(0xFF00B4D8),
  )
  val recencyPalette = listOf(
    colors.accent2,
    androidx.compose.ui.graphics.Color(0xFF4DABF7),
    colors.accent,
    androidx.compose.ui.graphics.Color(0xFFFFC857),
    colors.danger,
  )
  val countrySlices = buildCountrySlices(users, strings, palette)
  val recencySlices = buildRecencySlices(users, strings, recencyPalette)
  val topCountry = countrySlices.maxByOrNull { it.value }?.label ?: "-"
  val latestSignup = users.maxByOrNull { parseCreatedAtMillis(it.createdAt) ?: 0L }
    ?.let { formatCreatedAt(it.createdAt) }
    ?: "-"

  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    SectionTitle(
      icon = "🛡️",
      title = strings["admin_dashboard_title"],
      subtitle = strings["admin_dashboard_subtitle"],
    )
    AppCard {
      Text(text = strings["admin_overview"], fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.text)
      Spacer(modifier = Modifier.height(10.dp))
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MiniStat(label = strings["admin_total_users"], value = totalUsers.toString())
        MiniStat(label = strings["admin_top_country"], value = topCountry)
        MiniStat(label = strings["admin_latest_signup"], value = latestSignup)
      }
    }
    AppCard {
      Text(text = strings["admin_chart_country"], fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.text)
      Spacer(modifier = Modifier.height(12.dp))
      if (countrySlices.isEmpty()) {
        Text(text = strings["admin_no_users"], color = colors.muted, fontSize = 12.sp)
      } else {
        DonutChart(
          slices = countrySlices,
          centerLabel = totalUsers.toString(),
          centerSubLabel = strings["admin_total_users"],
          modifier = Modifier
            .size(200.dp)
            .align(Alignment.CenterHorizontally),
        )
        Spacer(modifier = Modifier.height(12.dp))
        DonutLegend(slices = countrySlices)
      }
    }
    AppCard {
      Text(text = strings["admin_chart_recency"], fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.text)
      Spacer(modifier = Modifier.height(12.dp))
      if (recencySlices.isEmpty()) {
        Text(text = strings["admin_no_users"], color = colors.muted, fontSize = 12.sp)
      } else {
        DonutChart(
          slices = recencySlices,
          centerLabel = totalUsers.toString(),
          centerSubLabel = strings["admin_total_users"],
          modifier = Modifier
            .size(200.dp)
            .align(Alignment.CenterHorizontally),
        )
        Spacer(modifier = Modifier.height(12.dp))
        DonutLegend(slices = recencySlices)
      }
    }
    AppCard {
      Text(text = strings["admin_users_title"], fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.text)
      Spacer(modifier = Modifier.height(8.dp))
      if (users.isEmpty()) {
        Text(text = strings["admin_no_users"], color = colors.muted, fontSize = 12.sp)
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          users.forEach { user ->
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.bg2)
                .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                .padding(12.dp),
            ) {
              Text(text = user.name, fontWeight = FontWeight.Bold)
              Text(text = user.email, color = colors.muted, fontSize = 12.sp)
              Text(text = user.country, color = colors.muted, fontSize = 12.sp)
              Text(text = formatCreatedAt(user.createdAt), color = colors.muted, fontSize = 12.sp)
            }
          }
        }
      }
    }
  }
}
