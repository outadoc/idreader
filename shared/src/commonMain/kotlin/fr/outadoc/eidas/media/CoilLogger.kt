package fr.outadoc.eidas.media

import coil3.util.Logger
import fr.outadoc.eidas.logging.LogLevel

class CoilLogger(
    private val logger: fr.outadoc.eidas.logging.Logger,
) : Logger {
    override var minLevel: Logger.Level = Logger.Level.Debug

    override fun log(
        tag: String,
        level: Logger.Level,
        message: String?,
        throwable: Throwable?,
    ) {
        val level =
            when (level) {
                Logger.Level.Verbose -> LogLevel.DEBUG
                Logger.Level.Debug -> LogLevel.DEBUG
                Logger.Level.Info -> LogLevel.INFO
                Logger.Level.Warn -> LogLevel.WARN
                Logger.Level.Error -> LogLevel.ERROR
            }

        logger.log(
            level = level,
            tag = tag,
            message = message.orEmpty(),
            throwable = throwable,
        )
    }
}
