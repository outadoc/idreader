package fr.outadoc.eidas.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.e
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
    private val settingsEncryptor: SettingsEncryptor,
    private val logger: Logger,
) : SettingsRepository {

    private companion object {
        val KEY_CAN = stringPreferencesKey("can")
        const val TAG = "DataStoreSettingsRepository"
    }

    override val settings: Flow<AppSettings> =
        dataStore.data.map { store ->
            AppSettings(
                can = store[KEY_CAN]
                    ?.let { cipherText ->
                        settingsEncryptor.decrypt(cipherText)
                            .onFailure { e ->
                                logger.e(TAG, "Decryption failure", e)
                            }
                            .getOrDefault("")
                    }
                    .orEmpty()
            )
        }

    override suspend fun saveSettings(settings: AppSettings) {
        dataStore.edit { store ->
            store[KEY_CAN] = settingsEncryptor
                .encrypt(settings.can)
                .onFailure { e ->
                    logger.e(TAG, "Encryption failure", e)
                }
                .getOrDefault("")
        }
    }
}
