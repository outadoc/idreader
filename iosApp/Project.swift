import ProjectDescription

let project = Project(
    name: "iosApp",
    targets: [
        .target(
            name: "iosApp",
            destinations: .iOS,
            product: .app,
            bundleId: "fr.outadoc.idreader",
            infoPlist: .extendingDefault(
                with: [
                    "UILaunchScreen": [
                        "UIColorName": "",
                        "UIImageName": "",
                    ],
                    "NFCReaderUsageDescription": "This app reads your identity document over NFC.",
                    "CADisableMinimumFrameDurationOnPhone": true,
                    // Despite the prefix, this is an Info.plist key, not an
                    // entitlement. ISO 7816 tags are only reported for the
                    // AIDs listed here (A0000002471001 = MRTD).
                    "com.apple.developer.nfc.readersession.iso7816.select-identifiers": [
                        "A0000002471001",
                    ],
                ]
            ),
            buildableFolders: [
                "iosApp/Sources",
                "iosApp/Resources",
            ],
            entitlements: .dictionary([
                "com.apple.developer.nfc.readersession.formats": .array([.string("TAG")]),
            ]),
            scripts: [
                .pre(
                    script: """
                        if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
                            echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \\"YES\\""
                            exit 0
                        fi
                        cd "$SRCROOT/.."
                        ./gradlew :shared:embedAndSignAppleFrameworkForXcode
                        """,
                    name: "Build Shared KMP Module",
                    basedOnDependencyAnalysis: false
                )
            ],
            dependencies: [
                // The static Shared framework doesn't autolink CoreNFC;
                // link it explicitly at the app level.
                .sdk(name: "CoreNFC", type: .framework, status: .required),
            ],
            settings: .settings(
                base: [
                    "ENABLE_USER_SCRIPT_SANDBOXING": "NO",
                    "CODE_SIGN_STYLE": "Automatic",
                    "DEVELOPMENT_TEAM": "C38RDC5QNT"
                ]
            )
        ),
        .target(
            name: "iosAppTests",
            destinations: .iOS,
            product: .unitTests,
            bundleId: "fr.outadoc.eidas.tests",
            infoPlist: .default,
            buildableFolders: [
                "iosApp/Tests"
            ],
            dependencies: [.target(name: "iosApp")]
        ),
    ]
)
