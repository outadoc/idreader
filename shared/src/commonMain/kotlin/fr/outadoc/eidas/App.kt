package fr.outadoc.eidas

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.MemoryLogger
import fr.outadoc.eidas.logging.e
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.Aid
import fr.outadoc.eidas.nfc.CApdu
import fr.outadoc.eidas.nfc.NfcException
import fr.outadoc.eidas.nfc.NfcTagReader
import fr.outadoc.eidas.utils.toPrettyHex
import org.koin.compose.koinInject

private const val TAG = "App"

@Composable
fun App(
    memoryLogger: MemoryLogger = koinInject(),
    logger: Logger = koinInject(),
    tagReader: NfcTagReader = koinInject(),
) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) {
            darkColorScheme()
        } else {
            lightColorScheme()
        }
    ) {
        val entries by memoryLogger.entries.collectAsState()

        LaunchedEffect(tagReader) {
            try {
                tagReader.detectedTags.collect { tag ->
                    logger.i(
                        TAG,
                        "Tag detected: uid=${tag.id.toPrettyHex()}, ${tag.description}"
                    )

                    val selectResponse = tagReader.transceive(tag, CApdu.selectAid(Aid.MRTD))

                    logger.i(TAG, "SELECT AID response: $selectResponse")
                }
            } catch (e: NfcException) {
                logger.e(TAG, "NFC error", e)
            }
        }

        Scaffold { insets ->
            TerminalView(
                entries = entries,
                modifier = Modifier
                    .padding(insets)
                    .background(Color(0xFF0D0D0D))
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
