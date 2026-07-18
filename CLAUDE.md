# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A mobile app for reading EU ID cards and passports via NFC, implementing the [BSI TR-03110](https://www.bsi.bund.de/EN/Themen/Unternehmen-und-Organisationen/Standards-und-Zertifizierung/Technische-Richtlinien/TR-nach-Thema-sortiert/tr03110/TR-03110_node.html) standard (Advanced Security Mechanisms for Machine Readable Travel Documents / eIDAS) and ICAO Doc 9303. Built with Kotlin Multiplatform and Compose Multiplatform, targeting **Android** and **iOS**.

The app performs the full PACE authentication handshake and LDS data-group reads entirely on-device: no network access, no server round-trips, and the document password (CAN/PIN/etc.) is stored at rest encrypted (Android Keystore / iOS Keychain).

## Commands

```bash
# Build debug APK
./gradlew :androidApp:assembleDebug

# Run all shared-module tests (JVM host, not a device)
./gradlew :shared:testAndroidHostTest

# Run a single test class
./gradlew :shared:testAndroidHostTest --tests "fr.outadoc.eidas.pace.PaceGetNonceUseCaseTest"
```

There is no linter/formatter task configured (no ktlint/detekt/spotless) — match the existing style by hand.

iOS is a **Tuist** project, not a raw `.xcodeproj`: open/build via `tuist generate` (or Xcode after generating) from `iosApp/`. The generated app target runs a pre-build script that invokes `./gradlew :shared:embedAndSignAppleFrameworkForXcode` to build and embed the KMP `Shared.framework`, so a plain Xcode build implicitly triggers a Gradle build first. There is no Kotlin/Native iOS toolchain on Linux — iOS-side Kotlin (`iosMain`) and Swift code can only be compiled/tested on macOS.

## Architecture

Two Gradle modules plus a Tuist-managed iOS app:

- **`androidApp`** — thin Android entry point; wires Koin DI, builds Coil's `ImageLoader`, hosts the Compose activity.
- **`shared`** — all business logic and Compose UI, structured as KMP source sets:
  - `commonMain` — platform-agnostic code, interfaces, and Compose UI
  - `androidMain` — Android implementations of platform interfaces
  - `androidHostTest` — tests running on JVM (no device required)
  - `iosMain` — iOS implementations of platform interfaces (targets `iosArm64` only)
- **`iosApp`** — Tuist project (`Project.swift` + `Tuist/Package.swift` for SPM dependencies) hosting the SwiftUI shell and Swift implementations of a few Kotlin interfaces that need native platform crypto/keychain APIs unreachable from Kotlin/Native. See "iOS app (Tuist + Swift)" below.

### Platform abstraction: two different patterns

Most platform-specific behavior (`CryptoEngine`, `KeyGenerator`, `SettingsEncryptor`, `NfcTagReader`, `Logger`) is a **plain `commonMain` interface**, implemented once per platform, and wired together via **Koin DI** — not Kotlin's `expect`/`actual`. This lets the iOS implementations of `CryptoEngine`/`KeyGenerator`/`SettingsEncryptor` live in **Swift** (`iosApp/iosApp/Sources/`) instead of `iosMain` Kotlin, since they lean on Swift-only libraries (SwiftECC, CryptoKit, Keychain) — Swift instantiates them and hands them to `MainViewController(cryptoEngine:keyGenerator:settingsEncryptor:)`, which registers them into Koin via `externalIosModule(...)`. `NfcTagReader` and `Logger`, by contrast, don't need Swift-only APIs, so their iOS implementations are plain Kotlin in `iosMain`.

`Jpeg2000Decoder` (Coil3 decoder) is the one place that genuinely uses `expect`/`actual`: `commonMain` declares `expect class Jpeg2000Decoder(source: ImageSource) : Decoder`, and each platform provides an `actual class` — Android decodes via the third-party `com.gemalto.jp2.JP2Decoder`, iOS decodes via ImageIO (`CGImageSourceCreateWithData`/`CGImageSourceCreateImageAtIndex`) since neither Android's default `BitmapFactory` nor Coil3's built-in Skia decoder support JPEG2000. `Jpeg2000Factory` (magic-byte sniffing + `Decoder.Factory`) lives in `commonMain` and is shared as-is. Each platform's entry point (`MainActivity.kt` / `MainViewController.kt`) registers `Jpeg2000Factory()` plus `DocumentPictureFetcher.Factory()`/`DocumentPictureKeyer()` (also `commonMain`, platform-agnostic) into its `ImageLoader.Builder { .components { ... } }`.

