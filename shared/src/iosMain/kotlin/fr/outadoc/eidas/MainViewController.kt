package fr.outadoc.eidas

import androidx.compose.ui.window.ComposeUIViewController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.request.crossfade
import fr.outadoc.eidas.crypto.CryptoEngine
import fr.outadoc.eidas.crypto.KeyGenerator
import fr.outadoc.eidas.di.externalIosModule
import fr.outadoc.eidas.di.iosModule
import fr.outadoc.eidas.di.sharedModule
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.media.CoilLogger
import fr.outadoc.eidas.media.DocumentPictureFetcher
import fr.outadoc.eidas.media.DocumentPictureKeyer
import fr.outadoc.eidas.media.Jpeg2000Factory
import fr.outadoc.eidas.settings.SettingsEncryptor
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration

fun MainViewController(
    cryptoEngine: CryptoEngine,
    keyGenerator: KeyGenerator,
    settingsEncryptor: SettingsEncryptor,
) = ComposeUIViewController {
    KoinApplication(
        configuration =
            koinConfiguration {
                modules(
                    sharedModule,
                    iosModule,
                    externalIosModule(
                        cryptoEngine = cryptoEngine,
                        keyGenerator = keyGenerator,
                        settingsEncryptor = settingsEncryptor,
                    ),
                )
            },
        content = {
            val logger: Logger = koinInject()

            setSingletonImageLoaderFactory { context ->
                ImageLoader
                    .Builder(context)
                    .crossfade(true)
                    .logger(CoilLogger(logger))
                    .components {
                        add(DocumentPictureKeyer())
                        add(DocumentPictureFetcher.Factory())
                        add(Jpeg2000Factory())
                    }.build()
            }

            App()
        },
    )
}
