package fr.outadoc.eidas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import fr.outadoc.eidas.navigation.Screen
import fr.outadoc.eidas.screen.logs.LogsScreen
import fr.outadoc.eidas.screen.picture.FullScreenPictureScreen
import fr.outadoc.eidas.screen.reader.ReaderScreen
import fr.outadoc.eidas.screen.result.ScanResultScreen

@Composable
fun App() {
    AppTheme {
        val backStack = remember { mutableStateListOf<Screen>(Screen.DEFAULT) }

        val navigate: (Screen) -> Unit = { screen ->
            if (backStack.lastOrNull() != screen) {
                if (screen == Screen.DEFAULT) {
                    backStack.removeAll { it != Screen.Reader }
                } else {
                    backStack.add(screen)
                }
            }
        }

        NavDisplay(
            backStack = backStack,
            entryProvider =
                entryProvider {
                    entry<Screen.Reader> {
                        ReaderScreen(
                            navigate = navigate,
                        )
                    }

                    entry<Screen.Logs> {
                        LogsScreen(
                            navigate = navigate,
                        )
                    }

                    entry<Screen.ScanResult> { screen ->
                        ScanResultScreen(
                            cardInfo = screen.cardInfo,
                            navigate = navigate,
                            onBack = { backStack.removeLastOrNull() },
                        )
                    }

                    entry<Screen.FullScreenPicture> { screen ->
                        FullScreenPictureScreen(
                            documentPicture = screen.documentPicture,
                            onBack = { backStack.removeLastOrNull() },
                        )
                    }
                },
        )
    }
}

@Composable
@Preview
fun AppPreview() {
    App()
}
