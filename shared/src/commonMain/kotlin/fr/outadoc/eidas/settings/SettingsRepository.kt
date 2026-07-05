package fr.outadoc.eidas.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val can: Flow<String>
    suspend fun setCan(value: String)
}
