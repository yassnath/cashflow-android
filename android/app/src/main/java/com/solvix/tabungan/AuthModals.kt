package com.solvix.tabungan

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AuthTab { SignIn, SignUp }
enum class LoadingTarget { Startup, Logout }

@Composable
fun ModalOverlay(content: @Composable () -> Unit) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xB20A0A14))
      .padding(horizontal = 16.dp),
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier = Modifier
        .matchParentSize()
        .clickable(enabled = true, onClick = {}),
    )
    content()
  }
}

@Composable
fun LoadingLogo() {
  val strings = LocalStrings.current
  val transition = rememberInfiniteTransition(label = "loading")
  val scale by transition.animateFloat(
    initialValue = 0.96f,
    targetValue = 1.04f,
    animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
    label = "loading-scale",
  )
  Box(modifier = Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier.align(Alignment.Center),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        painter = painterResource(id = R.drawable.logo2),
        contentDescription = strings["logo_alt"],
        modifier = Modifier
          .size(96.dp)
          .graphicsLayer(scaleX = scale, scaleY = scale),
        contentScale = ContentScale.Fit,
      )
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "CashFlow by Solvix Studio",
        color = Color(0xFF9CA3AF),
        fontSize = 12.sp,
      )
    }
  }
}

@Composable
fun LogoCircle() {
  val strings = LocalStrings.current
  val colors = LocalAppColors.current
  Box(
    modifier = Modifier
      .size(120.dp)
      .clip(RoundedCornerShape(20.dp))
      .background(colors.card)
      .shadow(16.dp, RoundedCornerShape(20.dp)),
    contentAlignment = Alignment.Center,
  ) {
    Image(
      painter = painterResource(id = R.drawable.logo2),
      contentDescription = strings["logo_alt"],
      modifier = Modifier.size(96.dp),
      contentScale = ContentScale.Fit,
    )
  }
}

@Composable
fun RowScope.AuthTabButton(text: String, active: Boolean, onClick: () -> Unit) {
  val colors = LocalAppColors.current
  val background = if (active) {
    Brush.linearGradient(listOf(colors.accent, colors.accent2))
  } else {
    Brush.linearGradient(listOf(colors.bg2, colors.bg2))
  }
  Box(
    modifier = Modifier
      .weight(1f)
      .clip(RoundedCornerShape(12.dp))
      .background(background)
      .clickable(onClick = onClick)
      .padding(vertical = 10.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = text,
      fontWeight = FontWeight.Bold,
      color = if (active) Color.White else colors.text,
    )
  }
}

fun clearAuthFields(
  onSignInUsername: (String) -> Unit,
  onSignInPassword: (String) -> Unit,
  onSignUpName: (String) -> Unit,
  onSignUpEmail: (String) -> Unit,
  onSignUpCountry: (String) -> Unit,
  onSignUpBirthdate: (String) -> Unit,
  onSignUpBio: (String) -> Unit,
  onSignUpUsername: (String) -> Unit,
  onSignUpPassword: (String) -> Unit,
) {
  onSignInUsername("")
  onSignInPassword("")
  onSignUpName("")
  onSignUpEmail("")
  onSignUpCountry("")
  onSignUpBirthdate("")
  onSignUpBio("")
  onSignUpUsername("")
  onSignUpPassword("")
}
