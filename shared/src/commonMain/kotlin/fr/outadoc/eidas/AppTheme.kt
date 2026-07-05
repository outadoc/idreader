package fr.outadoc.eidas

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ExtendedColorScheme(
    val logDebug: Color,
    val logInfo: Color,
    val logWarn: Color,
    val logError: Color,
)

private val darkExtendedColorScheme = ExtendedColorScheme(
    logDebug = Color(0xFF888888),
    logInfo = Color(0xFFEEEEEE),
    logWarn = Color(0xFFFFCC00),
    logError = Color(0xFFFF4444),
)

private val lightExtendedColorScheme = ExtendedColorScheme(
    logDebug = Color(0xFF666666),
    logInfo = Color(0xFF111111),
    logWarn = Color(0xFF7A5000),
    logError = Color(0xFFB00020),
)

val LocalExtendedColorScheme = staticCompositionLocalOf { darkExtendedColorScheme }

val MaterialTheme.extendedColorScheme: ExtendedColorScheme
    @Composable get() = LocalExtendedColorScheme.current

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    CompositionLocalProvider(
        LocalExtendedColorScheme provides if (isDark) {
            darkExtendedColorScheme
        } else {
            lightExtendedColorScheme
        },
    ) {
        MaterialTheme(
            colorScheme = if (isDark) {
                darkColorScheme()
            } else {
                lightColorScheme()
            },
            content = content,
        )
    }
}
