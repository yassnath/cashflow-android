package com.solvix.tabungan

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordSecurity {
  private const val ALGO = "PBKDF2WithHmacSHA256"
  private const val PREFIX = "pbkdf2_sha256"
  private const val ITERATIONS = 120_000
  private const val SALT_BYTES = 16
  private const val KEY_LENGTH_BITS = 256
  private val encoder = Base64.getEncoder()
  private val decoder = Base64.getDecoder()

  enum class ValidationResult {
    Valid,
    TooShort,
    MissingUppercase,
    MissingLowercase,
    MissingDigit,
  }

  fun validatePassword(rawPassword: String): ValidationResult {
    if (rawPassword.length < 8) return ValidationResult.TooShort
    if (!rawPassword.any { it.isUpperCase() }) return ValidationResult.MissingUppercase
    if (!rawPassword.any { it.isLowerCase() }) return ValidationResult.MissingLowercase
    if (!rawPassword.any { it.isDigit() }) return ValidationResult.MissingDigit
    return ValidationResult.Valid
  }

  fun hashPassword(rawPassword: String): String {
    val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
    val hash = pbkdf2(rawPassword, salt, ITERATIONS, KEY_LENGTH_BITS)
    val saltText = encoder.encodeToString(salt)
    val hashText = encoder.encodeToString(hash)
    return "$PREFIX\$$ITERATIONS\$$saltText\$$hashText"
  }

  fun verifyPassword(rawPassword: String, storedValue: String): Boolean {
    if (storedValue.isBlank()) return false
    if (!isHashed(storedValue)) {
      return rawPassword == storedValue
    }
    val parts = storedValue.split("$")
    if (parts.size != 4) return false
    val iterations = parts[1].toIntOrNull() ?: return false
    val salt = runCatching { decoder.decode(parts[2]) }.getOrNull() ?: return false
    val expected = runCatching { decoder.decode(parts[3]) }.getOrNull() ?: return false
    val candidate = pbkdf2(rawPassword, salt, iterations, expected.size * 8)
    return constantTimeEquals(candidate, expected)
  }

  fun isHashed(storedValue: String): Boolean = storedValue.startsWith("$PREFIX$")

  private fun pbkdf2(rawPassword: String, salt: ByteArray, iterations: Int, keyLengthBits: Int): ByteArray {
    val spec = PBEKeySpec(rawPassword.toCharArray(), salt, iterations, keyLengthBits)
    return SecretKeyFactory.getInstance(ALGO).generateSecret(spec).encoded
  }

  private fun constantTimeEquals(left: ByteArray, right: ByteArray): Boolean {
    if (left.size != right.size) return false
    var diff = 0
    for (i in left.indices) {
      diff = diff or (left[i].toInt() xor right[i].toInt())
    }
    return diff == 0
  }
}
