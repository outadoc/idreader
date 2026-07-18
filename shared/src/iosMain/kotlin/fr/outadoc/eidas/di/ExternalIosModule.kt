package fr.outadoc.eidas.di

import fr.outadoc.eidas.crypto.CryptoEngine
import fr.outadoc.eidas.crypto.KeyGenerator
import fr.outadoc.eidas.settings.SettingsEncryptor
import org.koin.dsl.module

fun externalIosModule(
    cryptoEngine: CryptoEngine,
    keyGenerator: KeyGenerator,
    settingsEncryptor: SettingsEncryptor,
) = module {
    factory<CryptoEngine> { cryptoEngine }
    factory<KeyGenerator> { keyGenerator }
    factory<SettingsEncryptor> { settingsEncryptor }
}
