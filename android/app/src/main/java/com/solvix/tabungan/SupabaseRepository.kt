package com.solvix.tabungan

import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

fun SupabaseUser.toUserProfile(): UserProfile {
  return UserProfile(
    id = id,
    authId = authId,
    name = name,
    email = email,
    country = country,
    birthdate = toUiDate(birthdate),
    bio = bio.orEmpty(),
    createdAt = createdAt,
    username = username,
    password = "",
  )
}

object SupabaseRepository {

  suspend fun fetchUserByUsername(username: String): SupabaseUser? {
    val normalized = username.trim()
    if (normalized.isBlank()) return null

    val errors = mutableListOf<Throwable>()
    val directHit = try {
      SupabaseClient.client
        .from("users")
        .select {
          filter {
            eq("username", normalized)
          }
          limit(1)
        }
        .decodeList<SupabaseUser>()
        .firstOrNull()
    } catch (e: Throwable) {
      errors.add(e)
      null
    }
    if (directHit != null) return directHit

    val rpcHit = try {
      SupabaseClient.client.postgrest.rpc(
        function = "lookup_user_by_username",
        parameters = buildJsonObject {
          put("p_username", normalized)
        },
      ).decodeSingleOrNull<SupabaseUser>()
    } catch (e: Throwable) {
      errors.add(e)
      null
    }

    if (rpcHit != null) return rpcHit
    if (errors.isNotEmpty()) {
      val kind = when {
        errors.any { classifySignInFailure(it) == SignInFailureKind.Policy } -> SignInFailureKind.Policy
        errors.any { classifySignInFailure(it) == SignInFailureKind.Network } -> SignInFailureKind.Network
        errors.any { classifySignInFailure(it) == SignInFailureKind.Credentials } -> SignInFailureKind.Credentials
        else -> SignInFailureKind.Unknown
      }
      throw SignInFlowException(kind, errors.first())
    }
    return null
  }

  suspend fun fetchUserByEmail(email: String): SupabaseUser? {
    val normalized = email.trim()
    if (normalized.isBlank()) return null

    val errors = mutableListOf<Throwable>()
    val directHit = try {
      SupabaseClient.client
        .from("users")
        .select {
          filter {
            eq("email", normalized)
          }
          limit(1)
        }
        .decodeList<SupabaseUser>()
        .firstOrNull()
    } catch (e: Throwable) {
      errors.add(e)
      null
    }
    if (directHit != null) return directHit

    val rpcHit = try {
      SupabaseClient.client.postgrest.rpc(
        function = "lookup_user_by_email",
        parameters = buildJsonObject {
          put("p_email", normalized)
        },
      ).decodeSingleOrNull<SupabaseUser>()
    } catch (e: Throwable) {
      errors.add(e)
      null
    }

    if (rpcHit != null) return rpcHit
    if (errors.isNotEmpty()) {
      val kind = when {
        errors.any { classifySignInFailure(it) == SignInFailureKind.Policy } -> SignInFailureKind.Policy
        errors.any { classifySignInFailure(it) == SignInFailureKind.Network } -> SignInFailureKind.Network
        errors.any { classifySignInFailure(it) == SignInFailureKind.Credentials } -> SignInFailureKind.Credentials
        else -> SignInFailureKind.Unknown
      }
      throw SignInFlowException(kind, errors.first())
    }
    return null
  }

  suspend fun fetchUserById(userId: String): SupabaseUser? {
    if (userId.isBlank()) return null
    val response = SupabaseClient.client
      .from("users")
      .select {
        filter {
          eq("id", userId)
        }
        limit(1)
      }
      .decodeList<SupabaseUser>()
    return response.firstOrNull()
  }

  suspend fun fetchUserByAuthId(authId: String): SupabaseUser? {
    if (authId.isBlank()) return null
    val response = SupabaseClient.client
      .from("users")
      .select {
        filter {
          eq("auth_id", authId)
        }
        limit(1)
      }
      .decodeList<SupabaseUser>()
    return response.firstOrNull()
  }

  suspend fun updateUserAuthId(userId: String, authId: String) {
    if (userId.isBlank() || authId.isBlank()) return
    SupabaseClient.client
      .from("users")
      .update(
        buildJsonObject {
          put("auth_id", authId)
        },
      ) {
        filter { eq("id", userId) }
      }
  }

