package fr.outadoc.eidas.pace

import fr.outadoc.eidas.crypto.Algorithm
import fr.outadoc.eidas.crypto.CryptoEngine
import fr.outadoc.eidas.crypto.oidBytes
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.d
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.nfc.NfcTag
import fr.outadoc.eidas.nfc.NfcTagReader
import fr.outadoc.eidas.nfc.commands.CommandFactory
import fr.outadoc.eidas.nfc.tlvList
import fr.outadoc.eidas.utils.toPrettyHex
import io.github.rafaelrabeloit.bertlv.TLVList

private const val TAG = "PaceMutualAuthUseCase"

@OptIn(ExperimentalUnsignedTypes::class)
class PaceMutualAuthUseCase(
    private val tagReader: NfcTagReader,
    private val commandFactory: CommandFactory,
    private val cryptoEngine: CryptoEngine,
    private val logger: Logger,
) {
    suspend operator fun invoke(
        tag: NfcTag,
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

        val response =
            tagReader
                .transceive(
                    tag,
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
                ).getOrElse { return Result.failure(it) }
                .getData()
                .getOrElse { return Result.failure(it) }

        val dynAuth: TLVList =
            response
                .parseDynamicAuthData()
                .getOrElse { return Result.failure(it) }

        return runCatching {
            val chipToken =
                (dynAuth.find(Iso7816.Tags.ChipAuthenticationToken.toInt())?.value as? ByteArray)
                    ?.toUByteArray()
                    ?: throw IllegalStateException("Could not find chip authentication token")

            val expectedChipToken =
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

            check(chipToken.contentEquals(expectedChipToken)) {
                "Chip authentication token mismatch: got ${chipToken.toPrettyHex()}, expected ${expectedChipToken.toPrettyHex()}"
            }
        }
    }

    // Builds the auth token input: 7F49 { 06 <oid>, 86 <pubKey> }
    private fun paceTokenInput(
        oid: UByteArray,
        pubKey: UByteArray,
    ): UByteArray {
        val oidTlv = ubyteArrayOf(0x06u, oid.size.toUByte(), *oid)
        val pubKeyTlv = ubyteArrayOf(0x86u, pubKey.size.toUByte(), *pubKey)
        val inner = oidTlv + pubKeyTlv
        return ubyteArrayOf(0x7Fu, 0x49u, inner.size.toUByte(), *inner)
    }
}
