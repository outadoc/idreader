package fr.outadoc.eidas

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.outadoc.eidas.logging.LogEntry
import fr.outadoc.eidas.logging.LogLevel

@Composable
fun TerminalView(entries: List<LogEntry>, modifier: Modifier = Modifier) {
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