  suspend fun signInSupabaseAuth(email: String, password: String): String {
    SupabaseClient.client.auth.awaitInitialization()
    SupabaseClient.client.auth.signInWith(Email) {
      this.email = email
      this.password = password
    }
    val session = SupabaseClient.client.auth.currentSessionOrNull()
    return session?.user?.id ?: SupabaseClient.client.auth.retrieveUserForCurrentSession().id
  }

  suspend fun signUpSupabaseAuth(email: String, password: String): String {
    SupabaseClient.client.auth.awaitInitialization()
    val signUpResult = SupabaseClient.client.auth.signUpWith(Email) {
      this.email = email
      this.password = password
    }
    val authUserIdFromResult = signUpResult?.id.orEmpty()
    if (authUserIdFromResult.isNotBlank()) return authUserIdFromResult
    val session = SupabaseClient.client.auth.currentSessionOrNull()
    return session?.user?.id ?: SupabaseClient.client.auth.retrieveUserForCurrentSession().id
  }

  suspend fun updateUserPasswordHash(userId: String, passwordHash: String) {
    if (userId.isBlank()) return
    SupabaseClient.client
      .from("users")
      .update(
        buildJsonObject {
          put("password", passwordHash)
        },
      ) {
        filter { eq("id", userId) }
      }
  }

  suspend fun authenticateUser(usernameOrEmail: String, rawPassword: String): UserProfile? {
    val normalized = usernameOrEmail.trim()
    if (normalized.isBlank()) return null

    val isEmail = normalized.contains("@")
    val userLookupResult = runCatching {
      if (isEmail) fetchUserByEmail(normalized) else fetchUserByUsername(normalized)
    }
    if (userLookupResult.isFailure) {
      val lookupError = userLookupResult.exceptionOrNull()
      throw SignInFlowException(classifySignInFailure(lookupError), lookupError)
    }

    val user = userLookupResult.getOrNull() ?: return null

    // 1. Check legacy/table password match
    val legacyPasswordMatch = user.password.isNotBlank() && PasswordSecurity.verifyPassword(rawPassword, user.password)

    // 2. Try Supabase Auth sign in
    var activeAuthId = ""
    if (user.email.isNotBlank()) {
      activeAuthId = runCatching { signInSupabaseAuth(user.email, rawPassword) }.getOrNull().orEmpty()
    }

    // 3. Validation: Must match either Supabase Auth OR legacy password
    if (activeAuthId.isBlank() && !legacyPasswordMatch) {
      return null
    }

    // 4. If legacy password matched but Supabase Auth wasn't logged in, establish Supabase Auth session
    if (activeAuthId.isBlank() && legacyPasswordMatch && user.email.isNotBlank()) {
      activeAuthId = runCatching { signUpSupabaseAuth(user.email, rawPassword) }.getOrNull().orEmpty()
      if (activeAuthId.isBlank()) {
        activeAuthId = runCatching { signInSupabaseAuth(user.email, rawPassword) }.getOrNull().orEmpty()
      }
    }

    // 5. Update auth_id in users table if needed to ensure RLS policies work
    val effectiveAuthId = activeAuthId.ifBlank { user.authId }
    if (activeAuthId.isNotBlank() && !activeAuthId.equals(user.authId, ignoreCase = true)) {
      runCatching { updateUserAuthId(user.id, activeAuthId) }
    }

    // 6. Migrate un-hashed password if needed
    if (legacyPasswordMatch && !PasswordSecurity.isHashed(user.password)) {
      runCatching { updateUserPasswordHash(user.id, PasswordSecurity.hashPassword(rawPassword)) }
    }

    return user.copy(authId = effectiveAuthId).toUserProfile()
  }

  suspend fun userExists(username: String, email: String): Boolean {
    val usernameHit = fetchUserByUsername(username) != null
    if (usernameHit) return true
    if (email.isBlank()) return false
    return fetchUserByEmail(email) != null
  }

