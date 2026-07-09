import ProjectDescription

let project = Project(
    name: "iosApp",
    targets: [
        .target(
            name: "iosApp",
            destinations: .iOS,
            product: .app,
            bundleId: "dev.tuist.iosApp",
            infoPlist: .extendingDefault(
                with: [
                    "UILaunchScreen": [
                        "UIColorName": "",
                        "UIImageName": "",
                    ],
                ]
            ),
            buildableFolders: [
                "iosApp/Sources",
                "iosApp/Resources",
            ],
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
            dependencies: [],
            settings: .settings(
                base: [
                    "ENABLE_USER_SCRIPT_SANDBOXING": "NO"
                ]
            )
        ),
        .target(
            name: "iosAppTests",
            destinations: .iOS,
            product: .unitTests,
            bundleId: "dev.tuist.iosAppTests",
            infoPlist: .default,
            buildableFolders: [
                "iosApp/Tests"
            ],
            dependencies: [.target(name: "iosApp")]
        ),
    ]
)