### KMP ↔ Swift interop utilities (`shared/.../utils/`)

Two wrapper types exist purely to cross the Kotlin/Native Objective-C interop boundary cleanly, since the plain Kotlin types don't bridge well:

- `KmpBytes(raw: ByteArray)` — wraps `ByteArray` (Kotlin `ByteArray` becomes an awkward `KotlinByteArray` in Swift, converted via `.get(index:)`/`.set(index:value:)`, not a subscript). Any `CryptoEngine`/`PrivateKey`/`PublicKey`/`EcPoint` method that crosses into Swift uses `KmpBytes`, not raw `ByteArray`/`UByteArray`.
- `KmpResult<T>` — wraps `kotlin.Result<T>` (which doesn't bridge to Swift at all) with `isSuccess`/`getOrNull`/`getOrThrow`/`onSuccess`/`onFailure` plus companion factories `success`/`failure`. `SettingsEncryptor.encrypt`/`decrypt` return `KmpResult<String>` specifically so Swift can construct/consume them. **Constructing one from Swift requires `Type<T>.companion.factory(...) as! Type<T>`** — writing `let x: KmpResult<NSString> = KmpResult.companion.success(...)` (bare `KmpResult`, relying on return-type inference) does not compile; Swift infers the wrong generic parameter.

### NFC / APDU layer (`shared/.../nfc/`)

- **`NfcTagReader`** — `waitForTag(): Flow<NfcSession>`. Collecting starts NFC discovery; cancelling stops it. Throws `NfcException` if NFC is unavailable.
- **`NfcSession`** — `transceive(CApdu): Result<RApdu>` (never throws — failures come back as `Result.failure(NfcException)`) plus a `commandCount: StateFlow<Int>`.
- `AndroidNfcTagReader`/`AndroidNfcSession` (androidMain) wrap `android.nfc.tech.IsoDep`. `IosNfcTagReader`/`IosNfcSession` (iosMain) wrap CoreNFC's `NFCTagReaderSession`/`NFCISO7816Tag` (`platform.CoreNFC`) — note iOS sessions effectively yield one tag per collection (the system NFC sheet dismisses after a read, and sessions auto-invalidate after ~60s), unlike Android's reusable reader-mode session.
- `CApdu` models a command APDU built via `CommandFactory` (`nfc/commands/`). `RApdu` exposes `sw1`/`sw2`, `isSuccess`, and `getData(): Result<UByteArray>` (fails with `ApduException` on non-9000 status — distinct from `NfcException`, which is for transport/session-level failures).
- `Icao9303` (formerly named `Iso7816`) is a top-level constants object with nested `Aid` (hex strings, e.g. `Aid.MRTD`, decoded via `hexToUByteArray()` at call sites), `File`, `DataGroup` (DG1–16), `KeyRef`, and `Tags`; single-byte tags are `UByte`, multi-byte tags are `UInt`.
- `pace/DynAuthExt.kt`'s `UByteArray.parseDynamicAuthData(): Result<TLVList>` follows the same `Result`-based convention as the TLV parser, for PACE's General Authenticate response payloads.

### TLV layer (`shared/.../tlv/`)

- `TlvNode(tag: UInt, value: UByteArray)` — a parsed TLV node; `children()` re-parses `value` as a nested TLV list.
- `UByteArray.parseTlv(): Result<List<TlvNode>>` — parses a flat byte array into a TLV list.
- `List<TlvNode>.firstWithTag(tag: UInt/UByte): TlvNode?` — tag lookup; `UByte` overload widens to `UInt`.
- `buildTlv { }` / `TlvBuilder` — DSL for constructing TLV-encoded byte arrays. `nfc/Tlv.kt`'s `tlvList { }` is a thin NFC-specific convenience wrapper around the same builder.

### Error handling convention

Railway-oriented style using `kotlin.Result` throughout use cases and APDU-touching code:

- `nfcSession.transceive(...).getOrElse { return Result.failure(it) }` — propagates NFC transport errors
- `.getData().getOrElse { return Result.failure(it) }` — propagates APDU status errors (`ApduException`)
- Crypto/parsing blocks that can throw are wrapped in `runCatching { ... }` and returned directly
- All five PACE use cases and the LDS read/parse use cases return `Result<T>`; orchestrators (`PaceAuthenticateUseCase`, `ReadCardDataUseCase`) chain them with `getOrElse { return Result.failure(it) }`.
- `ReaderViewModel` catches errors at the outer `try/catch` level around `collect { }`.

### Crypto layer (`shared/.../crypto/`)

`CryptoEngine` is the interface both platforms implement (EC point math, AES-CBC, AES-CMAC, SHA-1/256, KDF). It's scoped narrowly: only the `PACE_ECDH_GM_AES_CBC_CMAC_256` `Protocol` is actually implemented on either platform (used with `BRAINPOOLP256R1`/`SECP256R1` per `Algorithm.preferredAlgorithms`); other `Protocol`/`DomainParameter` cases exist in the enums but throw/`fatalError()`.

- **Android** (`AndroidCryptoEngine`, `AndroidKeyGenerator`) — built on BouncyCastle. `AlgoExt.kt`'s `DomainParameter.ecParams()` maps to BouncyCastle's named-curve `X9ECParameters`.
- **iOS** (`iosApp/iosApp/Sources/SwiftCryptoEngine.swift`, `SwiftKeyGenerator.swift`) — built on three different libraries since no single Apple framework covers everything needed: **SwiftECC** (`leif-ibsen/SwiftECC` via SPM) for EC point arithmetic, because CryptoKit has no Brainpool curve support; **CommonCrypto** for AES-CBC and a hand-rolled RFC 4493 AES-CMAC (CryptoKit exposes neither raw CBC nor CMAC); **CryptoKit** for SHA-1/SHA-256. `DomainParameterExt.swift`/`ECFieldElement.swift` (also in `iosApp/iosApp/Sources/`) hold the `DomainParameter → SwiftECC.Domain` mapping and fixed-width field-element encoding helpers, mirroring `AlgoExt.kt`.
- `PrivateKey`/`PublicKey`/`EcPoint`/`KeyPair` are plain `commonMain` interfaces/data classes; each platform has its own concrete implementations (`AndroidPrivateKey`/`AndroidPublicKey` vs. Swift's `SwiftPrivateKey`/`SwiftPublicKey`).

### PACE authentication flow (`shared/.../pace/`)

BSI TR-03110 PACE in five steps, each a use case, orchestrated by `PaceAuthenticateUseCase`:

1. `ReadCardAccessUseCase` — SELECT + READ BINARY EF.CardAccess, parse `SecurityInfo` to discover supported algorithms
2. `PaceGetNonceUseCase` — MSE:Set AT + General Authenticate step 1; derives key from CAN, decrypts chip nonce
3. `PaceMapNonceUseCase` — General Authenticate step 2 (generic mapping); produces mapped EC generator G′
4. `PaceKeyAgreementUseCase` — General Authenticate step 3; ECDH, derives session keys K_enc/K_mac
5. `PaceMutualAuthUseCase` — General Authenticate step 4; verifies CMAC tokens both ways

### Secure messaging (`shared/.../securemessaging/`)

`SecureMessagingSession` implements `NfcSession` as a decorator over a plain `NfcSession`, created via `SecureSessionFactory` after PACE succeeds. Maintains a 128-bit Send Sequence Counter (SSC) and applies TR-03110 SM (AES-CBC + CMAC) on every command/response pair.

### LDS layer (`shared/.../lds/`)

Post-PACE reads over the secure session, orchestrated by `ReadCardDataUseCase` (selects the MRTD AID, reads EF.COM to discover available DGs via `ReadComFileUseCase`, reads each DG via `ReadDataGroupUseCase`/`ReadWholeFileUseCase`, then parses the known ones). Per-DG parsers (`ParseDG1UseCase`, `ParseDG2UseCase` for the face photo, `ParseDG11UseCase`, `ParseDG13UseCase`, ...) and shared helpers (`ParseMrzUseCase`, `ParseMrzNameUseCase`, `Parse6DigitDateUseCase`, `Parse8DigitDateUseCase`) each live in their own file. Models live in `lds/model/`, not alongside the use cases (`CardDump`, `MrzInfo`, `CardHolderName`, `DocumentPicture`, `Date`, `Centimeters`, `OptionalDetails`, ...).

### Settings encryption (`shared/.../settings/`)

`SettingsEncryptor.encrypt`/`decrypt` return `KmpResult<String>` (see the interop section above). Used by `DataStoreSettingsRepository` to store the CAN/password at rest; decrypt/encrypt failures are logged and fall back to an empty string rather than surfacing an error to the UI.

- **Android** (`AndroidSettingsEncryptor`) — Android Keystore (lazily-generated AES-256-GCM key), IV+ciphertext Base64-encoded.
- **iOS** (`iosApp/iosApp/Sources/SwiftSettingsEncryptor.swift`) — a lazily-generated `SymmetricKey` persisted in the Keychain (`kSecClassGenericPassword`), encrypted with `CryptoKit.AES.GCM`. `sealedBox.combined` (nonce‖ciphertext‖tag) is Base64-encoded the same way, so both platforms produce structurally equivalent output even though the keys themselves are never shared.

### Logging (`shared/.../logging/`)

- `Logger` — common interface with `d/i/w/e` extension helpers; `LogLevel` has 4 levels (no separate "verbose").
- `MemoryLogger` — wraps an optional delegate and accumulates entries into a `StateFlow<List<LogEntry>>` observed by the UI (`LogsScreen`).
- `AndroidLogger` delegates to `android.util.Log`. `IosLogger` uses the C `os_log` unified-logging API (`platform.darwin`'s `os_log_create`/`_os_log_internal`, category `"trx/$tag"`) — **not** `NSLog`: `NSLog`/`os_log`'s C varargs don't auto-bridge a raw Kotlin `String`, and calling them with one crashes; a message needs a single already-built string argument, and `%` must be escaped since the message is treated as a printf-style format string.

## Key Conventions

- Never access `android.nfc`/CoreNFC/CoreGraphics/etc. outside their respective platform source sets, or Swift files, per the DI pattern above.
- `NfcSession.transceive` never throws; use `.getOrElse { return Result.failure(it) }` to propagate errors.
- Use `getData()` on `RApdu` to extract the payload; it returns `Result.failure(ApduException)` on non-9000 status.
- Hex encoding/decoding utilities live in `utils/Hex.kt`; the AID is stored as a hex string and decoded via `hexToUByteArray()` at call sites.
- TLV tag constants belong in `Icao9303.Tags`; single-byte tags use `UByte`, multi-byte tags use `UInt`.
- LDS data models live in `lds/model/`, not alongside the use cases.
- Always use explicit named parameters at Kotlin call sites in this codebase's own code (third-party APIs where the exact parameter names aren't verified are the one exception).

## iOS app (Tuist + Swift)

- Managed by Tuist, not a committed `.xcodeproj`: `iosApp/Project.swift` declares the app + test targets, entitlements (NFC reader-session formats), and Info.plist keys (including the NFC AID allow-list and app display name); `iosApp/Tuist/Package.swift` declares SPM dependencies (currently just `SwiftECC`).
- `iosApp/iosApp/Sources/` holds `IosApp.swift` (SwiftUI `@main`), `ContentView.swift` (instantiates `SwiftCryptoEngine`/`SwiftKeyGenerator`/`SwiftSettingsEncryptor` and calls the Kotlin-exported `MainViewController(...)`), plus the Swift `CryptoEngine`/`KeyGenerator`/`SettingsEncryptor` implementations and their supporting bridging/curve-math files described above.
- Kotlin/Native's Objective-C export requires a few non-obvious patterns when writing Swift code against the `Shared` framework: `SomeKotlinClass.companion.someMember(...)` for companion-object access; `KotlinByteArray.get(index:)`/`.set(index:value:)` (not a subscript) for bridging `KmpBytes.raw`; and — as noted above — `Type<T>.companion.factory(...) as! Type<T>` for constructing a `KmpResult<T>`.
