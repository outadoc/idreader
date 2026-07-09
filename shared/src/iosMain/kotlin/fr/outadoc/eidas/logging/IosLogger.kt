package fr.outadoc.eidas.logging

import platform.Foundation.NSLog

class IosLogger : Logger {
    override fun log(
        level: LogLevel,
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        val suffix = throwable?.let { "\n${it.stackTraceToString()}" } ?: ""
        // NSLog("%@", "[${level.name}] trx/$tag: $message$suffix")
    }
}
