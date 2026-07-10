package fr.outadoc.eidas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.outadoc.eidas.navigation.MainNavigationBar
import fr.outadoc.eidas.navigation.Screen
import fr.outadoc.eidas.settings.SettingsContent
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
        viewModel.startListening()

        viewModel.scanResults.collect { cardInfo ->
            navigate(
                Screen.ScanResult(cardInfo = cardInfo),
            )
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("eIDAS Reader") },
            )
        },
        bottomBar = {
            MainNavigationBar(
                selected = Screen.Reader,
                navigate = navigate,
            )
        },
    ) { insets ->
        if (state.isReading) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(insets),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            SettingsContent(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(insets),
            )
        }
    }
}
