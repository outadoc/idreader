package fr.outadoc.eidas.pace

import fr.outadoc.eidas.crypto.Algorithm
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.NfcTag
import fr.outadoc.eidas.nfc.asn1.SecurityInfo

private const val TAG = "PaceAuthenticateUseCase"

@OptIn(ExperimentalUnsignedTypes::class)
data class PaceSession(
    val kEnc: UByteArray,
    val kMac: UByteArray,
)

class PaceAuthenticateUseCase(
    private val readCardAccess: ReadCardAccessUseCase,
    private val getNonce: PaceGetNonceUseCase,
    private val mapNonce: PaceMapNonceUseCase,
    private val keyAgreement: PaceKeyAgreementUseCase,
    private val mutualAuth: PaceMutualAuthUseCase,
    private val logger: Logger,
) {
    private val algorithm = Algorithm.PACE_AES256_GM_ECDH_BRAINPOOLP256R1

    suspend operator fun invoke(
        tag: NfcTag,
        can: String,
    ): PaceSession {
        val infos = readCardAccess(tag)

        check(
            infos.any { info ->
                info is SecurityInfo.Pace &&
                    info.protocol == algorithm.oid &&
                    info.parameterId == algorithm.parameterId
            },
        ) { "Chip does not support expected PACE algorithm." }

        val nonce =
            getNonce(
                tag = tag,
                algorithm = algorithm,
                can = can,
            )

        val mappedGenerator =
            mapNonce(
                tag = tag,
                algorithm = algorithm,
                nonce = nonce,
            )

        val keys =
            keyAgreement(
                tag = tag,
                algorithm = algorithm,
                mappedGenerator = mappedGenerator,
            )

        mutualAuth(
            tag = tag,
            algorithm = algorithm,
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
