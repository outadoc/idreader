package fr.outadoc.eidas.securemessaging

import fr.outadoc.eidas.crypto.CryptoEngine
import fr.outadoc.eidas.nfc.CApdu
import fr.outadoc.eidas.nfc.NfcSessionManager
import fr.outadoc.eidas.nfc.NfcTag
import fr.outadoc.eidas.nfc.NfcTagReader
import fr.outadoc.eidas.nfc.RApdu
import fr.outadoc.eidas.pace.PaceSessionKeys

class SecureSessionManager(
    private val nfcTagReader: NfcTagReader,
    private val paceSessionKeys: PaceSessionKeys,
    private val cryptoEngine: CryptoEngine,
) : NfcSessionManager {
    var sendSequenceCounter: UInt = 0u

    override suspend fun transceive(
        tag: NfcTag,
        command: CApdu,
    ): Result<RApdu> {
        TODO("Not yet implemented")
    }
}
