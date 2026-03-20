# Kindroid Health Sync

Android MVP app for sending health context from `Health Connect` to one Kin in Kindroid.

## Current State

What works now:
- Jetpack Compose UI with a bottom nav, collapsible status cards, and persistent dark/light theme
- local settings stored in `DataStore`
- Health Connect availability check and permission flow for sleep, steps, and heart rate
- preview message built from real Health Connect data
- editable message template with insertable placeholders:
  - `{sleep}`
  - `{activity}`
  - `{heart_rate_now}`
  - `{heart_rate_avg}`
- separate app language and message language
  - app language changes the UI
  - message language is assigned to the Kin and controls the generated message language
- manual test send to Kin through real `POST /v1/send-message`
- basic `WorkManager` auto-sync flow with saved status and worker state in the UI
- quiet hours picker based on Android time pickers instead of raw text input
- modal preview of the last saved Kin/API response

Current limitations:
- the project is still MVP-grade and optimized for one Kin only
- auto-sync works, but still needs more UX polish and deeper diagnostics
- sleep still uses the latest sleep session, not a stricter “last night only” rule
- settings are stored locally in `DataStore` without additional encryption
- message template is shared with the current Kin setup; future multi-Kin support still needs a real data model

## Data Flow

`Huawei Health -> Health Sync -> Health Connect -> this app -> Kindroid API`

Huawei data is expected to reach `Health Connect` through `Health Sync`. This app reads only from `Health Connect`.

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
1. Make sure `Health Connect` is installed and already contains synced data.
2. Open the app.
3. Use `Grant access` / `Nadaj dostęp` to allow sleep, steps, and heart-rate reads.
4. If the permission launcher behaves oddly, use `Open Health Connect` and grant access manually there.
5. Fill in the `Kindroid API key`, `Kindroid ai_id`, and Kin name.
6. Optionally edit the message template and choose the Kin message language.
7. Use `Send test` / `Wyślij test` to verify end-to-end delivery.

If sending fails:
- use `Copy error` / `Kopiuj błąd` to copy the stack trace
- after a successful send, use `Open response` / `Otwórz odpowiedź` to inspect the last saved API response

## Next Steps

- introduce a real multi-Kin model instead of one shared settings object
- improve auto-sync visibility and scheduling diagnostics further
- tighten sleep-session selection so “last night” is semantically correct
- consider encrypting local sensitive settings if the app moves beyond MVP
