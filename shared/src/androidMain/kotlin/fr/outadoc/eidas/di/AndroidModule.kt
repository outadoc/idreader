package fr.outadoc.eidas.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import fr.outadoc.eidas.crypto.AndroidCryptoEngine
import fr.outadoc.eidas.crypto.AndroidKeyGenerator
import fr.outadoc.eidas.crypto.CryptoEngine
import fr.outadoc.eidas.crypto.KeyGenerator
import fr.outadoc.eidas.logging.AndroidLogger
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.settings.AndroidSettingsEncryptor
import fr.outadoc.eidas.settings.SettingsEncryptor
import okio.Path.Companion.toPath
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.koin.core.qualifier.named
import org.koin.dsl.module

val androidModule =
    module {
        single { BouncyCastleProvider() }
        single<Logger>(named("platformLogger")) { AndroidLogger() }
        single<CryptoEngine> { AndroidCryptoEngine() }
        single<KeyGenerator> { AndroidKeyGenerator(get()) }
        factory<SettingsEncryptor> { AndroidSettingsEncryptor() }
        single<DataStore<Preferences>> {
            PreferenceDataStoreFactory.createWithPath(
                produceFile = {
                    get<Context>()
                        .filesDir
                        .resolve("datastore/settings.preferences_pb")
                        .absolutePath
                        .toPath()
                },
            )
        }
    }
