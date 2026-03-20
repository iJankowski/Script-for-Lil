package com.scriptforlil.kindroidhealthsync.domain

import android.content.Context
import android.content.res.Configuration
import com.scriptforlil.kindroidhealthsync.R
import com.scriptforlil.kindroidhealthsync.data.health.HealthSnapshot
import com.scriptforlil.kindroidhealthsync.ui.SettingsState
import java.util.Locale

class MessageComposer(
    private val context: Context,
) {
    fun compose(snapshot: HealthSnapshot, settings: SettingsState): String {
        val messageContext = contextForLanguage(settings.messageLanguage)
        val defaultTemplate = messageContext.getString(R.string.message_template_default)
        val template = settings.messageTemplate.ifBlank { defaultTemplate }

        val tokens = mapOf(
            TEMPLATE_SLEEP to if (settings.includeSleep && snapshot.sleepSummary != null) {
                messageContext.getString(R.string.message_sleep, snapshot.sleepSummary)
            } else {
                ""
            },
            TEMPLATE_ACTIVITY to if (settings.includeActivity && snapshot.activitySummary != null) {
                messageContext.getString(R.string.message_activity, snapshot.activitySummary)
            } else {
                ""
            },
            TEMPLATE_HEART_RATE_NOW to if (settings.includeCurrentHeartRate && snapshot.currentHeartRate != null) {
                messageContext.getString(R.string.message_heart_rate_now, snapshot.currentHeartRate)
            } else {
                ""
            },
            TEMPLATE_HEART_RATE_AVG to if (settings.includeAverageHeartRate && snapshot.averageHeartRate15Min != null) {
                messageContext.getString(R.string.message_heart_rate_avg, snapshot.averageHeartRate15Min)
            } else {
                ""
            },
        )

        return sanitizeTemplate(
            tokens.entries.fold(template) { acc, (token, value) ->
                acc.replace(token, value)
            }
        )
    }

    private fun contextForLanguage(languageTag: String): Context {
        val locale = when (languageTag) {
            "pl" -> Locale("pl")
            else -> Locale("en")
        }
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    private fun sanitizeTemplate(text: String): String {
        return text
            .replace("\r\n", "\n")
            .replace(Regex("[ \t]+"), " ")
            .replace(Regex(" *\n *"), "\n")
            .replace(Regex("\n{3,}"), "\n\n")
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString("\n")
            .trim()
    }

    companion object {
        const val TEMPLATE_SLEEP = "{sleep}"
        const val TEMPLATE_ACTIVITY = "{activity}"
        const val TEMPLATE_HEART_RATE_NOW = "{heart_rate_now}"
        const val TEMPLATE_HEART_RATE_AVG = "{heart_rate_avg}"
    }
}
