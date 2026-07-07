# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

An Android app for reading EU passports and ID cards via NFC, implementing the [BSI TR-03110](https://www.bsi.bund.de/EN/Themen/Unternehmen-und-Organisationen/Standards-und-Zertifizierung/Technische-Richtlinien/TR-nach-Thema-sortiert/tr03110/TR-03110_node.html) standard (Advanced Security Mechanisms for Machine Readable Travel Documents / eIDAS). Built with Kotlin Multiplatform targeting Android, with a potential iOS target in the future.

## Commands

```bash
# Build debug APK
./gradlew :androidApp:assembleDebug

# Run all tests (runs on JVM, not a device)
./gradlew :shared:testAndroidHostTest

# Run a single test class
./gradlew :shared:testAndroidHostTest --tests "fr.outadoc.eidas.pace.PaceGetNonceUseCaseTest"
```

## Architecture

Two Gradle modules:

- **`androidApp`** — thin Android entry point; wires Koin DI and hosts the Compose activity.
- **`shared`** — all business logic, Compose UI, and platform abstractions. Structured as KMP source sets:
  - `commonMain` — platform-agnostic code, interfaces, and Compose UI
  - `androidMain` — Android implementations of platform interfaces
  - `androidHostTest` — tests running on JVM (no device required)

### NFC / APDU layer (`shared/.../nfc/`)

- **`NfcTagReader`** — exposes `detectedTags: Flow<NfcSession>`. Collecting starts NFC discovery; cancelling stops it. Errors setting up the reader (e.g. NFC unavailable) throw from the flow.
- **`NfcSession`** — exposes `transceive(CApdu): Result<RApdu>`. Returns `Result.failure(NfcException)` on transport errors; never throws.

`AndroidNfcTagReader` (androidMain) implements `NfcTagReader` and emits a `RealNfcSession` per detected tag. `RealNfcSession` wraps `IsoDep`, implements `NfcSession` and `AutoCloseable`, and dispatches I/O on `Dispatchers.IO`.

`CApdu` models a command APDU built via `CommandFactory`. `RApdu` exposes `sw1`/`sw2`, `isSuccess`, and `getData(): Result<UByteArray>` (returns `Result.failure` on non-90-00 status).

### Error handling convention

All use cases and APDU-touching code follow a railway-oriented style using `kotlin.Result`:

- `nfcSession.transceive(...).getOrElse { return Result.failure(it) }` — propagates NFC transport errors
- `.getData().getOrElse { return Result.failure(it) }` — propagates APDU status errors
- Crypto/parsing blocks that can throw are wrapped in `runCatching { ... }` and returned directly
- `parseDynamicAuthData(): Result<TLVList>` (extension on `UByteArray` in `DynAuthExt.kt`) follows the same convention

All five PACE use cases (`ReadCardAccessUseCase`, `PaceGetNonceUseCase`, `PaceMapNonceUseCase`, `PaceKeyAgreementUseCase`, `PaceMutualAuthUseCase`) return `Result<T>`. `PaceAuthenticateUseCase` chains them with `getOrElse { return Result.failure(it) }`.

`ReaderViewModel` catches errors at the outer `try/catch` level around `collect { }`.

### PACE authentication flow (`shared/.../pace/`)

BSI TR-03110 PACE in five steps, each a use case:
1. `ReadCardAccessUseCase` — SELECT + READ BINARY EF.CardAccess, parse `SecurityInfo` to discover the chip's supported algorithms
2. `PaceGetNonceUseCase` — MSE:Set AT + General Authenticate step 1; derives key from CAN, decrypts chip nonce
3. `PaceMapNonceUseCase` — General Authenticate step 2 (generic mapping); produces mapped EC generator G′
4. `PaceKeyAgreementUseCase` — General Authenticate step 3; performs ECDH, derives session keys K_enc and K_mac
5. `PaceMutualAuthUseCase` — General Authenticate step 4; verifies CMAC tokens both ways

`PaceAuthenticateUseCase` orchestrates all five and returns `Result<PaceCredentials>` containing the session keys.

### Secure messaging (`shared/.../securemessaging/`)

`SecureMessagingSession` implements `NfcSession` as a decorator over a plain `NfcSession`. After PACE succeeds, `ReaderViewModel` wraps the raw session in a `SecureMessagingSession` (created via `SecureSessionFactory`) before performing further reads. It maintains a 128-bit Send Sequence Counter (SSC) and applies TR-03110 SM (AES-CBC encrypt + CMAC) on every command/response pair.

### LDS layer (`shared/.../lds/`)

Post-PACE reads over the secure session:

- `ReadDirUseCase` — SELECT EF.DIR + READ BINARY; returns raw bytes.
- `ReadLdsDataUseCase` — selects the MRTD AID, calls `ReadDirUseCase`, then reads LDS data (in progress).

### Logging (`shared/.../logging/`)

- `Logger` — common interface with `d/i/w/e` extension helpers.
- `MemoryLogger` — wraps an optional delegate and accumulates entries into a `StateFlow<List<LogEntry>>` observed by the UI.
- `AndroidLogger` — delegates to Android's `Log`.

### Settings encryption (`shared/.../settings/`)

`SettingsEncryptor` (interface) exposes `encrypt`/`decrypt`, both returning `Result<String>`. `AndroidSettingsEncryptor` backs it with Android Keystore + AES-GCM. Used by `DataStoreSettingsRepository` to store the CAN at rest.

### Dependency Injection (Koin)

Three Koin modules loaded in order:

- **`androidModule`** (androidMain) — `AndroidLogger` (named `"platformLogger"`), `AndroidCryptoEngine`, `AndroidKeyGenerator`, `AndroidSettingsEncryptor` → `SettingsEncryptor`, `SecureSessionFactory`, `DataStore`.
- **`sharedModule`** (commonMain) — `MemoryLogger` → `Logger`, all PACE use cases, `ReadDirUseCase`, `ReadLdsDataUseCase`, `SettingsRepository`, `SettingsViewModel`, `ReaderViewModel`.
- **`activityScopedModule(activity)`** (androidMain) — `AndroidNfcTagReader` → `NfcTagReader` (activity-scoped because Android's reader mode API requires an `Activity`).

## Key Conventions

- Never access `android.nfc` directly outside `androidMain`.
- `NfcSession.transceive` never throws; use `.getOrElse { return Result.failure(it) }` to propagate errors.
- Use `getData()` on `RApdu` to extract the payload; it returns `Result.failure` on non-9000 status.
- Hex encoding/decoding utilities live in `utils/Hex.kt`.
