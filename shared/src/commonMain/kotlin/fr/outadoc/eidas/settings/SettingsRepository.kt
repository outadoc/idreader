package fr.outadoc.eidas.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun saveSettings(settings: AppSettings)
}
