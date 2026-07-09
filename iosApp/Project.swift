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
            dependencies: []
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
