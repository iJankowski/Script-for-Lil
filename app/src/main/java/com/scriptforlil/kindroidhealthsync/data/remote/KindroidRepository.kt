package com.scriptforlil.kindroidhealthsync.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class KindroidRepository(
    private val client: OkHttpClient = OkHttpClient(),
) {
    suspend fun sendMessage(apiKey: String, aiId: String, message: String): Result<String> {
        if (apiKey.isBlank() || aiId.isBlank() || message.isBlank()) {
            return Result.failure(IllegalArgumentException("Brakuje API key, ai_id albo wiadomości."))
        }

        return runCatching {
            withContext(Dispatchers.IO) {
                val json = JSONObject()
                    .put("ai_id", aiId)
                    .put("message", message)

                val request = Request.Builder()
                    .url("https://api.kindroid.ai/v1/send-message")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(json.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty().ifBlank { "Brak treści odpowiedzi" }
                    if (!response.isSuccessful) {
                        error("Kindroid API zwróciło ${response.code}: $body")
                    }
                    body
                }
            }
        }
    }
}
