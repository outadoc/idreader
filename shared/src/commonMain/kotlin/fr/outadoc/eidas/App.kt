package fr.outadoc.eidas

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.outadoc.eidas.icons.AppIcons
import fr.outadoc.eidas.icons.settings
import fr.outadoc.eidas.logging.MemoryLogger
import fr.outadoc.eidas.settings.SettingsScreen
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    memoryLogger: MemoryLogger = koinInject(),
    viewModel: ReaderViewModel = koinInject(),
) {
    AppTheme {
        val entries by memoryLogger.entries.collectAsState()
        var showSettings by remember { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState()

        LaunchedEffect(viewModel) {
            viewModel.startListening()
        }

        val state by viewModel.state.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("eIDAS Reader") },
                    actions = {
                        IconButton(
                            onClick = { showSettings = true },
                        ) {
                            Icon(
                                imageVector = AppIcons.settings,
                                contentDescription = "Open settings",
                            )
                        }
                    },
                )
            },
        ) { insets ->
            TerminalView(
                modifier = Modifier.fillMaxSize(),
                entries = entries,
                insets = insets,
            )
        }

        state.cardDump?.let { cardDump ->
            ModalBottomSheet(
                onDismissRequest = {},
                sheetState = sheetState,
            ) {
                CardInfo(
                    modifier = Modifier.padding(16.dp),
                    cardDump = cardDump,
                )
            }
        }

        if (showSettings) {
            ModalBottomSheet(
                onDismissRequest = { showSettings = false },
                sheetState = sheetState,
            ) {
                SettingsScreen()
            }
        }
    }
}

@Composable
@Preview
fun AppPreview() {
    App()
}
