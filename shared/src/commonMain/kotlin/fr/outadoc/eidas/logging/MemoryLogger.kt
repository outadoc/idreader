package fr.outadoc.eidas.logging

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MemoryLogger(private val delegate: Logger? = null) : Logger {
    private val _entries = MutableStateFlow<PersistentList<LogEntry>>(persistentListOf())
    val entries: StateFlow<PersistentList<LogEntry>> = _entries.asStateFlow()

    override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        delegate?.log(level, tag, message, throwable)
        _entries.update { current ->
            current.add(LogEntry(level, tag, message, throwable))
        }
    }
}
