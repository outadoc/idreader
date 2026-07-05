package fr.outadoc.eidas.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import fr.outadoc.eidas.logging.AndroidLogger
import fr.outadoc.eidas.logging.Logger
import okio.Path.Companion.toPath
import org.koin.core.qualifier.named
import org.koin.dsl.module

val androidModule =
    module {
        single<Logger>(named("platformLogger")) { AndroidLogger() }
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
