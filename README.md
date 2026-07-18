# Mobile ID Reader

<img src="assets/ic_launcher.png" alt="App icon" width="200">

This project contains an application capable of reading an EU ID card via NFC using a
smartphone.

The project was inspired by [hufon/cnie-python-tools][cnie-python-tools] and its code was used 
as a reference in some places.

## Download

Download the APK from the [latest release](https://github.com/outadoc/idreader/releases/latest).

## Privacy policy

- The app does **not** have access to any biometrics data deemed sensitive by the issuer of your
  document, such as your fingerprints.
- The app does **not** extract any data to any server; in fact, it doesn't even have permission to 
connect to the Internet.
- The app does **not** store any of the data from your document on your device.
- All data exchanges with your document use standard and public communication described in the 
referenced documents; it doesn't do any weird magic, just (admittedly weird) math.
- The password of your document (e.g. the CAN) is stored at rest **encrypted** by the Android 
Keystore with a strong encryption key.

## Compatibility

### Known compatible documents

- French CNIe (issued April 2024)

### Known compatible devices

- Samsung Z Flip6 (Android 16)

## References

- [ICAO Doc 9303][icao-9303]
- [BSI TR-03110][bsi-tr-03110]

## Structure

All the business logic is contained within the `shared` module. The `androidApp` module only
contains the minimal required entry points. The project is build with Kotlin and Compose 
Multiplatform.

### Supported targets

- Android ✅
- iOS ✅ (not published for now)

## AI Disclosure

An AI agent was used here and there to perform some grunt work or help me when I was stuck, e.g. for
the last exchanges of the PACE algorithm. The architectural decisions, implementation details,
research work, and the rest of the actual implementation were performed by me. Some code
documentation was auto-generated as well.

[icao-9303]: https://www.icao.int/publications/doc-series/doc-9303

[bsi-tr-03110]: https://www.bsi.bund.de/EN/Themen/Unternehmen-und-Organisationen/Standards-und-Zertifizierung/Technische-Richtlinien/TR-nach-Thema-sortiert/tr03110/tr-03110.html

[cnie-python-tools]: https://github.com/hufon/cnie-python-tools/tree/main/cnie