  suspend fun insertUser(user: SupabaseUser) {
    val passwordHash = when {
      user.password.isBlank() -> ""
      PasswordSecurity.isHashed(user.password) -> user.password
      else -> PasswordSecurity.hashPassword(user.password)
    }
    SupabaseClient.client
      .from("users")
      .insert(
        buildJsonObject {
          put("name", user.name)
          put("email", user.email)
          put("country", user.country)
          put("bio", user.bio)
          put("birthdate", toUiDate(user.birthdate))
          if (user.createdAt.isNotBlank()) {
            put("created_at", user.createdAt)
          }
          put("auth_id", user.authId)
          put("username", user.username)
          put("password", passwordHash)
        },
      )
  }

  suspend fun updateUserProfile(user: UserProfile) {
    val hasPasswordInput = user.password.isNotBlank()
    val passwordHash = if (hasPasswordInput) {
      if (PasswordSecurity.isHashed(user.password)) user.password else PasswordSecurity.hashPassword(user.password)
    } else {
      ""
    }
    SupabaseClient.client
      .from("users")
      .update(
        buildJsonObject {
          put("name", user.name)
          put("email", user.email)
          put("country", user.country)
          put("bio", user.bio)
          put("birthdate", toUiDate(user.birthdate))
          if (user.authId.isNotBlank()) {
            put("auth_id", user.authId)
          }
          put("username", user.username)
          if (hasPasswordInput) {
            put("password", passwordHash)
          }
        },
      ) {
        filter { eq("id", user.id) }
      }
  }

  suspend fun fetchAllUsers(): List<SupabaseUser> {
    val users = SupabaseClient.client
      .from("users")
      .select()
      .decodeList<SupabaseUser>()
    return users.sortedByDescending { parseCreatedAtMillis(it.createdAt) ?: 0L }
  }

  suspend fun updateMoneyEntryDate(entryId: String, date: String) {
    SupabaseClient.client
      .from("money_entries")
      .update(
        buildJsonObject {
          put("date", date)
        },
      ) {
        filter { eq("id", entryId) }
      }
  }

  suspend fun insertMoneyEntry(userId: String, entry: MoneyEntry) {
    SupabaseClient.client
      .from("money_entries")
      .insert(
        buildJsonObject {
          put("id", entry.id)
          put("user_id", userId)
          put("type", entry.type.name)
          put("amount", entry.amount)
          put("date", entry.date)
          if (entry.createdAt.isNotBlank()) {
            put("created_at", entry.createdAt)
          }
          put("category", entry.category)
          put("note", entry.note)
          put("source_method", entry.sourceOrMethod)
          put("channel_bank", entry.channelOrBank)
        },
      )
  }

  suspend fun updateMoneyEntry(entry: MoneyEntry) {
    SupabaseClient.client
      .from("money_entries")
      .update(
        buildJsonObject {
          put("amount", entry.amount)
          put("date", entry.date)
          put("category", entry.category)
          put("note", entry.note)
          put("source_method", entry.sourceOrMethod)
          put("channel_bank", entry.channelOrBank)
        },
      ) {
        filter { eq("id", entry.id) }
      }
  }

  suspend fun deleteMoneyEntry(entryId: String) {
    SupabaseClient.client
      .from("money_entries")
      .delete {
        filter { eq("id", entryId) }
      }
  }

  suspend fun insertDreamEntry(userId: String, entry: DreamEntry) {
    SupabaseClient.client
      .from("dream_entries")
      .insert(
        buildJsonObject {
          put("id", entry.id)
          put("user_id", userId)
          put("title", entry.title)
          put("target", entry.target)
          put("current", entry.current)
          put("deadline", entry.deadline)
          put("note", entry.note)
          put("source_type", entry.sourceType)
        },
      )
  }

  suspend fun updateDreamEntry(entry: DreamEntry) {
    SupabaseClient.client
      .from("dream_entries")
      .update(
        buildJsonObject {
          put("title", entry.title)
          put("target", entry.target)
          put("current", entry.current)
          put("deadline", entry.deadline)
          put("note", entry.note)
          put("source_type", entry.sourceType)
        },
      ) {
        filter { eq("id", entry.id) }
      }
  }

  suspend fun deleteDreamEntry(entryId: String) {
    SupabaseClient.client
      .from("dream_entries")
      .delete {
        filter { eq("id", entryId) }
      }
  }

  suspend fun deleteUserAccount(userId: String) {
    SupabaseClient.client
      .from("users")
      .delete {
        filter { eq("id", userId) }
      }
  }
}
