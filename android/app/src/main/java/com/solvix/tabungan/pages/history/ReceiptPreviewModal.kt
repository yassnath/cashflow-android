package com.solvix.tabungan

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

import java.io.File

@Composable
fun ReceiptPreviewModal(
  receiptUri: String,
  onDismiss: () -> Unit,
  strings: AppStrings = LocalStrings.current,
) {
  val context = LocalContext.current
  val colors = LocalAppColors.current

  val bitmapState = produceState<ImageBitmap?>(initialValue = null, key1 = receiptUri) {
    val bmp = ReceiptStorageHelper.loadBitmap(context, receiptUri)
    value = bmp?.asImageBitmap()
  }
  val bitmap = bitmapState.value

  Dialog(onDismissRequest = onDismiss) {
    Box(
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .clip(RoundedCornerShape(20.dp))
        .background(colors.card)
        .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
        .padding(16.dp),
    ) {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(text = strings["receipt_preview_title"].ifBlank { "Foto Bukti Transaksi" }, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.text)
          IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
            Icon(imageVector = Icons.Rounded.Close, contentDescription = strings["close"], tint = colors.muted)
          }
        }

        if (bitmap != null) {
          Image(
            bitmap = bitmap,
            contentDescription = strings["receipt_preview_title"],
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 360.dp)
              .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit,
          )
        } else {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(colors.bg2),
            contentAlignment = Alignment.Center,
          ) {
            Text(text = strings["receipt_preview_error"].ifBlank { "Gagal memuat pratinjau gambar" }, color = colors.muted, fontSize = 12.sp)
          }
        }

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.accent)
            .clickable { onDismiss() },
          contentAlignment = Alignment.Center,
        ) {
          Text(text = strings["close"].ifBlank { "Tutup" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
      }
    }
  }
}
