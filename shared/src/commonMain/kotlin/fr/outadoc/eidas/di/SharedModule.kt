package fr.outadoc.eidas.di

import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.MemoryLogger
import fr.outadoc.eidas.nfc.commands.MseSetAtUseCase
import fr.outadoc.eidas.nfc.commands.SelectUseCase
import fr.outadoc.eidas.settings.SettingsViewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

val sharedModule =
    module {
        single { MemoryLogger(getOrNull(named("platformLogger"))) }
        single<Logger> { get<MemoryLogger>() }
        factory { SelectUseCase() }
        factory { MseSetAtUseCase() }
        viewModel { SettingsViewModel(get()) }
    }
