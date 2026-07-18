package fr.outadoc.eidas.screen.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.eidas.lds.ReadCardDataUseCase
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.e
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.NfcTagReader
import fr.outadoc.eidas.pace.PaceAuthenticateUseCase
import fr.outadoc.eidas.presentation.CardInfo
import fr.outadoc.eidas.presentation.MapCardDumpToCardInfoUseCase
import fr.outadoc.eidas.securemessaging.SecureMessagingSession
import fr.outadoc.eidas.securemessaging.SecureSessionFactory
import fr.outadoc.eidas.settings.SettingsRepository
import fr.outadoc.eidas.settings.model.AppSettings
import fr.outadoc.eidas.settings.model.AuthenticationMethod
import fr.outadoc.eidas.utils.flatMap
import kotlinx.coroutines.Job
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
    private val mapCardDumpToCardInfo: MapCardDumpToCardInfoUseCase,
) : ViewModel() {
    sealed interface State {
        val settings: AppSettings

        data class Idle(
            val exception: Throwable? = null,
            override val settings: AppSettings,
        ) : State

        data class Listening(
            override val settings: AppSettings,
        ) : State

        data class Reading(
            override val settings: AppSettings,
            val commandCount: Int = 0,
        ) : State
    }

    sealed interface Event {
        data class ScanResultsAvailable(
            val cardInfo: CardInfo,
        ) : Event
    }

    private val _state =
        MutableStateFlow<State>(
            State.Idle(settings = AppSettings()),
        )
    val state: StateFlow<State> = _state.asStateFlow()

    private val _events = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    private var listeningJob: Job? = null

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _state.update { state ->
                    when (state) {
                        is State.Idle -> state.copy(settings = settings)
                        is State.Listening -> state.copy(settings = settings)
                        is State.Reading -> state.copy(settings = settings)
                    }
                }
            }
        }
    }

    fun onStartListeningClicked() {
        if (_state.value !is State.Idle) {
            return
        }

        listeningJob?.cancel()
        listeningJob =
            viewModelScope.launch {
                // Save current settings
                settingsRepository.saveSettings(_state.value.settings)

                _state.update { state ->
                    State.Listening(
                        settings = state.settings,
                    )
                }

                try {
                    tagReader.detectedTags.collect { nfcSession ->
                        _state.update { state ->
                            State.Reading(
                                settings = state.settings,
                            )
                        }

                        launch {
                            nfcSession.commandCount.collect {
                                _state.update { state ->
                                    if (state is State.Reading) {
                                        state.copy(
                                            commandCount = it,
                                        )
                                    } else {
                                        state
                                    }
                                }
                            }
                        }

                        val settings: AppSettings = _state.value.settings

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
                                State.Idle(
                                    settings = state.settings,
                                )
                            }

                            _events.send(
                                Event.ScanResultsAvailable(
                                    cardInfo = mapCardDumpToCardInfo(cardDump),
                                ),
                            )
                        }.onFailure { e ->
                            logger.e(TAG, "Failed to read data", e)
                            _state.update { state ->
                                State.Idle(
                                    settings = state.settings,
                                    exception = e,
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.e(TAG, "NFC error", e)
                    _state.update { state ->
                        State.Idle(
                            settings = state.settings,
                            exception = e,
                        )
                    }
                }
            }
        listeningJob = null
    }

    fun stopListening() {
        listeningJob?.cancel()
        listeningJob = null
        _state.update { state ->
            State.Idle(
                settings = state.settings,
            )
        }
    }

    fun onPasswordChanged(password: String) {
        _state.update { state ->
            if (state is State.Idle) {
                state.copy(
                    settings =
                        state.settings.copy(
                            password = password,
                        ),
                )
            } else {
                state
            }
        }
    }

    fun onAuthenticationMethodChanged(method: AuthenticationMethod) {
        _state.update { state ->
            if (state is State.Idle) {
                state.copy(
                    settings =
                        state.settings.copy(
                            authenticationMethod = method,
                        ),
                )
            } else {
                state
            }
        }
    }
}
