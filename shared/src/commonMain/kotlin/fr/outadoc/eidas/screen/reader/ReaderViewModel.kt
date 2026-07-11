package fr.outadoc.eidas.screen.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.eidas.lds.ReadCardDataUseCase
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.e
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.NfcTagReader
import fr.outadoc.eidas.pace.PaceAuthenticateUseCase
import fr.outadoc.eidas.presentation.CardInfoUiModel
import fr.outadoc.eidas.presentation.toCardInfoUiModel
import fr.outadoc.eidas.securemessaging.SecureMessagingSession
import fr.outadoc.eidas.securemessaging.SecureSessionFactory
import fr.outadoc.eidas.settings.SettingsRepository
import fr.outadoc.eidas.settings.model.AppSettings
import fr.outadoc.eidas.settings.model.AuthenticationMethod
import fr.outadoc.eidas.utils.flatMap
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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
        val exception: Throwable? = null,
        val settings: AppSettings = AppSettings(),
    )

    private val _state = MutableStateFlow<State>(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private val _scanResults = Channel<CardInfoUiModel>(Channel.BUFFERED)
    val scanResults: Flow<CardInfoUiModel> = _scanResults.receiveAsFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _state.update { state ->
                    state.copy(
                        settings = settings,
                    )
                }
            }
        }
    }

    fun startListening() {
        viewModelScope.launch {
            try {
                tagReader.detectedTags.collect { nfcSession ->
                    _state.update { state ->
                        state.copy(
                            isReading = true,
                            exception = null,
                        )
                    }

                    val settings: AppSettings = _state.value.settings

                    settingsRepository.saveSettings(settings)

                    paceAuthenticate(
                        nfcSession = nfcSession,
                        authenticationMethod = settings.authenticationMethod,
                        password = settings.password,
                    ).flatMap { credentials ->
                        val ssm: SecureMessagingSession =
                            secureSessionFactory.newInstance(
                                nfcSession = nfcSession,
                                paceCredentials = credentials,
                            )

                        readCardData(
                            nfcSession = ssm,
                        )
                    }.onSuccess { cardDump ->
                        logger.i(TAG, "Got data from card: $cardDump")
                        _state.update { state ->
                            state.copy(
                                isReading = false,
                            )
                        }
                        _scanResults.send(cardDump.toCardInfoUiModel())
                    }.onFailure { e ->
                        logger.e(TAG, "Failed to read data", e)
                        _state.update { state ->
                            state.copy(
                                isReading = false,
                                exception = e,
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                logger.e(TAG, "NFC error", e)
                _state.update { state ->
                    state.copy(
                        isReading = false,
                        exception = e,
                    )
                }
            }
        }
    }

    fun onPasswordChanged(password: String) {
        _state.update { state ->
            state.copy(
                settings =
                    state.settings.copy(
                        password = password,
                    ),
            )
        }
    }

    fun onAuthenticationMethodChanged(method: AuthenticationMethod) {
        _state.update { state ->
            state.copy(
                settings =
                    state.settings.copy(
                        authenticationMethod = method,
                    ),
            )
        }
    }
}
