package fr.outadoc.eidas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import fr.outadoc.eidas.logging.AndroidLogger
import fr.outadoc.eidas.nfc.AndroidNfcTagReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val logger = AndroidLogger()
        val tagReader = AndroidNfcTagReader(this, logger)

        setContent {
            App(tagReader, logger)
        }
    }
}
