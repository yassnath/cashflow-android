package com.solvix.tabungan

import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Handles user registration (sign-up) flow against Supabase Auth and database.
 */
fun performSignUp(
  scope: CoroutineScope,
  strings: AppStrings,
  signUpName: String,
  signUpEmail: String,
  signUpCountry: String,
  signUpBirthdate: String,
  signUpBio: String,
  signUpUsername: String,
  signUpPassword: String,
  onAlert: (String) -> Unit,
  onSuccess: (normalizedUsername: String) -> Unit,
) {
  val normalizedUsername = signUpUsername.trim()
  val normalizedEmail = signUpEmail.trim()
  scope.launch(Dispatchers.IO) {
    try {
      val exists = SupabaseRepository.userExists(normalizedUsername, normalizedEmail)
      if (exists) {
        withContext(Dispatchers.Main) {
          onAlert(strings["signup_exists"])
        }
        return@launch
      }
      val authUserId = SupabaseRepository.signUpSupabaseAuth(normalizedEmail, signUpPassword)
      val newUser = SupabaseUser(
        name = signUpName,
        email = normalizedEmail,
        country = signUpCountry,
        bio = signUpBio,
        birthdate = signUpBirthdate,
        createdAt = nowJakartaText(),
        authId = authUserId,
        username = normalizedUsername,
        password = signUpPassword,
      )
      SupabaseRepository.insertUser(newUser)
      runCatching { SupabaseClient.client.auth.signOut() }
      withContext(Dispatchers.Main) {
        onSuccess(normalizedUsername)
      }
    } catch (e: Exception) {
      withContext(Dispatchers.Main) {
        onAlert("Gagal daftar: ${e.localizedMessage ?: "Cek koneksi internet"}")
      }
    }
  }
}
