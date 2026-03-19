# Kindroid Health Sync

Android MVP app for sending wearable-derived health context to one Kin through Health Connect.

## Current State

What works now:
- modern Jetpack Compose UI with cleaned-up settings and status flow
- local settings stored in DataStore
- Health Connect availability check and permission flow for sleep, steps, and heart rate
- preview message built from real Health Connect data
- manual test send to Kin through the real `POST /v1/send-message`
- basic WorkManager auto-sync flow using saved settings
- response diagnostics: API error copy and modal preview of the last successful API response
- Gradle project builds successfully in Android Studio

Current limitations:
- auto-sync is still MVP-grade and not deeply observable in the UI
- sleep selection still uses the latest sleep session, not a stricter "last night" heuristic
- settings are stored locally in DataStore without extra encryption
- diagnostics are meant for testing and may be simplified later

## Data Flow

`Huawei Health -> Health Sync -> Health Connect -> this app -> Kin`

Huawei data is expected to reach Health Connect through Health Sync. This app reads only from Health Connect.

## Build

In Android Studio:
1. Open the repository as a project.
2. Let Gradle sync finish.
3. Run `Build > Make Project`.
4. To generate an APK, use `Build > Build Bundle(s) / APK(s) > Build APK(s)`.

The debug APK is typically generated in:
- `app/build/outputs/apk/debug/app-debug.apk`

## Testing Notes

For first-time setup on the phone:
1. Make sure Health Connect is installed and already contains synced data.
2. Open the app.
3. Use `Nadaj dostęp` to grant read access for sleep, steps, and heart rate.
4. If the permission launcher behaves oddly, use `Otwórz Health Connect` and grant access manually.
5. Fill in the Kindroid API key and `ai_id`.
6. Use `Wyślij test` to verify end-to-end delivery.

If sending fails:
- use `Kopiuj błąd` to copy the full stack trace
- after a successful send, use `Otwórz odpowiedź` to inspect the latest API response

## Next Steps

- make auto-sync status visible in the UI
- refine quiet-hours UX and validation
- tighten sleep-session selection so `Sen ostatniej nocy` is semantically correct
- consider encrypting local sensitive settings if the app moves beyond MVP

