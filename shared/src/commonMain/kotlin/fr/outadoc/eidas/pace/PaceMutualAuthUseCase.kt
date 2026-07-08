package fr.outadoc.eidas.pace

import fr.outadoc.eidas.crypto.Algorithm
import fr.outadoc.eidas.crypto.CryptoEngine
import fr.outadoc.eidas.crypto.oidBytes
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.d
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.nfc.NfcSession
import fr.outadoc.eidas.nfc.commands.CommandFactory
import fr.outadoc.eidas.nfc.tlvList
import fr.outadoc.eidas.tlv.TlvNode
import fr.outadoc.eidas.tlv.buildTlv
import fr.outadoc.eidas.tlv.firstWithTag
import fr.outadoc.eidas.utils.flatMap
import fr.outadoc.eidas.utils.toPrettyHex

private const val TAG = "PaceMutualAuthUseCase"

@OptIn(ExperimentalUnsignedTypes::class)
class PaceMutualAuthUseCase(
    private val commandFactory: CommandFactory,
    private val cryptoEngine: CryptoEngine,
    private val logger: Logger,
) {
    suspend operator fun invoke(
        nfcSession: NfcSession,
        algorithm: Algorithm,
        kMac: UByteArray,
        terminalFinalPub: UByteArray,
        chipFinalPub: UByteArray,
    ): Result<Unit> {
        val tokenInput =
            paceTokenInput(
                oid = algorithm.protocol.oidBytes,
                pubKey = chipFinalPub,
            )

        logger.d(TAG, "Terminal token CMAC input: ${tokenInput.toPrettyHex()}")

        val terminalToken =
            runCatching {
                cryptoEngine
                    .computeCmac(
                        algorithm = algorithm,
                        key = kMac,
                        data = tokenInput,
                    ).copyOfRange(0, 8)
            }.getOrElse {
                return Result.failure(it)
            }

        logger.d(TAG, "Terminal token (8 bytes): ${terminalToken.toPrettyHex()}")

        logger.i(TAG, "GENERAL AUTHENTICATE (step 4: mutual authentication)")

        val dynAuth: List<TlvNode> =
            nfcSession
                .transceive(
                    commandFactory.generalAuthenticate(
                        data =
                            tlvList {
                                tlv(
                                    Iso7816.Tags.DynamicAuthenticationData,
                                    tlvList {
                                        tlv(
                                            Iso7816.Tags.AuthenticationToken,
                                            terminalToken,
                                        )
                                    },
                                )
                            },
                        chained = false,
                    ),
                ).flatMap { it.getData() }
                .flatMap { it.parseDynamicAuthData() }
                .getOrElse { return Result.failure(it) }

        val chipToken: UByteArray =
            dynAuth.firstWithTag(Iso7816.Tags.ChipAuthenticationToken)?.value
                ?: return Result.failure(
                    IllegalStateException("Could not find chip authentication token"),
                )

        val expectedChipToken: UByteArray =
            runCatching {
                cryptoEngine
                    .computeCmac(
                        algorithm = algorithm,
                        key = kMac,
                        data =
                            paceTokenInput(
                                oid = algorithm.protocol.oidBytes,
                                pubKey = terminalFinalPub,
                            ),
                    ).copyOfRange(0, 8)
            }.getOrElse {
                return Result.failure(it)
            }

        if (!chipToken.contentEquals(expectedChipToken)) {
            return Result.failure(
                IllegalStateException(
                    "Chip authentication token mismatch: got ${chipToken.toPrettyHex()}, expected ${expectedChipToken.toPrettyHex()}",
                ),
            )
        }

        return Result.success(Unit)
    }

    private fun paceTokenInput(
        oid: UByteArray,
        pubKey: UByteArray,
    ): UByteArray =
        buildTlv {
            constructed(0x7F49u) {
                tlv(0x06u, oid)
                tlv(0x86u, pubKey)
            }
        }
}
