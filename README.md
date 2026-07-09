# eIDAS

This project contains an application capable of reading an EU passport or ID card via NFC using a
smartphone.

All the business logic is contained within the `shared` module. The `androidApp` module only
contains the minimal required entry points.

The project is build with Kotlin Multiplatform and targets Android, with a possible iOS target in
the future.

### References

- [ICAO Doc 9303][icao-9303]
- [BSI TR-03110][bsi-tr-03110]
- [hufon/cnie-python-tools][cnie-python-tools]

### Structure

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform
  applications.
  It contains several subfolders:
    - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
    - Other folders are for Kotlin code that will be compiled for only the platform indicated in the
      folder name.
      For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
      the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
      Similarly, if you want to edit the Desktop (JVM) specific part,
      the [jvmMain](./shared/src/jvmMain/kotlin)
      folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these
commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`

[icao-9303]: https://www.icao.int/publications/doc-series/doc-9303

[bsi-tr-03110]: https://www.bsi.bund.de/EN/Themen/Unternehmen-und-Organisationen/Standards-und-Zertifizierung/Technische-Richtlinien/TR-nach-Thema-sortiert/tr03110/tr-03110.html

[cnie-python-tools]: https://github.com/hufon/cnie-python-tools/tree/main/cnie
