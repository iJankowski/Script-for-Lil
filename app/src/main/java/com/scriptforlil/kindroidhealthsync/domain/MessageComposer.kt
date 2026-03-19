package com.scriptforlil.kindroidhealthsync.domain

import com.scriptforlil.kindroidhealthsync.data.health.HealthSnapshot
import com.scriptforlil.kindroidhealthsync.ui.SettingsState

class MessageComposer {
    fun compose(snapshot: HealthSnapshot, settings: SettingsState): String {
        val parts = buildList {
            if (settings.includeSleep && snapshot.sleepSummary != null) {
                add("Sen ostatniej nocy: ${snapshot.sleepSummary}.")
            }
            if (settings.includeActivity && snapshot.activitySummary != null) {
                add("Aktywność dzisiaj: ${snapshot.activitySummary}.")
            }
            if (settings.includeCurrentHeartRate && snapshot.currentHeartRate != null) {
                add("Tętno teraz: ${snapshot.currentHeartRate} bpm.")
            }
            if (settings.includeAverageHeartRate && snapshot.averageHeartRate15Min != null) {
                add("Średnie tętno z ostatnich 15 minut: ${snapshot.averageHeartRate15Min} bpm.")
            }
        }

        return buildString {
            append("Aktualizacja mojego stanu na podstawie danych z zegarka. ")
            append(parts.joinToString(" "))
            append(" Potraktuj to jako krótki kontekst o moim samopoczuciu.")
        }.trim()
    }
}
