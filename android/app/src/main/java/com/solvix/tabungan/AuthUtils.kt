package com.solvix.tabungan

enum class SignInFailureKind {
  Policy,
  Network,
  Credentials,
  Unknown,
}

class SignInFlowException(
  val kind: SignInFailureKind,
  cause: Throwable? = null,
) : RuntimeException(cause)

fun classifySignInFailure(error: Throwable?): SignInFailureKind {
  val lowered = generateSequence(error) { it.cause }
    .joinToString(" | ") { it.message.orEmpty() }
    .lowercase()
  return when {
    lowered.contains("lookup_user_by_username") ->
      SignInFailureKind.Policy

    lowered.contains("function") && lowered.contains("does not exist") ->
      SignInFailureKind.Policy

    lowered.contains("42501") ||
      lowered.contains("42883") ||
      lowered.contains("permission denied") ||
      lowered.contains("row-level security") ||
      lowered.contains("rls") ->
      SignInFailureKind.Policy

    lowered.contains("timeout") ||
      lowered.contains("unable to resolve host") ||
      lowered.contains("failed to connect") ||
      lowered.contains("network is unreachable") ||
      lowered.contains("connection reset") ->
      SignInFailureKind.Network

    lowered.contains("invalid login credentials") ||
      lowered.contains("invalid_credentials") ||
      lowered.contains("email not confirmed") ->
      SignInFailureKind.Credentials

    else -> SignInFailureKind.Unknown
  }
}
