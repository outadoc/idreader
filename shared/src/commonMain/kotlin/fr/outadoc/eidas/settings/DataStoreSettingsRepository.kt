package fr.outadoc.eidas.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {
    private val KEY_CAN = stringPreferencesKey("can")

    override val settings: Flow<AppSettings> =
        dataStore.data.map { AppSettings(can = it[KEY_CAN] ?: "") }

    override suspend fun saveSettings(settings: AppSettings) {
        dataStore.edit { it[KEY_CAN] = settings.can }
    }
}
