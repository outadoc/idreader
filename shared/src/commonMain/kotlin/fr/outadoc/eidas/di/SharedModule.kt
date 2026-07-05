package fr.outadoc.eidas.di

import fr.outadoc.eidas.ReaderViewModel
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.MemoryLogger
import fr.outadoc.eidas.nfc.asn1.SecurityInfosParser
import fr.outadoc.eidas.nfc.commands.GeneralAuthenticateCommand
import fr.outadoc.eidas.nfc.commands.MseSetAtCommand
import fr.outadoc.eidas.nfc.commands.ReadBinaryCommand
import fr.outadoc.eidas.nfc.commands.SelectCommand
import fr.outadoc.eidas.nfc.commands.SelectFileCommand
import fr.outadoc.eidas.settings.DataStoreSettingsRepository
import fr.outadoc.eidas.settings.SettingsRepository
import fr.outadoc.eidas.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val sharedModule =
    module {
        single { MemoryLogger(getOrNull(named("platformLogger"))) }
        single<Logger> { get<MemoryLogger>() }

        factory { SecurityInfosParser() }

        factory { SelectCommand() }
        factory { MseSetAtCommand() }
        factory { ReadBinaryCommand() }
        factory { SelectFileCommand() }
        factory { GeneralAuthenticateCommand() }

        single<SettingsRepository> { DataStoreSettingsRepository(get()) }

        viewModel {
            ReaderViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel { SettingsViewModel(get()) }
    }
