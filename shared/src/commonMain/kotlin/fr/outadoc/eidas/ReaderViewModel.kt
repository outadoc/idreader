package fr.outadoc.eidas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.eidas.lds.ReadCardDataUseCase
import fr.outadoc.eidas.lds.model.CardDump
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.e
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.NfcTagReader
import fr.outadoc.eidas.pace.PaceAuthenticateUseCase
import fr.outadoc.eidas.pace.model.PaceCredentials
import fr.outadoc.eidas.securemessaging.SecureMessagingSession
import fr.outadoc.eidas.securemessaging.SecureSessionFactory
import fr.outadoc.eidas.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "ReaderViewModel"

@OptIn(ExperimentalUnsignedTypes::class)
class ReaderViewModel(
    private val logger: Logger,
    private val tagReader: NfcTagReader,
    private val paceAuthenticate: PaceAuthenticateUseCase,
    private val settingsRepository: SettingsRepository,
    private val secureSessionFactory: SecureSessionFactory,
    private val readCardData: ReadCardDataUseCase,
) : ViewModel() {
    data class State(
        val isReading: Boolean = false,
        val cardDump: CardDump? = null,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun startListening() {
        viewModelScope.launch {
            try {
                tagReader.detectedTags.collect { nfcSession ->
                    val settings = settingsRepository.settings.first()

                    _state.update { state ->
                        state.copy(
                            isReading = true,
                        )
                    }

                    val credentials: PaceCredentials =
                        paceAuthenticate(
                            nfcSession = nfcSession,
                            authenticationMethod = settings.authenticationMethod,
                            password = settings.password,
                        ).getOrElse { e ->
                            logger.e(TAG, "Authentication failed", e)
                            return@collect
                        }

                    val ssm: SecureMessagingSession =
                        secureSessionFactory.newInstance(
                            nfcSession = nfcSession,
                            paceCredentials = credentials,
                        )

                    readCardData(
                        nfcSession = ssm,
                    ).onSuccess { cardDump ->
                        logger.i(TAG, "Got data from card: $cardDump")
                        _state.update { state ->
                            state.copy(
                                isReading = false,
                                cardDump = cardDump,
                            )
                        }
                    }.onFailure { e ->
                        logger.e(TAG, "Failed to read data", e)
                    }

                    logger.i(TAG, "Done reading")
                }
            } catch (e: Exception) {
                logger.e(TAG, "NFC error", e)
            } finally {
                _state.update { state ->
                    state.copy(
                        isReading = false,
                    )
                }
            }
        }
    }
}
