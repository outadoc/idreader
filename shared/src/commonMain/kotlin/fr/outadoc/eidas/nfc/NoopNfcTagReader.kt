package fr.outadoc.eidas.nfc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class NoopNfcTagReader :
    NfcTagReader,
    NfcSessionManager {
    override val detectedTags: Flow<NfcTag> = emptyFlow()

    override suspend fun transceive(
        tag: NfcTag,
        command: CApdu,
    ): Result<RApdu> = Result.failure(NfcException("Not supported"))
}
