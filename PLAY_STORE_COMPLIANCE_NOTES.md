# Play Store Compliance Notes

## Current compliance posture

SumRead is structured to make sensitive behavior explicit, user-initiated, and reviewable.

## Sensitive capability design

- The overlay is visible and user-controlled.
- Screen capture requires the Android MediaProjection consent dialog for each session.
- No hidden capture flow exists.
- No background surveillance workflow exists.
- Foreground notifications are shown for the overlay service and capture service.
- Cloud AI processing is opt in and clearly explained in settings and permission flows.

## Permissions rationale

### `SYSTEM_ALERT_WINDOW`

Used only to present the draggable floating action bubble above other apps.

### `FOREGROUND_SERVICE`

Used for the visible overlay service so Android can keep the user-initiated overlay alive.

### `FOREGROUND_SERVICE_MEDIA_PROJECTION`

Used only during the active screen capture session initiated by the user.

### `RECORD_AUDIO`

Optional. Used only when the user chooses speech input inside chat.

### `INTERNET`

Optional. Used only for Groq or Gemini when the user enables cloud AI features and provides their own key.

## Review notes for future submission

- Prepare a short demo showing user initiation of overlay, capture consent, region selection, and AI opt-in.
- Provide a clear in-app disclosure for cloud AI transmission before release review.
- Keep store listing copy specific about accessibility and productivity use cases.
- Add a signed release pipeline before the first production submission.
- Re-validate Play policy text before publishing because policy language can change.
