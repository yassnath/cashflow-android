package com.solvix.tabungan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppPinSecurityManagerTest {

  @Test
  fun hashWithSalt_producesDeterministicSha256Hex() {
    val pin = "123456"
    val salt = "test-salt-12345"
    val hash1 = AppPinSecurityManager.hashWithSalt(pin, salt)
    val hash2 = AppPinSecurityManager.hashWithSalt(pin, salt)

    assertEquals(hash1, hash2)
    assertEquals(64, hash1.length) // SHA-256 produces 64 hex characters
    assertTrue(hash1.all { it in "0123456789abcdef" })
  }

  @Test
  fun hashWithSalt_differentPinProducesDifferentHash() {
    val salt = "static-salt"
    val hashA = AppPinSecurityManager.hashWithSalt("111222", salt)
    val hashB = AppPinSecurityManager.hashWithSalt("111223", salt)

    assertNotEquals(hashA, hashB)
  }

  @Test
  fun hashWithSalt_differentSaltProducesDifferentHash() {
    val pin = "654321"
    val hashA = AppPinSecurityManager.hashWithSalt(pin, "salt-alpha")
    val hashB = AppPinSecurityManager.hashWithSalt(pin, "salt-beta")

    assertNotEquals(hashA, hashB)
  }
}

