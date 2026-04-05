# Data Safety Notes

## Purpose

This file is a working reference for the future Google Play Data safety form.

## Data categories

### Screen content

- Collected: only after explicit user action and Android system consent.
- Processed: on device for OCR, optionally sent to the selected AI provider.
- Stored: not persisted by default.

### App settings

- Collected: yes.
- Stored: locally on device.
- Purpose: app functionality and user preferences.

### API keys

- Collected: yes, only if the user enters them.
- Stored: locally on device using Android Keystore-backed encryption.
- Shared: no, except when the user directly uses the configured provider.

### Chat history

- Collected: transiently for the current in-memory session.
- Stored: memory only.
- Shared: only if the user sends a message through an enabled cloud AI provider.

### Audio input

- Collected: only when the user taps microphone input.
- Stored: not persisted by default in this repository state.
- Purpose: speech-to-text convenience inside chat.

## Third parties

Optional provider integrations:

- Groq
- Google Gemini

## Current declaration assumptions

- No data selling.
- No advertising.
- No account system.
- No cross-app tracking.
- No analytics SDK.
