package fr.outadoc.eidas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.request.crossfade
import fr.outadoc.eidas.di.activityScopedModule
import fr.outadoc.eidas.di.androidModule
import fr.outadoc.eidas.di.sharedModule
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.media.CoilLogger
import fr.outadoc.eidas.media.DocumentPictureFetcher
import fr.outadoc.eidas.media.DocumentPictureKeyer
import fr.outadoc.eidas.media.Jpeg2000Decoder
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.module.rememberKoinModules
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinConfiguration

@OptIn(KoinExperimentalAPI::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            KoinApplication(
                configuration =
                    koinConfiguration {
                        androidContext(applicationContext)
                        modules(
                            sharedModule,
                            androidModule,
                        )
                    },
                content = {
                    rememberKoinModules(
                        unloadOnAbandoned = true,
                    ) {
                        listOf(
                            activityScopedModule(this),
                        )
                    }

                    val logger: Logger = koinInject()

                    setSingletonImageLoaderFactory { context ->
                        ImageLoader
                            .Builder(context)
                            .crossfade(true)
                            .logger(CoilLogger(logger))
                            .components {
                                add(DocumentPictureKeyer())
                                add(DocumentPictureFetcher.Factory())
                                add(Jpeg2000Decoder.Factory())
                            }.build()
                    }

                    App()
                },
            )
        }
    }
}
