[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-App-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-0F766E)](https://developer.android.com/about/versions/oreo)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35-0F766E)](https://developer.android.com/about/versions)
[![License](https://img.shields.io/badge/License-Proprietary-1F2937)](./LICENSE)
[![Status](https://img.shields.io/badge/Status-In%20Development-C2410C)](#current-status)
[![Release](https://img.shields.io/badge/Release-Pre--Release-9A3412)](#latest-pre-release-apk)
[![Build Validation](https://github.com/Majkey25/SumRead/actions/workflows/android-build.yml/badge.svg)](https://github.com/Majkey25/SumRead/actions/workflows/android-build.yml)

# SumRead

SumRead is a production-minded Android application in active development. It provides a floating overlay that lets the user capture a selected screen region, extract text with on-device OCR, read the text aloud locally, summarize it with an optional AI provider, or open a private in-memory chat grounded in the captured content.

This repository is public yet proprietary, and structured for a serious future Google Play release. The current distribution target is a pre-release APK, not a production Play build.
<img width="1536" height="1024" alt="SumRead app on Samsung Galaxy S26 Ultra (1)" src="https://github.com/user-attachments/assets/54439a19-13ef-4ef4-88bb-774ce5233329" />


## Current status

- Architecture: MVVM + Clean Architecture + Hilt.
- UI: Jetpack Compose for the app, view-based overlay for system overlay behavior, custom region selection view for capture accuracy.
- Local-first features: overlay control, screen capture flow, region selection, ML Kit OCR, TextToSpeech.
- AI features: Groq and Gemini behind provider abstractions with user-supplied keys.
- Chat privacy: memory-only session state, no default storage of captured text or chat history.
- Release posture: pre-release APK workflow prepared, production signing still pending.

## Feature overview

- Draggable floating overlay button powered by a foreground service.
- User-initiated MediaProjection capture flow with explicit Android consent.
- Region selection before OCR processing.
- On-device ML Kit text recognition.
- On-device text to speech with configurable speed, pitch, and language.
- AI summary mode using a selectable provider.
- AI chat mode grounded in the selected screen content.
- Secure local storage for provider API keys using Android Keystore-backed encryption.
- Compose settings screen with permission helpers and privacy disclosures.

## Architecture overview

The app follows a single-module clean structure inside `app/src/main/java/com/sumread`:

- `data/` for settings storage, secure secret storage, OCR, TTS, provider integrations, and repository implementations.
- `domain/` for models, repository contracts, and use cases.
- `presentation/` for Compose screens, activities, viewmodels, and capture/chat UI state.
- `service/` for the persistent overlay service and MediaProjection capture service.
- `di/` for Hilt modules and network wiring.
- `util/` for app configuration, bitmap helpers, intent building, notifications, and geometry helpers.

## Tech stack

- Kotlin
- Android SDK 35 target, min SDK 26
- Gradle Kotlin DSL with version catalog
- Jetpack Compose
- Hilt
- DataStore Preferences
- Android Keystore-backed secret storage
- ML Kit Text Recognition
- Android TextToSpeech
- Retrofit + OkHttp
- Groq and Gemini provider integrations

## Privacy-first principles

- Local OCR and local TTS are the default path.
- AI features are optional and clearly separated from local-only features.
- The app never ships with embedded provider keys.
- Captured text is not persisted by default.
- Chat history remains memory-only.
- Foreground notifications make long-running sensitive operations visible.
- Screen capture is always user initiated and always gated by the Android system consent dialog.

## Latest pre-release APK

- Intended latest APK URL: [sumread-pre-release-debug.apk](https://github.com/Majkey25/SumRead/releases/latest/download/sumread-pre-release-debug.apk)
- Release index: [GitHub Releases](https://github.com/Majkey25/SumRead/releases)
- Current expectation: debug-style pre-release APK until release signing is configured.

## Future Google Play release

- Google Play release is planned, not available yet.
- Production signing, store assets, final disclosure copy, and Play review packaging still need completion.
- The repository already includes privacy, data safety, and Play compliance notes to support that path.

## Setup

1. Install Android Studio with Android SDK 35 and JDK 17.
2. Clone the private repository.
3. Open the project in Android Studio or use the Gradle wrapper from the repository root.
4. Build with `./gradlew assembleDebug`.
5. Install the generated debug APK on a device running Android 8.0 or newer.
6. Open the app, review permissions, and configure optional provider keys in Settings if you want AI features.

## Local development

Useful commands:

- `./gradlew testDebugUnitTest`
- `./gradlew lintDebug`
- `./gradlew assembleDebug`

Recommended manual device checks:

- Start and stop the overlay repeatedly.
- Validate MediaProjection consent and cancellation behavior.
- Validate region selection on light and dark backgrounds.
- Validate local read-aloud without any provider key configured.
- Validate AI summary and chat with each provider independently.

## Privacy and permissions

Required or optional permissions are intentionally limited:

- `SYSTEM_ALERT_WINDOW` for the floating overlay.
- `FOREGROUND_SERVICE` for visible overlay execution.
- `FOREGROUND_SERVICE_MEDIA_PROJECTION` for the active capture service.
- `RECORD_AUDIO` for optional speech input in chat.
- `INTERNET` only for optional cloud AI features.

See:

- [Privacy policy](./privacy_policy.md)
- [Play Store compliance notes](./PLAY_STORE_COMPLIANCE_NOTES.md)
- [Data safety notes](./DATA_SAFETY_NOTES.md)

## Screenshots

Screenshot placeholders are intentionally reserved until stable device captures are generated:

- `docs/screenshots/settings.png`
- `docs/screenshots/overlay.png`
- `docs/screenshots/region-selection.png`
- `docs/screenshots/chat.png`

## Roadmap

- Add a signed pre-release distribution pipeline.
- Expand OCR language options beyond the current Latin recognizer baseline.
- Add streaming response support behind the existing AI provider abstraction.
- Harden device QA across Android 8 through Android 15.
- Prepare final Play listing assets and store review disclosures.

## License

This repository is proprietary and all rights are reserved. See [LICENSE](./LICENSE).
