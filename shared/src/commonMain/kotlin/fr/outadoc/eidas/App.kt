package fr.outadoc.eidas

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.outadoc.eidas.di.sharedModule
import fr.outadoc.eidas.logging.LogEntry
import fr.outadoc.eidas.logging.LogLevel
import fr.outadoc.eidas.logging.MemoryLogger
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.NfcException
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
        MaterialTheme(
            colorScheme = if (isSystemInDarkTheme()) {
                darkColorScheme()
            } else {
                lightColorScheme()
            }
        ) {
            val memoryLogger: MemoryLogger = koinInject()
            val tagReader: NfcTagReader = koinInject()

            val entries by memoryLogger.entries.collectAsState()

            LaunchedEffect(tagReader) {
                try {
                    tagReader.detectedTags.collect { tag ->
                        memoryLogger.i(
                            TAG,
                            "Tag detected: uid=${tag.id.toHexString()}\n${tag.description}"
                        )
                    }
                } catch (e: NfcException) {
                    memoryLogger.log(
                        LogLevel.ERROR,
                        TAG,
                        e.message ?: "NFC error",
                        e,
                    )
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
}

@Composable
private fun TerminalView(entries: List<LogEntry>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()

    LaunchedEffect(entries.size) {
        if (entries.isNotEmpty()) {
            listState.animateScrollToItem(entries.lastIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.padding(8.dp),
    ) {
        items(entries) { entry ->
            Text(
                text = formatEntry(entry),
                color = colorForLevel(entry.level),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )
        }
    }
}

private fun formatEntry(entry: LogEntry): String {
    val level = when (entry.level) {
        LogLevel.DEBUG -> "D"
        LogLevel.INFO -> "I"
        LogLevel.WARN -> "W"
        LogLevel.ERROR -> "E"
    }
    val base = "[$level/${entry.tag}] ${entry.message}"
    return if (entry.throwable != null) "$base\n${entry.throwable}" else base
}

private fun colorForLevel(level: LogLevel): Color = when (level) {
    LogLevel.DEBUG -> Color(0xFF888888)
    LogLevel.INFO -> Color(0xFFEEEEEE)
    LogLevel.WARN -> Color(0xFFFFCC00)
    LogLevel.ERROR -> Color(0xFFFF4444)
}

@Composable
@Preview
fun AppPreview() {
    App(
        platformModules = listOf(
            module {
                single<NfcTagReader> { NoopNfcTagReader() }
            },
        ),
    )
}
