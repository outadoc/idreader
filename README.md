# Mobile ID Reader

<img src="assets/ic_launcher.png" alt="App icon" width="200">

This project contains an application capable of reading an EU ID card via NFC using a
smartphone.

The project was inspired by [hufon/cnie-python-tools][cnie-python-tools] and its code was used 
as a reference in some places.

## Download

[<img src="https://github.com/ImranR98/Obtainium/raw/main/assets/graphics/badge_obtainium.png" alt="Get it on Obtainium" height="56">](https://apps.obtainium.imranr.dev/redirect?r=obtainium://app/%7B%22id%22%3A%22fr.outadoc.eidas%22%2C%22url%22%3A%22https%3A%2F%2FGitHub.com%2Foutadoc%2Fidreader%22%2C%22author%22%3A%22outadoc%22%2C%22name%22%3A%22ID%20Reader%22%2C%22preferredApkIndex%22%3A0%2C%22additionalSettings%22%3A%22%7B%5C%22includePrereleases%5C%22%3Afalse%2C%5C%22fallbackToOlderReleases%5C%22%3Atrue%2C%5C%22filterReleaseTitlesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22filterReleaseNotesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22verifyLatestTag%5C%22%3Afalse%2C%5C%22sortMethodChoice%5C%22%3A%5C%22date%5C%22%2C%5C%22useLatestAssetDateAsReleaseDate%5C%22%3Afalse%2C%5C%22releaseTitleAsVersion%5C%22%3Afalse%2C%5C%22trackOnly%5C%22%3Afalse%2C%5C%22versionExtractionRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22matchGroupToUse%5C%22%3A%5C%22%5C%22%2C%5C%22versionDetection%5C%22%3Afalse%2C%5C%22releaseDateAsVersion%5C%22%3Afalse%2C%5C%22useVersionCodeAsOSVersion%5C%22%3Afalse%2C%5C%22apkFilterRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22invertAPKFilter%5C%22%3Afalse%2C%5C%22autoApkFilterByArch%5C%22%3Atrue%2C%5C%22appName%5C%22%3A%5C%22%5C%22%2C%5C%22appAuthor%5C%22%3A%5C%22%5C%22%2C%5C%22shizukuPretendToBeGooglePlay%5C%22%3Afalse%2C%5C%22allowInsecure%5C%22%3Afalse%2C%5C%22exemptFromBackgroundUpdates%5C%22%3Afalse%2C%5C%22skipUpdateNotifications%5C%22%3Afalse%2C%5C%22about%5C%22%3A%5C%22%5C%22%2C%5C%22refreshBeforeDownload%5C%22%3Afalse%2C%5C%22includeZips%5C%22%3Afalse%2C%5C%22zippedApkFilterRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22includeTarballs%5C%22%3Afalse%2C%5C%22tarballedApkFilterRegEx%5C%22%3A%5C%22%5C%22%7D%22%2C%22overrideSource%22%3Anull%7D)

Or download the APK from the [latest release](https://github.com/outadoc/idreader/releases/latest).

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
