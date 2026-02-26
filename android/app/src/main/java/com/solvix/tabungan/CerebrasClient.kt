package com.solvix.tabungan

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

object CerebrasClient {
  private val json = Json { ignoreUnknownKeys = true }

  suspend fun requestFinancialAdvice(
    messages: List<ChatMessage>,
    appContext: String = "",
    userDataContext: String = "",
  ): String = withContext(Dispatchers.IO) {
    val endpoint = BuildConfig.CEREBRAS_API_URL.trim()
    val apiKey = BuildConfig.CEREBRAS_API_KEY.trim()
    val model = BuildConfig.CEREBRAS_MODEL.trim()

    if (endpoint.isBlank() || apiKey.isBlank() || model.isBlank()) {
      throw IllegalStateException("Cerebras configuration is incomplete")
    }

    val body = buildJsonObject {
      put("model", model)
      put("temperature", 0.35)
      put("max_tokens", 700)
      put(
        "messages",
        buildJsonArray {
          add(
            buildJsonObject {
              put("role", "system")
              put(
                "content",
                buildString {
                  append("You are a practical financial planning assistant for CashFlow app. ")
                  append("Give concise and actionable responses with clear next steps and risk reminders when relevant. ")
                  append("Never mention internal context labels, variable names, or system sources. ")
                  append("When referring to user financial records in Indonesian, use exactly this phrase: 'dari data transaksi anda'. ")
                  append("When referring to user records in English, use: 'from your transaction data'.\n")
                  if (appContext.isNotBlank()) {
                    append("Application information:\n")
                    append(appContext)
                    append('\n')
                  }
                  if (userDataContext.isNotBlank()) {
                    append("Transaction data snapshot:\n")
                    append(userDataContext)
                  }
                },
              )
            },
          )
          messages.forEach { message ->
            add(
              buildJsonObject {
                put("role", message.role)
                put("content", message.content)
              },
            )
          }
        },
      )
    }.toString()

    val responseText = executeRequest(endpoint, apiKey, body, connectTimeoutMs = 20000, readTimeoutMs = 30000)
    val payload = json.parseToJsonElement(responseText).jsonObject
    val content = payload["choices"]
      ?.jsonArray
      ?.firstOrNull()
      ?.jsonObject
      ?.get("message")
      ?.jsonObject
      ?.get("content")
      ?.jsonPrimitive
      ?.contentOrNull
      ?.trim()
      .orEmpty()

    if (content.isBlank()) {
      throw IllegalStateException("Cerebras response content is empty")
    }
    content
  }

  suspend fun classifyDebtCategory(
    category: String,
    note: String,
    sourceOrMethod: String,
    channelOrBank: String,
  ): String? = withContext(Dispatchers.IO) {
    val endpoint = BuildConfig.CEREBRAS_API_URL.trim()
    val apiKey = BuildConfig.CEREBRAS_API_KEY.trim()
    val model = BuildConfig.CEREBRAS_MODEL.trim()
    if (endpoint.isBlank() || apiKey.isBlank() || model.isBlank()) return@withContext null

    val body = buildJsonObject {
      put("model", model)
      put("temperature", 0.0)
      put("max_tokens", 12)
      put(
        "messages",
        buildJsonArray {
          add(
            buildJsonObject {
              put("role", "system")
              put(
                "content",
                "Classify expense text into one label only: friend_debt, installment, credit_card, paylater, none. Reply with label only.",
              )
            },
          )
          add(
            buildJsonObject {
              put("role", "user")
              put(
                "content",
                "category=$category; note=$note; source=$sourceOrMethod; channel=$channelOrBank",
              )
            },
          )
        },
      )
    }.toString()

    val response = runCatching {
      executeRequest(endpoint, apiKey, body, connectTimeoutMs = 8000, readTimeoutMs = 12000)
    }.getOrNull() ?: return@withContext null

    val content = json.parseToJsonElement(response).jsonObject["choices"]
      ?.jsonArray
      ?.firstOrNull()
      ?.jsonObject
      ?.get("message")
      ?.jsonObject
      ?.get("content")
      ?.jsonPrimitive
      ?.contentOrNull
      ?.trim()
      ?.lowercase()
      .orEmpty()

    when {
      content.contains("friend_debt") -> "friend_debt"
      content.contains("installment") -> "installment"
      content.contains("credit_card") -> "credit_card"
      content.contains("paylater") -> "paylater"
      else -> null
    }
  }

  suspend fun enrichInsightsNarrative(
    summaryJson: String,
  ): String = withContext(Dispatchers.IO) {
    val endpoint = BuildConfig.CEREBRAS_API_URL.trim()
    val apiKey = BuildConfig.CEREBRAS_API_KEY.trim()
    val model = BuildConfig.CEREBRAS_MODEL.trim()
    if (endpoint.isBlank() || apiKey.isBlank() || model.isBlank()) {
      throw IllegalStateException("Cerebras configuration is incomplete")
    }
    val body = buildJsonObject {
      put("model", model)
      put("temperature", 0.2)
      put("max_tokens", 260)
      put(
        "messages",
        buildJsonArray {
          add(
            buildJsonObject {
              put("role", "system")
              put(
                "content",
                "You summarize personal finance signals. Keep output under 3 short sentences. No investment or trading advice. Focus on budgeting, spending control, debt risk, and goal pacing.",
              )
            },
          )
          add(
            buildJsonObject {
              put("role", "user")
              put("content", "Summarize and provide practical next action from this compact data: $summaryJson")
            },
          )
        },
      )
    }.toString()
    val responseText = executeRequest(endpoint, apiKey, body, 15000, 20000)
    val payload = json.parseToJsonElement(responseText).jsonObject
    val content = payload["choices"]
      ?.jsonArray
      ?.firstOrNull()
      ?.jsonObject
      ?.get("message")
      ?.jsonObject
      ?.get("content")
      ?.jsonPrimitive
      ?.contentOrNull
      ?.trim()
      .orEmpty()
    if (content.isBlank()) throw IllegalStateException("Insights enrichment is empty")
    content
  }

  private fun executeRequest(
    endpoint: String,
    apiKey: String,
    body: String,
    connectTimeoutMs: Int,
    readTimeoutMs: Int,
  ): String {
    val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
      requestMethod = "POST"
      connectTimeout = connectTimeoutMs
      readTimeout = readTimeoutMs
      doOutput = true
      setRequestProperty("Authorization", "Bearer $apiKey")
      setRequestProperty("Content-Type", "application/json")
      setRequestProperty("Accept", "application/json")
    }

    connection.outputStream.use { stream ->
      stream.write(body.toByteArray(Charsets.UTF_8))
    }

    val code = connection.responseCode
    val responseText = (if (code in 200..299) connection.inputStream else connection.errorStream)
      ?.bufferedReader()
      ?.use { it.readText() }
      .orEmpty()

    if (code !in 200..299) {
      throw IllegalStateException("Cerebras request failed ($code)")
    }
    return responseText
  }
}
