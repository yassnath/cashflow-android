package com.solvix.tabungan

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object SupabaseClient {
  private val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
  }

  val client = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
  ) {
    install(Auth)
    install(Postgrest) {
      serializer = KotlinXSerializer(json)
    }
  }
}

@Serializable
data class SupabaseUser(
  val id: String = "",
  val name: String = "",
  val email: String = "",
  val country: String = "",
  val bio: String = "",
  val birthdate: String = "",
  @SerialName("created_at")
  val createdAt: String = "",
  @SerialName("auth_id")
  val authId: String = "",
  val username: String = "",
  val password: String = "",
)

@Serializable
data class SupabaseMoneyEntry(
  val id: String = "",
  @SerialName("user_id")
  val userId: String = "",
  val type: String = "",
  val amount: Double = 0.0,
  val date: String = "",
  @SerialName("created_at")
  val createdAt: String = "",
  val category: String = "",
  val note: String = "",
  @SerialName("source_method")
  val sourceOrMethod: String = "",
  @SerialName("channel_bank")
  val channelOrBank: String = "",
) {
  val amountInt: Int get() = amount.toInt()
}

@Serializable
data class SupabaseDreamEntry(
  val id: String = "",
  @SerialName("user_id")
  val userId: String = "",
  val title: String = "",
  val target: Double = 0.0,
  val current: Double = 0.0,
  val deadline: String = "",
  val note: String = "",
  @SerialName("source_type")
  val sourceType: String = "income",
) {
  val targetInt: Int get() = target.toInt()
  val currentInt: Int get() = current.toInt()
}

@Serializable
data class SupabaseInsightFeedback(
  val id: String = "",
  @SerialName("user_id")
  val userId: String = "",
  @SerialName("insight_id")
  val insightId: String = "",
  @SerialName("is_helpful")
  val isHelpful: Boolean = false,
  val reason: String = "",
  @SerialName("created_at")
  val createdAt: String = "",
)
