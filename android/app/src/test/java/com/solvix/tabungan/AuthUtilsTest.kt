package com.solvix.tabungan

import org.junit.Assert.assertEquals
import org.junit.Test

class AuthUtilsTest {

  @Test
  fun classifyPolicyErrorFromRlsMessage() {
    val kind = classifySignInFailure(IllegalStateException("permission denied for table users"))
    assertEquals(SignInFailureKind.Policy, kind)
  }

  @Test
  fun classifyPolicyErrorFromMissingFunctionCode() {
    val kind = classifySignInFailure(RuntimeException("ERROR: 42883 function lookup_user_by_username does not exist"))
    assertEquals(SignInFailureKind.Policy, kind)
  }

  @Test
  fun classifyNetworkError() {
    val kind = classifySignInFailure(RuntimeException("Unable to resolve host idxosoeqtsncwyjwsxeb.supabase.co"))
    assertEquals(SignInFailureKind.Network, kind)
  }

  @Test
  fun classifyCredentialsError() {
    val kind = classifySignInFailure(RuntimeException("invalid login credentials"))
    assertEquals(SignInFailureKind.Credentials, kind)
  }

  @Test
  fun classifyUnknownError() {
    val kind = classifySignInFailure(RuntimeException("something odd happened"))
    assertEquals(SignInFailureKind.Unknown, kind)
  }
}
