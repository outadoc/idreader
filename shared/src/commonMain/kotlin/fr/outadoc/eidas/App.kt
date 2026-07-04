package fr.outadoc.eidas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.outadoc.eidas.logging.LogLevel
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.NfcException
import fr.outadoc.eidas.nfc.NfcTag
import fr.outadoc.eidas.nfc.NfcTagReader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

private const val TAG = "App"

@Composable
fun App(tagReader: NfcTagReader, logger: Logger) {
    MaterialTheme {
        var detectedTag by remember { mutableStateOf<NfcTag?>(null) }
        var error by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(tagReader) {
            try {
                tagReader.detectedTags.collect { tag ->
                    logger.i(TAG, "Tag detected: uid=${tag.id.toHexString()}\n${tag.description}")
                    detectedTag = tag
                }
            } catch (e: NfcException) {
                error = e.message
            }
        }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Hold an NFC tag near the phone",
                style = MaterialTheme.typography.titleMedium,
            )

            error?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            detectedTag?.let { tag ->
                Text(
                    text = "Tag detected",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text("UID: ${tag.id.toHexString()}")
                Text(tag.description)
            }
        }
    }
}

@Composable
@Preview
fun AppPreview() {
    App(
        tagReader = object : NfcTagReader {
            override val detectedTags: Flow<NfcTag> = emptyFlow()
            override suspend fun transceive(tag: NfcTag, command: ByteArray): ByteArray {
                throw NfcException("Not supported in preview")
            }
        },
        logger = object : Logger {
            override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) = Unit
        },
    )
}
