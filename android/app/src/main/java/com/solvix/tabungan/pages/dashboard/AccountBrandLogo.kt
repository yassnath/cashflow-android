package com.solvix.tabungan.pages.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.R

@Composable
fun AccountBrandLogo(
  brand: BankWalletBrand,
  modifier: Modifier = Modifier,
  size: Dp = 36.dp,
) {
  val shape = RoundedCornerShape(10.dp)

  // Cek apakah ada logo resmi yang disediakan di drawable-nodpi
  val drawableRes = when (brand) {
    BankWalletBrand.SEABANK -> R.drawable.seabank
    BankWalletBrand.BCA -> R.drawable.bca
    BankWalletBrand.BNI -> R.drawable.bni
    BankWalletBrand.BSI -> R.drawable.bsi
    BankWalletBrand.DANA -> R.drawable.dana
    BankWalletBrand.GOPAY -> R.drawable.gopay
    BankWalletBrand.MANDIRI -> R.drawable.mandiri
    BankWalletBrand.OVO -> R.drawable.ovo
    BankWalletBrand.SHOPEEPAY -> R.drawable.shopeepay
    else -> null
  }

  if (drawableRes != null) {
    Box(
      modifier = modifier
        .size(size)
        .clip(shape)
        .background(Color.White)
        .border(0.8.dp, Color(0xFFE2E8F0), shape),
      contentAlignment = Alignment.Center,
    ) {
      Image(
        painter = painterResource(id = drawableRes),
        contentDescription = brand.name,
        contentScale = ContentScale.Fit,
        modifier = Modifier
          .size(size * 0.85f)
          .clip(RoundedCornerShape(6.dp)),
      )
    }
    return
  }

  // Fallback visual identitas untuk aset yang tidak memiliki file gambar statis
  val brandColor = getBrandColor(brand)
  Box(
    modifier = modifier
      .size(size)
      .clip(shape)
      .background(brandColor),
    contentAlignment = Alignment.Center,
  ) {
    when (brand) {
      BankWalletBrand.CRYPTO -> {
        Text(
          text = "₿",
          color = Color.White,
          fontWeight = FontWeight.Black,
          fontSize = (size.value * 0.52f).sp,
        )
      }

      BankWalletBrand.SAHAM -> {
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
          contentDescription = "Saham",
          tint = Color.White,
          modifier = Modifier.size(size * 0.62f),
        )
      }

      BankWalletBrand.BRI -> {
        Box(contentAlignment = Alignment.Center) {
          Text(
            text = "BRI",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = (size.value * 0.34f).sp,
          )
          Canvas(modifier = Modifier.size(size)) {
            drawCircle(
              color = Color(0xFFF58220),
              radius = this.size.width * 0.12f,
              center = Offset(this.size.width * 0.82f, this.size.height * 0.22f),
            )
          }
        }
      }

      BankWalletBrand.JAGO -> {
        Text(
          text = "jago",
          color = Color.White,
          fontWeight = FontWeight.ExtraBold,
          fontSize = (size.value * 0.29f).sp,
        )
      }

      BankWalletBrand.JENIUS -> {
        Text(
          text = "jenius",
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = (size.value * 0.24f).sp,
        )
      }

      BankWalletBrand.LINKAJA -> {
        Text(
          text = "Link!",
          color = Color.White,
          fontWeight = FontWeight.Black,
          fontSize = (size.value * 0.26f).sp,
        )
      }

      BankWalletBrand.CIMB -> {
        Text(
          text = "CIMB",
          color = Color.White,
          fontWeight = FontWeight.Black,
          fontSize = (size.value * 0.26f).sp,
        )
      }

      BankWalletBrand.PERMATA -> {
        Text(
          text = "P",
          color = Color.White,
          fontWeight = FontWeight.Black,
          fontSize = (size.value * 0.40f).sp,
        )
      }

      BankWalletBrand.CASH -> {
        Icon(
          imageVector = Icons.Rounded.AccountBalanceWallet,
          contentDescription = "Dompet Tunai",
          tint = Color.White,
          modifier = Modifier.size(size * 0.60f),
        )
      }

      else -> {
        Icon(
          imageVector = Icons.Rounded.CreditCard,
          contentDescription = "Akun Bank",
          tint = Color.White,
          modifier = Modifier.size(size * 0.58f),
        )
      }
    }
  }
}
