package fr.outadoc.eidas

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import fr.outadoc.eidas.logging.MemoryLogger
import fr.outadoc.eidas.navigation.MainNavigationBar
import fr.outadoc.eidas.navigation.Screen
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    modifier: Modifier = Modifier,
    navigate: (Screen) -> Unit = {},
    memoryLogger: MemoryLogger = koinInject(),
) {
    val entries by memoryLogger.entries.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Logs") },
            )
        },
        bottomBar = {
            MainNavigationBar(
                selected = Screen.Logs,
                navigate = navigate,
            )
        },
    ) { insets ->
        TerminalView(
            modifier = Modifier.fillMaxSize(),
            entries = entries,
            insets = insets,
        )
    }
}
