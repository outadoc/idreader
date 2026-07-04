package fr.outadoc.eidas.logging

interface Logger {
    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null)
}

enum class LogLevel { DEBUG, INFO, WARN, ERROR }

fun Logger.d(tag: String, message: String, throwable: Throwable? = null) =
    log(LogLevel.DEBUG, tag, message, throwable)

fun Logger.i(tag: String, message: String, throwable: Throwable? = null) =
    log(LogLevel.INFO, tag, message, throwable)

fun Logger.w(tag: String, message: String, throwable: Throwable? = null) =
    log(LogLevel.WARN, tag, message, throwable)

fun Logger.e(tag: String, message: String, throwable: Throwable? = null) =
    log(LogLevel.ERROR, tag, message, throwable)
