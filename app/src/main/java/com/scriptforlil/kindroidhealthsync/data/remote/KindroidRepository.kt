package com.scriptforlil.kindroidhealthsync.data.remote

import kotlinx.coroutines.delay

class KindroidRepository {
    suspend fun sendMessage(apiKey: String, aiId: String, message: String): Result<Unit> {
        if (apiKey.isBlank() || aiId.isBlank() || message.isBlank()) {
            return Result.failure(IllegalArgumentException("Brakuje API key, ai_id albo wiadomości."))
        }

        // Stub for the real Kindroid API call.
        delay(250)
        return Result.success(Unit)
    }
}
