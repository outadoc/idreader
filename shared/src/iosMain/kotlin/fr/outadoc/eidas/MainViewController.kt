package fr.outadoc.eidas

import androidx.compose.ui.window.ComposeUIViewController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.request.crossfade
import fr.outadoc.eidas.di.iosModule
import fr.outadoc.eidas.di.sharedModule
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.media.CoilLogger
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration

fun MainViewController() =
    ComposeUIViewController {
        KoinApplication(
            configuration =
                koinConfiguration {
                    modules(
                        sharedModule,
                        iosModule,
                    )
                },
            content = {
                val logger: Logger = koinInject()

                setSingletonImageLoaderFactory { context ->
                    ImageLoader
                        .Builder(context)
                        .crossfade(true)
                        .logger(CoilLogger(logger))
                        .build()
                }

                App()
            },
        )
    }
