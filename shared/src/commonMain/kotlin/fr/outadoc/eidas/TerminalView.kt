package fr.outadoc.eidas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.outadoc.eidas.logging.LogEntry
import fr.outadoc.eidas.logging.LogLevel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun TerminalView(
    entries: ImmutableList<LogEntry>,
    modifier: Modifier = Modifier,
    insets: PaddingValues = PaddingValues(),
) {
    val listState = rememberLazyListState()

    LaunchedEffect(entries.size) {
        if (entries.isNotEmpty()) {
            listState.animateScrollToItem(entries.lastIndex)
        }
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = insets + PaddingValues(16.dp),
    ) {
        items(entries) { entry ->
            val color =
                colorForLevel(
                    entry.level,
                    MaterialTheme.extendedColorScheme,
                )
            Column {
                Text(
                    text = formatHeader(entry),
                    color = color,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )

                Text(
                    text = formatEntry(entry),
                    color = color,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun TerminalViewPreview() {
    AppTheme {
        TerminalView(
            persistentListOf(
                LogEntry(
                    level = LogLevel.DEBUG,
                    tag = "LoremIpsum",
                    message = "Lorem ipsum dolor sit amet.",
                ),
                LogEntry(
                    level = LogLevel.INFO,
                    tag = "LoremIpsum",
                    message = "Lorem ipsum dolor sit amet.",
                ),
                LogEntry(
                    level = LogLevel.WARN,
                    tag = "LoremIpsum",
                    message = "Lorem ipsum dolor sit amet.",
                ),
                LogEntry(
                    level = LogLevel.ERROR,
                    tag = "LoremIpsum",
                    message = "Lorem ipsum dolor sit amet.",
                ),
            ),
        )
    }
}

private fun formatHeader(entry: LogEntry): String {
    val level =
        when (entry.level) {
            LogLevel.DEBUG -> "D"
            LogLevel.INFO -> "I"
            LogLevel.WARN -> "W"
            LogLevel.ERROR -> "E"
        }
    return "[$level/${entry.tag}]"
}

private fun formatEntry(entry: LogEntry): String {
    val base = entry.message
    return if (entry.throwable != null) {
        "$base\n${entry.throwable}"
    } else {
        base
    }
}

private fun colorForLevel(
    level: LogLevel,
    colors: ExtendedColorScheme,
): Color =
    when (level) {
        LogLevel.DEBUG -> colors.logDebug
        LogLevel.INFO -> colors.logInfo
        LogLevel.WARN -> colors.logWarn
        LogLevel.ERROR -> colors.logError
    }
