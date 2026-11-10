package com.solvix.tabungan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountIconManagerTest {

  @Test
  fun testResearchCrypto() {
    val sol = AccountIconManager.researchAccount("Solana")
    assertEquals(AccountCategory.CRYPTO, sol.category)
    assertEquals("SOL", sol.ticker)
    assertNotNull(sol.remoteIconUrl)
    assertTrue(sol.remoteIconUrl!!.contains("sol@2x.png"))

    val btc = AccountIconManager.researchAccount("Bitcoin (BTC)")
    assertEquals(AccountCategory.CRYPTO, btc.category)
    assertNotNull(btc.remoteIconUrl)

    val doge = AccountIconManager.researchAccount("doge")
    assertEquals(AccountCategory.CRYPTO, doge.category)
    assertEquals("DOGE", doge.ticker)

    val indodax = AccountIconManager.researchAccount("Indodax")
    assertEquals(AccountCategory.CRYPTO, indodax.category)
    assertEquals("indodax.com", indodax.domain)
  }

  @Test
  fun testResearchBanks() {
    val neo = AccountIconManager.researchAccount("Bank Neo")
    assertEquals(AccountCategory.BANK, neo.category)
    assertEquals("bankneocommerce.co.id", neo.domain)
    assertTrue(neo.remoteIconUrl!!.contains("bankneocommerce.co.id"))

    val bca = AccountIconManager.researchAccount("Bank BCA")
    assertEquals(AccountCategory.BANK, bca.category)
    assertNotNull(bca.localDrawableRes)

    val jatim = AccountIconManager.researchAccount("Bank Jatim")
    assertEquals(AccountCategory.BANK, jatim.category)
    assertEquals("bankjatim.co.id", jatim.domain)

    val dynamicBank = AccountIconManager.researchAccount("Bank Maluku")
    assertEquals(AccountCategory.BANK, dynamicBank.category)
    assertTrue(dynamicBank.domain!!.contains("bankmaluku"))
  }

  @Test
  fun testResearchEWallet() {
    val gopay = AccountIconManager.researchAccount("GoPay")
    assertEquals(AccountCategory.EWALLET, gopay.category)
    assertNotNull(gopay.localDrawableRes)

    val linkaja = AccountIconManager.researchAccount("LinkAja")
    assertEquals(AccountCategory.EWALLET, linkaja.category)
    assertEquals("linkaja.id", linkaja.domain)

    val paypal = AccountIconManager.researchAccount("PayPal")
    assertEquals(AccountCategory.EWALLET, paypal.category)
    assertEquals("paypal.com", paypal.domain)
  }

  @Test
  fun testResearchInvestmentAndCash() {
    val bibit = AccountIconManager.researchAccount("Bibit")
    assertEquals(AccountCategory.INVESTMENT, bibit.category)

    val cash = AccountIconManager.researchAccount("Uang Tunai (Cash)")
    assertEquals(AccountCategory.CASH, cash.category)
  }

  @Test
  fun testEmptyAndBlankSafe() {
    val empty = AccountIconManager.researchAccount("")
    assertEquals(AccountCategory.OTHER, empty.category)

    val blank = AccountIconManager.researchAccount("   ")
    assertEquals(AccountCategory.OTHER, blank.category)
  }
}

