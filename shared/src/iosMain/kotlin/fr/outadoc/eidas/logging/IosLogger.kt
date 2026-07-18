package fr.outadoc.eidas.logging

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ptr
import platform.darwin.OS_LOG_TYPE_DEBUG
import platform.darwin.OS_LOG_TYPE_ERROR
import platform.darwin.OS_LOG_TYPE_FAULT
import platform.darwin.OS_LOG_TYPE_INFO
import platform.darwin.__dso_handle
import platform.darwin._os_log_internal
import platform.darwin.os_log_create
import platform.darwin.os_log_t

class IosLogger : Logger {
    @OptIn(ExperimentalForeignApi::class)
    override fun log(
        level: LogLevel,
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        val suffix = throwable?.let { "\n${it.stackTraceToString()}" } ?: ""

        val osLog: os_log_t =
            os_log_create(
                subsystem = "fr.outadoc.idreader",
                category = "trx/$tag",
            )

        _os_log_internal(
            dso = __dso_handle.ptr,
            log = osLog,
            type =
                when (level) {
                    LogLevel.DEBUG -> OS_LOG_TYPE_DEBUG
                    LogLevel.INFO -> OS_LOG_TYPE_INFO
                    LogLevel.WARN -> OS_LOG_TYPE_ERROR
                    LogLevel.ERROR -> OS_LOG_TYPE_FAULT
                },
            message = (message + suffix).replace("%", "%%"),
        )
    }
}
