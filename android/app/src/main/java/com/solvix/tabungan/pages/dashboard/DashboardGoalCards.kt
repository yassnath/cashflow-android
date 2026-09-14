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
fun DashboardGoalCards(
  dreamEntries: List<DreamEntry>,
  strings: AppStrings,
  onNavigateTo: (Page) -> Unit,
) {
  val colors = LocalAppColors.current
  val theme = LocalThemeName.current

  AppCard {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SectionTitle(
          icon = themePageIcon(theme, Page.Dreams),
          title = strings["dashboard_goal_progress"],
        )
        ChipButton(
          text = strings["dashboard_see_all"],
          onClick = { onNavigateTo(Page.Dreams) },
        )
      }

      if (dreamEntries.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(text = strings["dashboard_no_goals"], color = colors.muted, fontSize = 13.sp)
        }
      } else {
        val displayedGoals = dreamEntries.take(3)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          displayedGoals.forEachIndexed { index, goal ->
            val targetSafe = goal.target.coerceAtLeast(1)
            val ratio = (goal.current.toFloat() / targetSafe).coerceIn(0f, 1f)
            val percent = (ratio * 100).roundToInt()
            val animatedGoalRatio by animateFloatAsState(
              targetValue = ratio,
              animationSpec = tween(durationMillis = 650 + index * 100, easing = FastOutSlowInEasing),
              label = "goal_ratio_$index",
            )

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppDimens.radiusSm))
                .background(colors.bg2.copy(alpha = 0.5f))
                .padding(12.dp),
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = goal.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                  )
                  Text(
                    text = "$percent%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = colors.accent,
                  )
                }

                // Progress Bar
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(colors.card),
                ) {
                  Box(
                    modifier = Modifier
                      .fillMaxWidth(animatedGoalRatio)
                      .height(7.dp)
                      .clip(RoundedCornerShape(999.dp))
                      .background(
                        Brush.horizontalGradient(
                          listOf(colors.accent, colors.accent2)
                        )
                      ),
                  )
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Text(
                    text = formatRupiah(goal.current),
                    fontSize = 11.sp,
                    color = colors.muted,
                  )
                  Text(
                    text = formatRupiah(goal.target),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text,
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
