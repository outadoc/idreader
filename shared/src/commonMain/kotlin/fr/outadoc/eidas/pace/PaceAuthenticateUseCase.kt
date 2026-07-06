package fr.outadoc.eidas.pace

import fr.outadoc.eidas.crypto.Algorithm
import fr.outadoc.eidas.crypto.EcPoint
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.NfcTag
import fr.outadoc.eidas.nfc.asn1.SecurityInfo

private const val TAG = "PaceAuthenticateUseCase"

@OptIn(ExperimentalUnsignedTypes::class)
class PaceAuthenticateUseCase(
    private val readCardAccess: ReadCardAccessUseCase,
    private val getNonce: PaceGetNonceUseCase,
    private val mapNonce: PaceMapNonceUseCase,
    private val keyAgreement: PaceKeyAgreementUseCase,
    private val mutualAuth: PaceMutualAuthUseCase,
    private val logger: Logger,
) {
    suspend operator fun invoke(
        tag: NfcTag,
        can: String,
    ): PaceSession {
        val infos: List<SecurityInfo> = readCardAccess(tag)

        val selectedAlgorithm: Algorithm? =
            Algorithm.preferredAlgorithms
                .firstOrNull { algorithm ->
                    // Select the first algorithm in the list of preferred algorithms
                    // that is supported by the chip
                    infos.any { info ->
                        info is SecurityInfo.Pace &&
                                info.protocol == algorithm.oid &&
                                info.parameterId == algorithm.parameterId
                    }
                }

        checkNotNull(selectedAlgorithm) {
            "Chip does not support any of the expected algorithms: ${Algorithm.preferredAlgorithms}"
        }

        logger.i(TAG, "Selected algorithm: $selectedAlgorithm")

        val nonce: UByteArray =
            getNonce(
                tag = tag,
                algorithm = selectedAlgorithm,
                can = can,
            )

        val mappedGenerator: EcPoint =
            mapNonce(
                tag = tag,
                algorithm = selectedAlgorithm,
                nonce = nonce,
            )

        val keys: PaceKeyAgreementResult =
            keyAgreement(
                tag = tag,
                algorithm = selectedAlgorithm,
                mappedGenerator = mappedGenerator,
            )

        mutualAuth(
            tag = tag,
            algorithm = selectedAlgorithm,
            kMac = keys.kMac,
            terminalFinalPub = keys.terminalFinalPub,
            chipFinalPub = keys.chipFinalPub,
        )

        logger.i(TAG, "PACE authentication successful")

        return PaceSession(
            kEnc = keys.kEnc,
            kMac = keys.kMac,
        )
    }
}
