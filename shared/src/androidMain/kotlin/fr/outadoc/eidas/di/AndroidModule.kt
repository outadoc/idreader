package fr.outadoc.eidas.di

import android.app.Activity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import fr.outadoc.eidas.logging.AndroidLogger
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.nfc.AndroidNfcTagReader
import fr.outadoc.eidas.nfc.NfcTagReader
import okio.Path.Companion.toPath
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun androidModule(activity: Activity) =
    module {
        single<Logger>(named("platformLogger")) { AndroidLogger() }
        single<NfcTagReader> { AndroidNfcTagReader(activity, get()) }
        single<DataStore<Preferences>> {
            PreferenceDataStoreFactory.createWithPath(
                produceFile = {
                    activity.applicationContext.filesDir
                        .resolve("datastore/settings.preferences_pb")
                        .absolutePath
                        .toPath()
                },
            )
        }
    }
