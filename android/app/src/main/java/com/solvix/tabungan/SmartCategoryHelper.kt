package com.solvix.tabungan

object SmartCategoryHelper {
  private val expenseKeywords = mapOf(
    "Makanan & Minuman" to listOf(
      "makan", "minum", "kopi", "coffee", "nasi", "resto", "restoran", "cafe", "kafe", "snack",
      "jajan", "sarapan", "lunch", "dinner", "ayam", "mie", "bakso", "sate", "pizza", "burger",
      "gofood", "grabfood", "shopeefood", "teh", "boba", "roti", "kue", "martabak"
    ),
    "Transportasi" to listOf(
      "bensin", "pertamax", "pertalite", "solar", "bbm", "gojek", "goride", "gocar", "grab",
      "grabcar", "maxim", "angkot", "bus", "transjakarta", "kereta", "krl", "mrt", "lrt",
      "tol", "parkir", "parking", "ojek", "service motor", "service mobil", "cuci motor", "cuci mobil", "oli"
    ),
    "Belanja" to listOf(
      "belanja", "shopee", "tokopedia", "lazada", "tiktok shop", "baju", "celana", "sepatu",
      "tas", "jaket", "indomaret", "alfamart", "supermarket", "hypermart", "minimarket",
      "skincare", "kosmetik", "parfum", "makeup"
    ),
    "Tagihan & Utilitas" to listOf(
      "listrik", "pln", "token listrik", "air", "pdam", "wifi", "indihome", "biznet", "first media",
      "pulsa", "kuota", "paket data", "telkomsel", "indosat", "xl", "tri", "smartfren",
      "bpjs", "asuransi", "pajak", "iuran", "sampah", "keamanan", "maintenance"
    ),
    "Hiburan & Langganan" to listOf(
      "netflix", "spotify", "youtube", "disney", "prime", "game", "steam", "playstation",
      "bioskop", "cinema", "xxi", "cgv", "nonton", "karaoke", "liburan", "hotel", "tiket pesawat", "staycation"
    ),
    "Kesehatan" to listOf(
      "obat", "dokter", "apotek", "klinik", "rumah sakit", "rs", "vitamin", "suplemen",
      "periksa", "cek darah", "konsultasi", "dentist", "gigi", "terapi", "pijat"
    ),
    "Pendidikan" to listOf(
      "spp", "kursus", "buku", "kuliah", "sekolah", "les", "seminar", "workshop", "ujian", "skripsi", "almamater"
    ),
    "Keluarga & Donasi" to listOf(
      "sedekah", "zakat", "infaq", "donasi", "sumbangan", "kondangan", "kado", "hadiah", "ortu", "orang tua", "anak", "susu"
    ),
  )

  private val incomeKeywords = mapOf(
    "Gaji & Upah" to listOf("gaji", "salary", "upah", "payroll", "honor", "thr", "tunjangan", "overtime", "lembur"),
    "Bonus & Komisi" to listOf("bonus", "komisi", "reward", "hadiah", "tip", "cashback", "affiliate"),
    "Bisnis & Penjualan" to listOf("omset", "omzet", "jual", "penjualan", "laba", "profit", "toko", "proyek", "freelance", "klien", "orderan"),
    "Investasi & Bunga" to listOf("dividen", "bunga", "reksadana", "saham", "crypto", "kripto", "deposito", "imbal hasil", "sewa rumah", "sewa kos"),
    "Hadiah & Hibah" to listOf("transferan", "kiriman", "pemberian", "hadiah", "giveaway", "reimburse", "kembalian")
  )

  fun detectCategory(note: String, type: EntryType): String? {
    if (note.isBlank()) return null
    val lower = note.lowercase()
    val keywordMap = if (type == EntryType.Expense) expenseKeywords else incomeKeywords

    for ((category, keywords) in keywordMap) {
      if (keywords.any { lower.contains(it) }) {
        return category
      }
    }
    return null
  }
}
