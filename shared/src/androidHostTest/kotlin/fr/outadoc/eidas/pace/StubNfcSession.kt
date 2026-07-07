package fr.outadoc.eidas.pace

import fr.outadoc.eidas.nfc.CApdu
import fr.outadoc.eidas.nfc.NfcSession
import fr.outadoc.eidas.nfc.RApdu

@OptIn(ExperimentalUnsignedTypes::class)
class StubNfcSession(
    vararg responses: ByteArray,
) : NfcSession {
    private val queue = ArrayDeque(responses.toList())

    override suspend fun transceive(command: CApdu): Result<RApdu> = Result.success(RApdu.parse(queue.removeFirst().toUByteArray()))
}
