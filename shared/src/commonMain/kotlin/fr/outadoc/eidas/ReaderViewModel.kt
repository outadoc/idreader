package fr.outadoc.eidas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.e
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.NfcTagReader
import fr.outadoc.eidas.pace.PaceAuthenticateUseCase
import fr.outadoc.eidas.pace.PaceSession
import fr.outadoc.eidas.settings.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "ReaderViewModel"

class ReaderViewModel(
    private val logger: Logger,
    private val tagReader: NfcTagReader,
    private val paceAuthenticate: PaceAuthenticateUseCase,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    fun startListening() {
        viewModelScope.launch {
            try {
                tagReader.detectedTags.collect { tag ->
                    logger.i(TAG, "Tag detected: ${tag.description}")

                    paceAuthenticate(
                        tag = tag,
                        can = settingsRepository.settings.first().can,
                    ).onFailure { e ->
                        logger.e(TAG, "Authentication failed", e)
                    }
                }
            } catch (e: Exception) {
                logger.e(TAG, "NFC error", e)
            }
        }
    }
}
