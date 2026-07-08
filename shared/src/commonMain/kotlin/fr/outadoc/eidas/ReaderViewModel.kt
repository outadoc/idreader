package fr.outadoc.eidas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.eidas.lds.LdsDump
import fr.outadoc.eidas.lds.ReadLdsDataUseCase
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.e
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.NfcTagReader
import fr.outadoc.eidas.nfc.commands.CommandFactory
import fr.outadoc.eidas.pace.PaceAuthenticateUseCase
import fr.outadoc.eidas.pace.PaceCredentials
import fr.outadoc.eidas.securemessaging.SecureMessagingSession
import fr.outadoc.eidas.securemessaging.SecureSessionFactory
import fr.outadoc.eidas.settings.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "ReaderViewModel"

@OptIn(ExperimentalUnsignedTypes::class)
class ReaderViewModel(
    private val logger: Logger,
    private val tagReader: NfcTagReader,
    private val paceAuthenticate: PaceAuthenticateUseCase,
    private val settingsRepository: SettingsRepository,
    private val secureSessionFactory: SecureSessionFactory,
    private val commandFactory: CommandFactory,
    private val readLdsData: ReadLdsDataUseCase,
) : ViewModel() {
    fun startListening() {
        viewModelScope.launch {
            try {
                tagReader.detectedTags.collect { nfcSession ->
                    val settings = settingsRepository.settings.first()

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

                    val data: LdsDump =
                        readLdsData(
                            nfcSession = ssm,
                        ).getOrElse { e ->
                            logger.e(TAG, "Failed to read data", e)
                            return@collect
                        }

                    logger.i(TAG, "Done!")
                }
            } catch (e: Exception) {
                logger.e(TAG, "NFC error", e)
            }
        }
    }
}
