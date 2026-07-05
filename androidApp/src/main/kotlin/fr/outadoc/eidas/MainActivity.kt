package fr.outadoc.eidas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import fr.outadoc.eidas.di.activityScopedModule
import fr.outadoc.eidas.di.androidModule
import fr.outadoc.eidas.di.sharedModule
import fr.outadoc.eidas.logging.Logger
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication
import org.koin.compose.module.rememberKoinModules
import org.koin.dsl.koinConfiguration

class MainActivity : ComponentActivity() {
    private val logger: Logger by inject()

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

                    App()
                },
            )
        }
    }
}
