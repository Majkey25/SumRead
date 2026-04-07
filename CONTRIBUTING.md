# Contributing

This repository is public but maintained as a proprietary product codebase.

## Branch naming

Use focused branch names with the date suffix:

- `feat/<change>/DD-MM-YYYY`
- `fix/<change>/DD-MM-YYYY`
- `chore/<change>/DD-MM-YYYY`

Example:

- `fix/media-projection-cleanup/05-04-2026`

## Quality gates

- Keep architecture boundaries clear.
- Do not add hardcoded secrets.
- Keep captured content transient by default.
- Prefer root-cause fixes over defensive patch layers.
- Run `./gradlew testDebugUnitTest lintDebug assembleDebug` before publishing changes.

## Pull request expectations

- Explain the user-facing impact.
- Note any Play policy or privacy implications.
- Mention any permission, overlay, capture, OCR, TTS, or provider behavior that changed.
