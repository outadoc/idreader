package fr.outadoc.eidas.securemessaging

import fr.outadoc.eidas.crypto.CryptoEngine
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.nfc.NfcSession
import fr.outadoc.eidas.pace.PaceCredentials

@OptIn(ExperimentalUnsignedTypes::class)
class SecureSessionFactory(
    private val cryptoEngine: CryptoEngine,
    private val logger: Logger,
) {
    fun newInstance(
        nfcSession: NfcSession,
        paceCredentials: PaceCredentials,
    ): SecureMessagingSession =
        SecureMessagingSession(
            paceCredentials = paceCredentials,
            nfcSession = nfcSession,
            cryptoEngine = cryptoEngine,
            logger = logger,
        )
}
