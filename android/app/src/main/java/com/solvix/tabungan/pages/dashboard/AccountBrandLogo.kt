package com.solvix.tabungan.pages.dashboard

import android.graphics.Bitmap
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AccountCategory
import com.solvix.tabungan.AccountIconManager
import com.solvix.tabungan.R

/**
 * Komponen logo brand rekening / dompet / kripto dengan sistem riset otomatis.
 * Mampu mereset dan memuat icon otomatis dari internet atau cache tanpa perlu file manual di img/drawable.
 */
@Composable
fun AccountBrandLogo(
  accountName: String,
  modifier: Modifier = Modifier,
  size: Dp = 36.dp,
  brand: BankWalletBrand = detectBrand(accountName),
) {
  val context = LocalContext.current
  val shape = RoundedCornerShape(10.dp)
  val researched = remember(accountName) { AccountIconManager.researchAccount(accountName) }

  // 1. Cek apakah ada logo resmi yang disediakan di drawable lokal
  val localDrawable = researched.localDrawableRes ?: when (brand) {
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

  if (localDrawable != null) {
    Box(
      modifier = modifier
        .size(size)
        .clip(shape)
        .background(Color.White)
        .border(0.8.dp, Color(0xFFE2E8F0), shape),
      contentAlignment = Alignment.Center,
    ) {
      Image(
        painter = painterResource(id = localDrawable),
        contentDescription = researched.cleanName,
        contentScale = ContentScale.Fit,
        modifier = Modifier
          .size(size * 0.85f)
          .clip(RoundedCornerShape(6.dp)),
      )
    }
    return
  }

  // 2. Riset otomatis: Ambil dari Cache atau Download Online
  var remoteBitmap by remember(accountName) { mutableStateOf<Bitmap?>(null) }

  LaunchedEffect(accountName) {
    if (accountName.isNotBlank()) {
      remoteBitmap = AccountIconManager.loadIconBitmap(context, accountName)
    }
  }

  if (remoteBitmap != null) {
    Box(
      modifier = modifier
        .size(size)
        .clip(shape)
        .background(Color.White)
        .border(0.8.dp, Color(0xFFE2E8F0), shape),
      contentAlignment = Alignment.Center,
    ) {
      Image(
        bitmap = remoteBitmap!!.asImageBitmap(),
        contentDescription = researched.cleanName,
        contentScale = ContentScale.Fit,
        modifier = Modifier
          .size(size * 0.82f)
          .clip(RoundedCornerShape(6.dp)),
      )
    }
    return
  }

  // 3. Fallback visual identitas untuk aset yang belum selesai diunduh atau offline
  val brandColor = if (researched.category != AccountCategory.OTHER) researched.brandColor else getBrandColor(brand)

  Box(
    modifier = modifier
      .size(size)
      .clip(shape)
      .background(brandColor),
    contentAlignment = Alignment.Center,
  ) {
    when {
      researched.category == AccountCategory.CRYPTO || brand == BankWalletBrand.CRYPTO -> {
        Text(
          text = researched.ticker ?: "₿",
          color = Color.White,
          fontWeight = FontWeight.Black,
          fontSize = if ((researched.ticker?.length ?: 1) > 3) (size.value * 0.30f).sp else (size.value * 0.48f).sp,
        )
      }

      researched.category == AccountCategory.INVESTMENT || brand == BankWalletBrand.SAHAM -> {
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
          contentDescription = "Saham",
          tint = Color.White,
          modifier = Modifier.size(size * 0.62f),
        )
      }

      brand == BankWalletBrand.BRI || accountName.contains("bri", ignoreCase = true) -> {
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

      brand == BankWalletBrand.JAGO || accountName.contains("jago", ignoreCase = true) -> {
        Text(
          text = "jago",
          color = Color.White,
          fontWeight = FontWeight.ExtraBold,
          fontSize = (size.value * 0.29f).sp,
        )
      }

      brand == BankWalletBrand.JENIUS || accountName.contains("jenius", ignoreCase = true) -> {
        Text(
          text = "jenius",
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = (size.value * 0.24f).sp,
        )
      }

      brand == BankWalletBrand.LINKAJA || accountName.contains("linkaja", ignoreCase = true) -> {
        Text(
          text = "Link!",
          color = Color.White,
          fontWeight = FontWeight.Black,
          fontSize = (size.value * 0.26f).sp,
        )
      }

      brand == BankWalletBrand.CIMB || accountName.contains("cimb", ignoreCase = true) -> {
        Text(
          text = "CIMB",
          color = Color.White,
          fontWeight = FontWeight.Black,
          fontSize = (size.value * 0.26f).sp,
        )
      }

      brand == BankWalletBrand.PERMATA || accountName.contains("permata", ignoreCase = true) -> {
        Text(
          text = "P",
          color = Color.White,
          fontWeight = FontWeight.Black,
          fontSize = (size.value * 0.40f).sp,
        )
      }

      researched.category == AccountCategory.CASH || brand == BankWalletBrand.CASH -> {
        Icon(
          imageVector = Icons.Rounded.AccountBalanceWallet,
          contentDescription = "Dompet Tunai",
          tint = Color.White,
          modifier = Modifier.size(size * 0.60f),
        )
      }

      researched.category == AccountCategory.BANK -> {
        // Tampilkan inisial bank jika tidak ada logo khusus
        val initials = researched.cleanName.split(" ")
          .filter { it.isNotBlank() }
          .take(2)
          .map { it.first().uppercase() }
          .joinToString("")
        Text(
          text = initials.ifBlank { "BK" },
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = (size.value * 0.36f).sp,
        )
      }

      researched.category == AccountCategory.EWALLET -> {
        val initials = researched.cleanName.take(3).uppercase()
        Text(
          text = initials.ifBlank { "WAL" },
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = (size.value * 0.32f).sp,
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

/**
 * Overload kompatibilitas untuk pemanggil lama yang menggunakan BankWalletBrand
 */
@Composable
fun AccountBrandLogo(
  brand: BankWalletBrand,
  modifier: Modifier = Modifier,
  size: Dp = 36.dp,
) {
  val defaultName = when (brand) {
    BankWalletBrand.SEABANK -> "SeaBank"
    BankWalletBrand.BCA -> "Bank BCA"
    BankWalletBrand.BNI -> "Bank BNI"
    BankWalletBrand.BRI -> "Bank BRI"
    BankWalletBrand.BSI -> "Bank BSI"
    BankWalletBrand.MANDIRI -> "Bank Mandiri"
    BankWalletBrand.JAGO -> "Bank Jago"
    BankWalletBrand.JENIUS -> "Jenius"
    BankWalletBrand.CIMB -> "CIMB Niaga"
    BankWalletBrand.PERMATA -> "Bank Permata"
    BankWalletBrand.GOPAY -> "GoPay"
    BankWalletBrand.OVO -> "OVO"
    BankWalletBrand.DANA -> "DANA"
    BankWalletBrand.SHOPEEPAY -> "ShopeePay"
    BankWalletBrand.LINKAJA -> "LinkAja"
    BankWalletBrand.CRYPTO -> "Crypto"
    BankWalletBrand.SAHAM -> "Investasi / Saham"
    BankWalletBrand.CASH -> "Cash"
    BankWalletBrand.OTHER -> "Other"
  }
  AccountBrandLogo(
    accountName = defaultName,
    modifier = modifier,
    size = size,
    brand = brand,
  )
}
