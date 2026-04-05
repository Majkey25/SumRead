# SumRead Privacy Policy

Last updated: 2026-04-05

## Overview

SumRead is designed with privacy-first defaults.

- The default experience uses on-device OCR and on-device text to speech.
- Cloud AI features are optional and disabled until the user configures a provider and their own API key.
- Screen text is processed only after a user-initiated capture flow.

## Data SumRead Processes

SumRead may process the following categories of data on the device:

- Selected screen image content captured after explicit MediaProjection consent.
- OCR text extracted from the selected region.
- User settings such as speech rate, pitch, selected provider, and language preference.
- User-supplied API keys for enabled AI providers.
- Temporary in-memory chat session data for the current capture context.

## What Stays On Device

The following stays local by default:

- OCR for read-aloud mode.
- Text to speech playback.
- Overlay state and local settings.
- Temporary chat session history while the app process is active.

Captured text is not persisted to long-term storage by default.

## Cloud AI Processing

If the user enables an AI mode and configures a provider, SumRead may send the OCR text from the user-selected screen region to the chosen provider.

Supported providers:

- Groq
- Google Gemini

SumRead does not provide shared API keys. The user supplies and controls their own credentials.

Users should review the privacy terms of the provider they choose to use.

## Secret Storage

- API keys are stored locally using Android Keystore-backed encryption.
- Non-secret app settings are stored separately from secrets.

## Chat History

- Chat sessions use the current capture as context.
- Chat history remains memory-only.
- Chat history is cleared when the session ends or the process is terminated.

## Permissions

SumRead may request:

- `SYSTEM_ALERT_WINDOW` to show the floating overlay above other apps.
- `RECORD_AUDIO` to support optional speech input for chat.
- `FOREGROUND_SERVICE` to keep visible long-running user-initiated operations transparent.
- `FOREGROUND_SERVICE_MEDIA_PROJECTION` for the active screen capture session.
- `INTERNET` only for optional cloud AI features.

## Analytics and Tracking

SumRead does not include advertising SDKs, third-party analytics SDKs, or hidden tracking in this repository state.

## Contact

For privacy questions, contact the repository owner through the private support channel used for this project.
