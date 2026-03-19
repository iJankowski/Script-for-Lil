# Kindroid Health Sync

Android MVP scaffold for syncing wearable-derived health context to a single Kindroid.

## Current State

What works now:
- Android app scaffold with Jetpack Compose UI
- local settings stored in DataStore
- Health Connect availability check
- Health Connect permission flow for sleep, steps, and heart rate
- preview message built from Health Connect data
- Gradle project builds successfully in Android Studio

What is still a stub:
- Kindroid API sending
- background sync via WorkManager
- final sleep interpretation and production-grade error handling

## Planned Data Flow

`Huawei Health -> Health Sync -> Health Connect -> this app -> Kindroid`

Huawei data is expected to reach Health Connect through Health Sync. This app reads only from Health Connect.

## Build

In Android Studio:
1. Open the repository as a project.
2. Let Gradle sync finish.
3. Run `Build > Make Project`.
4. To generate an APK, use `Build > Build Bundle(s) / APK(s) > Build APK(s)`.

The debug APK is typically generated in:
- `app/build/outputs/apk/debug/app-debug.apk`

## Next Steps

- replace the Kindroid stub with a real `POST /v1/send-message`
- wire the same sending flow into `SyncWorker`
- make the selected sync interval actually affect WorkManager scheduling
- tighten sleep-session selection so `Sen ostatniej nocy` is semantically correct
