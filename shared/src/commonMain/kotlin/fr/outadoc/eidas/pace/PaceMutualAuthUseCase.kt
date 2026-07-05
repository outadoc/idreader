package fr.outadoc.eidas.pace

import fr.outadoc.eidas.crypto.Algorithm
import fr.outadoc.eidas.crypto.CryptoEngine
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.d
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.nfc.NfcTag
import fr.outadoc.eidas.nfc.NfcTagReader
import fr.outadoc.eidas.nfc.commands.CommandFactory
import fr.outadoc.eidas.nfc.tlvList
import fr.outadoc.eidas.utils.toPrettyHex

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
    ) {
        val tokenInput = paceTokenInput(algorithm.oid.bytes.toUByteArray(), chipFinalPub)
        logger.d(TAG, "Terminal token CMAC input: ${tokenInput.toPrettyHex()}")

        val terminalToken = cryptoEngine.computeCmac(algorithm, kMac, tokenInput).copyOfRange(0, 8)
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
                                    tlvList { tlv(Iso7816.Tags.AuthenticationToken, terminalToken) },
                                )
                            },
                        chained = false,
                    ),
                ).getDataOrThrow()

        val dynAuth = response.parseDynamicAuthData()

        val chipToken =
            checkNotNull(
                (dynAuth.find(Iso7816.Tags.ChipAuthenticationToken.toInt())?.value as? ByteArray)?.toUByteArray(),
            ) { "Could not find chip authentication token" }

        val expectedChipToken =
            cryptoEngine
                .computeCmac(algorithm, kMac, paceTokenInput(algorithm.oid.bytes.toUByteArray(), terminalFinalPub))
                .copyOfRange(0, 8)

        check(chipToken.contentEquals(expectedChipToken)) {
            "Chip authentication token mismatch: got ${chipToken.toPrettyHex()}, expected ${expectedChipToken.toPrettyHex()}"
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
