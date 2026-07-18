package fr.outadoc.eidas.nfc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow

class NoopNfcTagReader :
    NfcTagReader,
    NfcSession {
    override fun waitForTag(): Flow<NfcSession> = emptyFlow()

    override val commandCount: StateFlow<Int> = MutableStateFlow(0)

    override suspend fun transceive(command: CApdu): Result<RApdu> = Result.failure(NfcException("Not supported"))
}
