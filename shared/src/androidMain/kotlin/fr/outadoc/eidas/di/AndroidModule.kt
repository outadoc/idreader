package fr.outadoc.eidas.di

import android.app.Activity
import fr.outadoc.eidas.logging.AndroidLogger
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.nfc.AndroidNfcTagReader
import fr.outadoc.eidas.nfc.NfcTagReader
import org.koin.dsl.module

fun androidModule(activity: Activity) = module {
    single<Logger> { AndroidLogger() }
    single<NfcTagReader> { AndroidNfcTagReader(get(), get()) }
}
