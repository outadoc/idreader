package fr.outadoc.eidas.di

import fr.outadoc.eidas.logging.IosLogger
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.nfc.IosNfcTagReader
import fr.outadoc.eidas.nfc.NfcTagReader
import org.koin.core.qualifier.named
import org.koin.dsl.module

val iosModule =
    module {
        single<Logger>(named("platformLogger")) { IosLogger() }
        single<NfcTagReader> { IosNfcTagReader(get()) }
    }
