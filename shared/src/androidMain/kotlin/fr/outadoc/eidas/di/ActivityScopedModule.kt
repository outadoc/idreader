package fr.outadoc.eidas.di

import android.app.Activity
import fr.outadoc.eidas.nfc.AndroidNfcTagReader
import fr.outadoc.eidas.nfc.NfcTagReader
import org.koin.dsl.module

fun activityScopedModule(activity: Activity) =
    module {
        factory<NfcTagReader> { AndroidNfcTagReader(activity, get()) }
    }
