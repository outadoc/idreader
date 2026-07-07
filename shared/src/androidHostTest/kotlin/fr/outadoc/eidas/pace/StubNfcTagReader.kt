package fr.outadoc.eidas.pace

import fr.outadoc.eidas.nfc.CApdu
import fr.outadoc.eidas.nfc.NfcSession
import fr.outadoc.eidas.nfc.NfcTag
import fr.outadoc.eidas.nfc.NfcTagReader
import fr.outadoc.eidas.nfc.RApdu
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalUnsignedTypes::class)
class StubNfcTagReader(
    vararg responses: ByteArray,
) : NfcTagReader,
    NfcSession {
    private val queue = ArrayDeque(responses.toList())

    override val detectedTags: Flow<NfcTag> = emptyFlow()

    override suspend fun transceive(command: CApdu): Result<RApdu> = Result.success(RApdu.parse(queue.removeFirst().toUByteArray()))
}
