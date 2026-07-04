package fr.outadoc.eidas.di

import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.MemoryLogger
import org.koin.core.qualifier.named
import org.koin.dsl.module

val sharedModule = module {
    single { MemoryLogger(getOrNull(named("platformLogger"))) }
    single<Logger> { get<MemoryLogger>() }
}
