package fr.outadoc.eidas.di

import fr.outadoc.eidas.ReaderViewModel
import fr.outadoc.eidas.lds.ParseDG11UseCase
import fr.outadoc.eidas.lds.ParseDG13UseCase
import fr.outadoc.eidas.lds.ParseDG1UseCase
import fr.outadoc.eidas.lds.ParseDG2UseCase
import fr.outadoc.eidas.lds.ParseMrzNameUseCase
import fr.outadoc.eidas.lds.ParseMrzUseCase
import fr.outadoc.eidas.lds.ReadCardDataUseCase
import fr.outadoc.eidas.lds.ReadComFileUseCase
import fr.outadoc.eidas.lds.ReadDataGroupUseCase
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.MemoryLogger
import fr.outadoc.eidas.nfc.asn1.SecurityInfosParser
import fr.outadoc.eidas.nfc.commands.CommandFactory
import fr.outadoc.eidas.pace.PaceAuthenticateUseCase
import fr.outadoc.eidas.pace.PaceGetNonceUseCase
import fr.outadoc.eidas.pace.PaceKeyAgreementUseCase
import fr.outadoc.eidas.pace.PaceMapNonceUseCase
import fr.outadoc.eidas.pace.PaceMutualAuthUseCase
import fr.outadoc.eidas.pace.ReadCardAccessUseCase
import fr.outadoc.eidas.securemessaging.SecureSessionFactory
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
        factory { CommandFactory() }

        single<SettingsRepository> { DataStoreSettingsRepository(get(), get(), get()) }
        factory { SecureSessionFactory(get(), get()) }

        factory { ReadComFileUseCase(get(), get()) }
        factory { ReadDataGroupUseCase(get(), get()) }
        factory { ReadCardDataUseCase(get(), get(), get(), get(), get(), get(), get(), get()) }
        factory { ReadCardAccessUseCase(get(), get(), get()) }
        factory { PaceGetNonceUseCase(get(), get(), get()) }
        factory { PaceMapNonceUseCase(get(), get(), get(), get()) }
        factory { PaceKeyAgreementUseCase(get(), get(), get(), get()) }
        factory { PaceMutualAuthUseCase(get(), get(), get()) }
        factory { PaceAuthenticateUseCase(get(), get(), get(), get(), get(), get()) }
        factory { ParseMrzUseCase(get()) }
        factory { ParseMrzNameUseCase() }
        factory { ParseDG1UseCase(get()) }
        factory { ParseDG2UseCase() }
        factory { ParseDG11UseCase(get()) }
        factory { ParseDG13UseCase() }

        viewModel { SettingsViewModel(get()) }
        viewModel { ReaderViewModel(get(), get(), get(), get(), get(), get()) }
    }
