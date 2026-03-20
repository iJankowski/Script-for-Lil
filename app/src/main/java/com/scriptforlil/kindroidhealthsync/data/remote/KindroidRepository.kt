package com.scriptforlil.kindroidhealthsync.data.remote

import android.content.Context
import com.scriptforlil.kindroidhealthsync.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class KindroidRepository(
    context: Context,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .callTimeout(120, TimeUnit.SECONDS)
        .build(),
) {
    private val appContext = context.applicationContext

    suspend fun sendMessage(apiKey: String, aiId: String, message: String): Result<String> {
        if (apiKey.isBlank() || aiId.isBlank() || message.isBlank()) {
            return Result.failure(IllegalArgumentException(appContext.getString(R.string.error_missing_api_or_message)))
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
                    val body = response.body?.string().orEmpty().ifBlank {
                        appContext.getString(R.string.error_empty_response)
                    }
                    if (!response.isSuccessful) {
                        error(appContext.getString(R.string.error_kindroid_response, response.code, body))
                    }
                    body
                }
            }
        }.recoverCatching { throwable ->
            if (throwable is SocketTimeoutException) {
                appContext.getString(R.string.response_timeout_placeholder)
            } else {
                throw throwable
            }
        }
    }
}
