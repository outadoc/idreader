package fr.outadoc.eidas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import fr.outadoc.eidas.di.androidModule
import fr.outadoc.eidas.di.sharedModule
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            KoinApplication(
                configuration = koinConfiguration {
                    modules(
                        sharedModule,
                        androidModule(this@MainActivity)
                    )
                },
                content = {
                    App()
                }
            )
        }
    }
}
