package fr.outadoc.eidas.securemessaging

import fr.outadoc.eidas.crypto.CryptoEngine
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.nfc.NfcSessionManager
import fr.outadoc.eidas.pace.PaceSession

@OptIn(ExperimentalUnsignedTypes::class)
class SecureSessionFactory(
    private val nfcSessionManager: NfcSessionManager,
    private val cryptoEngine: CryptoEngine,
    private val logger: Logger,
) {
    fun newInstance(paceSession: PaceSession): SecureSessionManager =
        SecureSessionManager(
            paceSession = paceSession,
            nfcSessionManager = nfcSessionManager,
            cryptoEngine = cryptoEngine,
            logger = logger,
        )
}
