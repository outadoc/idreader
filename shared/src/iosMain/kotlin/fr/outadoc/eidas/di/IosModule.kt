package fr.outadoc.eidas.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import fr.outadoc.eidas.crypto.CryptoEngine
import fr.outadoc.eidas.crypto.IosCryptoEngine
import fr.outadoc.eidas.crypto.IosKeyGenerator
import fr.outadoc.eidas.crypto.KeyGenerator
import fr.outadoc.eidas.logging.IosLogger
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.nfc.IosNfcTagReader
import fr.outadoc.eidas.nfc.NfcTagReader
import fr.outadoc.eidas.settings.IosSettingsEncryptor
import fr.outadoc.eidas.settings.SettingsEncryptor
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path
import okio.Path.Companion.toPath
import org.koin.core.qualifier.named
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

val iosModule =
    module {
        single<Logger>(named("platformLogger")) { IosLogger() }
        single<NfcTagReader> { IosNfcTagReader(get()) }

        single<CryptoEngine> { IosCryptoEngine() }
        single<KeyGenerator> { IosKeyGenerator() }
        factory<SettingsEncryptor> { IosSettingsEncryptor() }
        single<DataStore<Preferences>> {
            PreferenceDataStoreFactory.createWithPath(
                produceFile = {
                    getDocumentsDirectory().resolve("fr.outadoc.eidas.preferences_pb")
                },
            )
        }
    }

@OptIn(ExperimentalForeignApi::class)
private fun getDocumentsDirectory(): Path =
    NSFileManager.defaultManager
        .URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )?.path
        ?.toPath()
        ?: error("Could not get document directory")
