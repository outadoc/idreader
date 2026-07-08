package fr.outadoc.eidas.settings

import fr.outadoc.eidas.settings.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun saveSettings(settings: AppSettings)
}
