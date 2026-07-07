package fr.outadoc.eidas.nfc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class NoopNfcTagReader :
    NfcTagReader,
    NfcSession {
    override val detectedTags: Flow<NfcSession> = emptyFlow()

    override suspend fun transceive(command: CApdu): Result<RApdu> = Result.failure(NfcException("Not supported"))
}
