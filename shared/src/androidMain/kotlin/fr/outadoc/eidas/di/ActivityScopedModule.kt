package fr.outadoc.eidas.di

import android.app.Activity
import fr.outadoc.eidas.nfc.AndroidNfcTagReader
import fr.outadoc.eidas.nfc.NfcTagReader
import org.koin.dsl.module

fun activityScopedModule(activity: Activity) =
    module {
        single<NfcTagReader> { AndroidNfcTagReader(activity, get()) }
    }
