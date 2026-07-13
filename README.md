# Mobile ID Reader

![App icon](assets/ic_launcher.png)

This project contains an application capable of reading an EU ID card via NFC using a
smartphone.

## Compatibility

### Known compatible documents

- French CNIe (issued April 2024)

### Known compatible devices

- Samsung Z Flip6 (Android 16)

## References

- [ICAO Doc 9303][icao-9303]
- [BSI TR-03110][bsi-tr-03110]
- [hufon/cnie-python-tools][cnie-python-tools]

## Structure

All the business logic is contained within the `shared` module. The `androidApp` module only
contains the minimal required entry points. The project is build with Kotlin and Compose 
Multiplatform.

### Supported targets

- Android ✅
- iOS: ❌ WIP

## Download

Download the APK from the [latest release](https://github.com/outadoc/idreader/releases/latest).

[icao-9303]: https://www.icao.int/publications/doc-series/doc-9303

[bsi-tr-03110]: https://www.bsi.bund.de/EN/Themen/Unternehmen-und-Organisationen/Standards-und-Zertifizierung/Technische-Richtlinien/TR-nach-Thema-sortiert/tr03110/tr-03110.html

[cnie-python-tools]: https://github.com/hufon/cnie-python-tools/tree/main/cnie
