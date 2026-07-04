package fr.outadoc.eidas.logging

class NoopLogger : Logger {
    override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) = Unit
}
