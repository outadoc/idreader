# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

An Android app for reading EU passports and ID cards via NFC, implementing the [BSI TR-03110](https://www.bsi.bund.de/EN/Themen/Unternehmen-und-Organisationen/Standards-und-Zertifizierung/Technische-Richtlinien/TR-nach-Thema-sortiert/tr03110/TR-03110_node.html) standard (Advanced Security Mechanisms for Machine Readable Travel Documents / eIDAS). Built with Kotlin Multiplatform targeting Android, with a potential iOS target in the future.

## Commands

```bash
# Build debug APK
./gradlew :androidApp:assembleDebug

# Run Android host tests (runs on JVM, not a device)
./gradlew :shared:testAndroidHostTest

# Run common tests
./gradlew :shared:testCommonUnitTest
```

## Architecture

Two Gradle modules:

- **`androidApp`** — thin Android entry point; wires Koin DI and hosts the Compose activity.
- **`shared`** — all business logic, Compose UI, and platform abstractions. Structured as KMP source sets:
  - `commonMain` — platform-agnostic code, interfaces, and Compose UI
  - `androidMain` — Android implementations of platform interfaces
  - `androidHostTest` / `commonTest` — tests

### NFC / APDU layer (`shared/.../nfc/`)

The core abstraction is `NfcTagReader`, a cold `Flow`-based interface:
- Collecting `detectedTags` starts NFC discovery; cancelling stops it.
- `transceive(tag, CApdu)` sends an ISO 7816-4 C-APDU and returns an `RApdu`.
- `CApdu` models a command APDU (built via factory methods like `CApdu.selectAid`).
- `RApdu` is an inline value class wrapping the raw response bytes, exposing `sw1`/`sw2` and `isSuccess`.
- `AndroidNfcTagReader` implements `NfcTagReader` using Android's reader mode API, limited to IsoDep (ISO 14443-4) tags.
- Known AIDs are collected in `Aid` (e.g., `Aid.MRTD` for the MRTD/ePassport application).

### Logging (`shared/.../logging/`)

Layered logger design:
- `Logger` is the common interface with `d/i/w/e` extension helpers.
- `MemoryLogger` wraps an optional delegate and accumulates entries into a `StateFlow<List<LogEntry>>`, which the UI observes.
- `AndroidLogger` delegates to Android's `Log`.
- The Koin graph wires `AndroidLogger` → `MemoryLogger` → `Logger`, so all log output also flows to the in-app terminal view.

### UI (`shared/.../`)

- `App` is the root `@Composable`; it collects `detectedTags` in a `LaunchedEffect`, sends the SELECT AID command, and logs results.
- `TerminalView` renders the `MemoryLogger` entries as a scrolling monospace terminal with color-coded log levels.

### Dependency Injection (Koin)

- `sharedModule` (commonMain) — registers `MemoryLogger` and `Logger`.
- `androidModule(activity)` (androidMain) — registers `AndroidLogger` (as named `"platformLogger"`) and `AndroidNfcTagReader`.
- The Android entry point starts Koin with both modules before the Compose tree is created.

## Key Conventions

- All NFC communication is mediated through `NfcTagReader`; never access `android.nfc` directly outside `androidMain`.
- APDU responses must be validated via `RApdu.isSuccess` before trusting the payload.
- Hex encoding/decoding utilities live in `utils/Hex.kt`.
