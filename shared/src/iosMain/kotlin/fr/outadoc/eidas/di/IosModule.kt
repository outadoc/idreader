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
import org.koin.core.qualifier.named
import org.koin.dsl.module

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
                    TODO()
                },
            )
        }
    }
