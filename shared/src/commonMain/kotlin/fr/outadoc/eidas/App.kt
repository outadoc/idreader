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
import fr.outadoc.eidas.di.sharedModule
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.NoopLogger
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.NfcException
import fr.outadoc.eidas.nfc.NfcTag
import fr.outadoc.eidas.nfc.NfcTagReader
import fr.outadoc.eidas.nfc.NoopNfcTagReader
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.module.Module
import org.koin.dsl.module

private const val TAG = "App"

@Composable
fun App(platformModules: List<Module> = emptyList()) {
    KoinApplication(application = {
        modules(listOf(sharedModule) + platformModules)
    }) {
        MaterialTheme {
            val tagReader: NfcTagReader = koinInject()
            val logger: Logger = koinInject()

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
}

@Composable
@Preview
fun AppPreview() {
    App(
        platformModules = listOf(
            module {
                single<Logger> { NoopLogger() }
                single<NfcTagReader> { NoopNfcTagReader() }
            },
        ),
    )
}
