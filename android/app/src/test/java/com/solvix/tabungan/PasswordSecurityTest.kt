package com.solvix.tabungan

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class PasswordSecurityTest {

  @Test
  fun hashProducesExpectedPrefixAndVerifies() {
    val raw = "Sup3rSecurePass!"
    val hash = PasswordSecurity.hashPassword(raw)

    assertTrue(hash.startsWith("pbkdf2_sha256$"))
    assertFalse(hash.contains(raw))
    assertTrue(PasswordSecurity.verifyPassword(raw, hash))
  }

  @Test
  fun verifyRejectsWrongPasswordForHash() {
    val hash = PasswordSecurity.hashPassword("MySecret123")

    assertFalse(PasswordSecurity.verifyPassword("WrongSecret123", hash))
  }

  @Test
  fun verifySupportsLegacyPlaintextForMigration() {
    val legacyStored = "legacy-password"

    assertTrue(PasswordSecurity.verifyPassword("legacy-password", legacyStored))
    assertFalse(PasswordSecurity.isHashed(legacyStored))
  }

  @Test
  fun validatePasswordRules() {
    assertEquals(
      PasswordSecurity.ValidationResult.TooShort,
      PasswordSecurity.validatePassword("Ab1"),
    )
    assertEquals(
      PasswordSecurity.ValidationResult.MissingUppercase,
      PasswordSecurity.validatePassword("lowercase1"),
    )
    assertEquals(
      PasswordSecurity.ValidationResult.MissingLowercase,
      PasswordSecurity.validatePassword("UPPERCASE1"),
    )
    assertEquals(
      PasswordSecurity.ValidationResult.MissingDigit,
      PasswordSecurity.validatePassword("NoDigitPass"),
    )
    assertEquals(
      PasswordSecurity.ValidationResult.Valid,
      PasswordSecurity.validatePassword("ValidPass123"),
    )
  }
}
