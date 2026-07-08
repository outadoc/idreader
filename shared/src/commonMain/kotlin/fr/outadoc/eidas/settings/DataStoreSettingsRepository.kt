package fr.outadoc.eidas.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.e
import fr.outadoc.eidas.settings.model.AppSettings
import fr.outadoc.eidas.settings.model.AuthenticationMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
    private val settingsEncryptor: SettingsEncryptor,
    private val logger: Logger,
) : SettingsRepository {
    override val settings: Flow<AppSettings> =
        dataStore.data.map { store ->
            AppSettings(
                authenticationMethod =
                    store[KEY_AUTHENTICATION_METHOD]
                        ?.let { runCatching { AuthenticationMethod.valueOf(it) }.getOrNull() }
                        ?: AuthenticationMethod.CAN,
                password =
                    store[KEY_PASSWORD]
                        ?.let { cipherText ->
                            settingsEncryptor
                                .decrypt(cipherText)
                                .onFailure { e -> logger.e(TAG, "Decryption failure", e) }
                                .getOrDefault("")
                        }.orEmpty(),
            )
        }

    override suspend fun saveSettings(settings: AppSettings) {
        dataStore.edit { store ->
            store[KEY_AUTHENTICATION_METHOD] = settings.authenticationMethod.name
            store[KEY_PASSWORD] =
                settingsEncryptor
                    .encrypt(settings.password)
                    .onFailure { e -> logger.e(TAG, "Encryption failure", e) }
                    .getOrDefault("")
        }
    }

    private companion object {
        val KEY_PASSWORD = stringPreferencesKey("password")
        val KEY_AUTHENTICATION_METHOD = stringPreferencesKey("authentication_method")
        const val TAG = "DataStoreSettingsRepository"
    }
}
