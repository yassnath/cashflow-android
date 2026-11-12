package com.solvix.tabungan.pages.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.Page

/**
 * Floating Action Button (FAB) Speed Dial yang ergonomis di pojok kanan bawah homepage.
 * Memungkinkan pencatatan cepat Pengeluaran, Pemasukan, Transfer, dan Pindai Struk dengan satu tangan.
 */
@Composable
fun DashboardFabSpeedDial(
  onNavigateTo: (Page) -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = LocalAppColors.current
  var expanded by remember { mutableStateOf(false) }

  val rotation by animateFloatAsState(
    targetValue = if (expanded) 45f else 0f,
    animationSpec = tween(durationMillis = 240),
    label = "fab_rotation",
  )

  Box(modifier = modifier) {
    // Backdrop saat Speed Dial terbuka
    if (expanded) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.35f))
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
          ) { expanded = false },
      )
    }

    // Tombol FAB & Menu Opsi Melayang di Kanan Bawah
    Column(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(bottom = 106.dp, end = 16.dp),
      horizontalAlignment = Alignment.End,
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      // Menu Aksi Cepat
      AnimatedVisibility(
        visible = expanded,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
      ) {
        Column(
          horizontalAlignment = Alignment.End,
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          SpeedDialItem(
            label = "Transfer Saldo",
            icon = "🔁",
            badgeColor = Color(0xFF0288D1),
            onClick = {
              expanded = false
              onNavigateTo(Page.Transfer)
            },
          )

          SpeedDialItem(
            label = "Catat Pemasukan",
            icon = "💸",
            badgeColor = Color(0xFF10B981),
            onClick = {
              expanded = false
              onNavigateTo(Page.Income)
            },
          )

          SpeedDialItem(
            label = "Catat Pengeluaran",
            icon = "🧾",
            badgeColor = Color(0xFFEF4444),
            onClick = {
              expanded = false
              onNavigateTo(Page.Expense)
            },
          )
        }
      }

      // Tombol FAB Utama
      Box(
        modifier = Modifier
          .size(54.dp)
          .shadow(8.dp, CircleShape)
          .clip(CircleShape)
          .background(Brush.linearGradient(listOf(colors.accent, colors.accent2)))
          .clickable { expanded = !expanded },
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Rounded.Add,
          contentDescription = "Menu Cepat",
          tint = colors.onGradient,
          modifier = Modifier
            .size(28.dp)
            .rotate(rotation),
        )
      }
    }
  }
}

@Composable
private fun SpeedDialItem(
  label: String,
  icon: String,
  badgeColor: Color,
  onClick: () -> Unit,
) {
  val colors = LocalAppColors.current

  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier.clickable(onClick = onClick),
  ) {
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .background(colors.card)
        .shadow(4.dp, RoundedCornerShape(8.dp))
        .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
      Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = colors.text,
      )
    }

    Box(
      modifier = Modifier
        .size(44.dp)
        .shadow(4.dp, CircleShape)
        .clip(CircleShape)
        .background(badgeColor),
      contentAlignment = Alignment.Center,
    ) {
      Text(text = icon, fontSize = 18.sp)
    }
  }
}

