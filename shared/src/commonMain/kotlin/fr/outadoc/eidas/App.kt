package fr.outadoc.eidas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import fr.outadoc.eidas.navigation.Screen
import org.koin.compose.koinInject

@Composable
fun App(viewModel: ReaderViewModel = koinInject()) {
    AppTheme {
        LaunchedEffect(viewModel) {
            viewModel.startListening()
        }

        val state by viewModel.state.collectAsState()

        val backStack = remember { mutableStateListOf<Screen>(Screen.Reader) }

        val onSelectTab: (Screen) -> Unit = { screen ->
            if (backStack.lastOrNull() != screen) {
                when (screen) {
                    Screen.Reader -> backStack.removeAll { it != Screen.Reader }
                    Screen.Logs -> backStack.add(Screen.Logs)
                    is Screen.ScanResult -> Unit
                }
            }
        }

        LaunchedEffect(state.cardInfo) {
            state.cardInfo?.let { cardInfo ->
                backStack.add(Screen.ScanResult(cardInfo = cardInfo))
                viewModel.dismissCardInfo()
            }
        }

        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider =
                entryProvider {
                    entry<Screen.Reader> {
                        ReaderScreen(onSelectTab = onSelectTab)
                    }

                    entry<Screen.Logs> {
                        LogsScreen(onSelectTab = onSelectTab)
                    }

                    entry<Screen.ScanResult> { screen ->
                        ScanResultScreen(
                            cardInfo = screen.cardInfo,
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
