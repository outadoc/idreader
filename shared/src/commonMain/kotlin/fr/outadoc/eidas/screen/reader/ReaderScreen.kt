package fr.outadoc.eidas.screen.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import fr.outadoc.eidas.AppTheme
import fr.outadoc.eidas.icons.AppIcons
import fr.outadoc.eidas.icons.terminal
import fr.outadoc.eidas.navigation.Screen
import fr.outadoc.eidas.navigation.Screen.ScanResult
import fr.outadoc.eidas.settings.model.AppSettings
import fr.outadoc.eidas.settings.model.AuthenticationMethod
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    modifier: Modifier = Modifier,
    navigate: (Screen) -> Unit = {},
    viewModel: ReaderViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ReaderViewModel.Event.ScanResultsAvailable -> {
                    navigate(
                        ScanResult(
                            cardInfo = event.cardInfo,
                        ),
                    )
                }
            }
        }
    }

    ReaderScreenContent(
        modifier = modifier,
        state = state,
        navigate = navigate,
        onAuthenticationMethodChanged = viewModel::onAuthenticationMethodChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onStartListeningClicked = viewModel::onStartListeningClicked,
        onStopListeningClicked = viewModel::onStopListeningClicked,
    )
}

@Composable
private fun ReaderScreenContent(
    modifier: Modifier = Modifier,
    state: ReaderViewModel.State,
    onAuthenticationMethodChanged: (AuthenticationMethod) -> Unit = {},
    onPasswordChanged: (String) -> Unit = {},
    onStartListeningClicked: () -> Unit = {},
    onStopListeningClicked: () -> Unit = {},
    navigate: (Screen) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("ID Reader") },
                actions = {
                    IconButton(onClick = { navigate(Screen.Logs) }) {
                        Icon(
                            imageVector = AppIcons.terminal,
                            contentDescription = "Logs",
                        )
                    }
                },
            )
        },
    ) { insets ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(insets),
            contentAlignment = Alignment.Center,
        ) {
            Column {
                when (state) {
                    is ReaderViewModel.State.Idle -> {
                        ReaderIdleContent(
                            settings = state.settings,
                            exception = state.exception,
                            onAuthenticationMethodChanged = onAuthenticationMethodChanged,
                            onPasswordChanged = onPasswordChanged,
                            onStartListeningClicked = onStartListeningClicked,
                        )
                    }

                    is ReaderViewModel.State.WaitingForDocument -> {
                        ReaderWaitingContent(
                            onStopListeningClicked = onStopListeningClicked,
                        )
                    }

                    is ReaderViewModel.State.Reading -> {
                        ReaderReadingContent(
                            commandCount = state.commandCount,
                        )
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ReaderScreenIdlePreview() {
    AppTheme {
        ReaderScreenContent(
            state =
                ReaderViewModel.State.Idle(
                    settings = AppSettings(),
                ),
        )
    }
}
