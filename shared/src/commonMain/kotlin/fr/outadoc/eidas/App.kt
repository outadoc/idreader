package fr.outadoc.eidas

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import fr.outadoc.eidas.icons.AppIcons
import fr.outadoc.eidas.icons.settings
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.MemoryLogger
import fr.outadoc.eidas.logging.e
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.NfcTagReader
import fr.outadoc.eidas.nfc.commands.MseSetAtUseCase
import fr.outadoc.eidas.nfc.commands.SelectUseCase
import org.koin.compose.koinInject

private const val TAG = "App"

@Composable
fun App(
    memoryLogger: MemoryLogger = koinInject(),
    logger: Logger = koinInject(),
    tagReader: NfcTagReader = koinInject(),
    mseSetAtUseCase: MseSetAtUseCase = koinInject(),
    selectUseCase: SelectUseCase = koinInject(),
) {
    AppTheme {
        val entries by memoryLogger.entries.collectAsState()

        LaunchedEffect(tagReader) {
            try {
                tagReader.detectedTags.collect { tag ->
                    logger.i(TAG, "Tag detected: ${tag.description}")

                    tagReader.transceive(tag, mseSetAtUseCase.paceSetAt()).assertSuccess()
                }
            } catch (e: Exception) {
                logger.e(TAG, "Error", e)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("eIDAS Reader") },
                    actions = {
                        IconButton(
                            onClick = {},
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
                entries = entries,
                modifier =
                    Modifier
                        .padding(insets)
                        .fillMaxSize(),
            )
        }
    }
}

@Composable
@Preview
fun AppPreview() {
    App()
}
