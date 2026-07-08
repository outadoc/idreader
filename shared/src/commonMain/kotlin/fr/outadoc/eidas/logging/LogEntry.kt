package fr.outadoc.eidas.logging

import androidx.compose.runtime.Immutable

@Immutable
data class LogEntry(
    val level: LogLevel,
    val tag: String,
    val message: String,
    val throwable: Throwable? = null,
)
