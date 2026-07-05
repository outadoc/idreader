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

private const val TAG = "PaceGetNonceUseCase"

@OptIn(ExperimentalUnsignedTypes::class)
class PaceGetNonceUseCase(
    private val tagReader: NfcTagReader,
    private val commandFactory: CommandFactory,
    private val cryptoEngine: CryptoEngine,
    private val logger: Logger,
) {
    suspend operator fun invoke(
        tag: NfcTag,
        algorithm: Algorithm,
        can: String,
    ): UByteArray {
        logger.i(TAG, "MSE:Set AT")

        tagReader
            .transceive(
                tag,
                commandFactory.paceSetAt(
                    algorithm = algorithm.oid.bytes.toUByteArray(),
                    keyReference = Iso7816.KeyRef.CAN,
                ),
            ).getDataOrThrow()

        logger.i(TAG, "GENERAL AUTHENTICATE (step 1: encrypted nonce)")

        val response =
            tagReader
                .transceive(
                    tag,
                    commandFactory.generalAuthenticate(
                        tlvList {
                            tlv(Iso7816.Tags.DynamicAuthenticationData, ubyteArrayOf())
                        },
                    ),
                ).getDataOrThrow()

        val dynAuth = response.parseDynamicAuthData()

        val encryptedNonce =
            (dynAuth.find(Iso7816.Tags.Nonce.toInt())?.value as? ByteArray)?.toUByteArray()

        checkNotNull(encryptedNonce) {
            "Could not find nonce in dynamic auth data"
        }

        logger.d(TAG, "Encrypted nonce: ${encryptedNonce.toPrettyHex()}")

        val canBytes = can.toByteArray(Charsets.US_ASCII).toUByteArray()

        val kPi =
            cryptoEngine.deriveKeyFromSecret(
                algorithm = algorithm,
                secret = canBytes,
                nonce = ubyteArrayOf(),
                counter = 3,
            )

        val decryptedNonce =
            cryptoEngine.decryptSymmetric(
                algorithm = algorithm,
                key = kPi,
                data = encryptedNonce,
            )

        logger.d(TAG, "Decrypted nonce: ${decryptedNonce.toPrettyHex()}")
        return decryptedNonce
    }
}
