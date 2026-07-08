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

`Iso7816` is a top-level constants object with nested objects:
- `Aid` — AID hex strings (e.g. `MRTD`)
- `File` — well-known file IDs (`CardAccess`, `COM`)
- `DataGroup` — DG numbers 1–16 as `UByte` constants
- `Tags` — BER-TLV tag constants; single-byte tags are `UByte`, multi-byte tags (e.g. `0x5F0E`) are `UInt`
- `KeyRef` — PACE key references (`MRZ`, `CAN`, `PIN`, `PUK`)

### TLV layer (`shared/.../tlv/`)

- `TlvNode(tag: UInt, value: UByteArray)` — a parsed TLV node; `children()` re-parses `value` as a nested TLV list.
- `UByteArray.parseTlv(): Result<List<TlvNode>>` — parses a flat byte array into a TLV list.
- `List<TlvNode>.firstWithTag(tag: UInt/UByte): TlvNode?` — tag lookup; `UByte` overload widens to `UInt`.
- `buildTlv { }` / `TlvBuilder` — DSL for constructing TLV-encoded byte arrays.

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

Post-PACE reads over the secure session. Models live in `lds/model/`.

**Reading pipeline:**

1. `ReadLdsDataUseCase` — top-level orchestrator: selects the MRTD AID, reads EF.COM to discover available DGs, reads each DG, then parses known ones. Returns `LdsDump`.
2. `ReadComFileUseCase` — SELECT + READ BINARY EF.COM; parses the TLV tag list (`0x5C`) to produce `ComData` (list of available DG numbers). Uses a local `DG_TAG_TO_NUMBER` map to convert LDS tags (e.g. `0x61` → DG1) to `Iso7816.DataGroup` numbers.
3. `ReadDataGroupUseCase` — SELECT FILE `01xx` + READ BINARY for any DG number, returns raw bytes.

**Parsing pipeline:**

- `ParseDG1UseCase` — strips TLV wrapper (`0x61` → `0x5F1F`), delegates to `ParseMrzUseCase`.
- `ParseMrzUseCase` — slices the MRZ string by TD1/TD2/TD3 field offsets, delegates name parsing to `ParseMrzNameUseCase`. Returns `MrzInfo`.
- `ParseMrzNameUseCase` — splits `SURNAME<<GIVEN1<GIVEN2` into a `CardHolderName`.
- `ParseDG11UseCase` — parses TLV wrapper (`0x6B`) and extracts optional fields from `Iso7816.Tags` into `AdditionalPersonalDetails`.

### Logging (`shared/.../logging/`)

- `Logger` — common interface with `d/i/w/e` extension helpers.
- `MemoryLogger` — wraps an optional delegate and accumulates entries into a `StateFlow<List<LogEntry>>` observed by the UI.
- `AndroidLogger` — delegates to Android's `Log`.

### Settings encryption (`shared/.../settings/`)

`SettingsEncryptor` (interface) exposes `encrypt`/`decrypt`, both returning `Result<String>`. `AndroidSettingsEncryptor` backs it with Android Keystore + AES-GCM. Used by `DataStoreSettingsRepository` to store the CAN at rest.

### Dependency Injection (Koin)

Three Koin modules loaded in order:

- **`androidModule`** (androidMain) — `AndroidLogger` (named `"platformLogger"`), `AndroidCryptoEngine`, `AndroidKeyGenerator`, `AndroidSettingsEncryptor` → `SettingsEncryptor`, `SecureSessionFactory`, `DataStore`.
- **`sharedModule`** (commonMain) — `MemoryLogger` → `Logger`, all PACE use cases, `ReadComFileUseCase`, `ReadDataGroupUseCase`, `ReadLdsDataUseCase`, `ParseDG1UseCase`, `ParseDG11UseCase`, `ParseMrzUseCase`, `ParseMrzNameUseCase`, `SettingsRepository`, `SettingsViewModel`, `ReaderViewModel`.
- **`activityScopedModule(activity)`** (androidMain) — `AndroidNfcTagReader` → `NfcTagReader` (activity-scoped because Android's reader mode API requires an `Activity`).

## Key Conventions

- Never access `android.nfc` directly outside `androidMain`.
- `NfcSession.transceive` never throws; use `.getOrElse { return Result.failure(it) }` to propagate errors.
- Use `getData()` on `RApdu` to extract the payload; it returns `Result.failure` on non-9000 status.
- Hex encoding/decoding utilities live in `utils/Hex.kt`; the AID is stored as a hex string and decoded via `hexToUByteArray()` at call sites.
- TLV tag constants belong in `Iso7816.Tags`; single-byte tags use `UByte`, multi-byte tags use `UInt`.
- LDS data models live in `lds/model/`, not alongside the use cases.
