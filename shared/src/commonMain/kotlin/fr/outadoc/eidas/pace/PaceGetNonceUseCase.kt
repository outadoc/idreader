package fr.outadoc.eidas.pace

import fr.outadoc.eidas.crypto.Algorithm
import fr.outadoc.eidas.crypto.CryptoEngine
import fr.outadoc.eidas.crypto.oidBytes
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.d
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.nfc.NfcSession
import fr.outadoc.eidas.nfc.NfcTag
import fr.outadoc.eidas.nfc.commands.CommandFactory
import fr.outadoc.eidas.nfc.tlvList
import fr.outadoc.eidas.utils.toPrettyHex
import io.github.rafaelrabeloit.bertlv.TLVList

private const val TAG = "PaceGetNonceUseCase"

@OptIn(ExperimentalUnsignedTypes::class)
class PaceGetNonceUseCase(
    private val commandFactory: CommandFactory,
    private val cryptoEngine: CryptoEngine,
    private val logger: Logger,
) {
    suspend operator fun invoke(
        nfcSession: NfcSession,
        algorithm: Algorithm,
        can: String,
    ): Result<UByteArray> {
        logger.i(TAG, "MSE:Set AT")

        nfcSession
            .transceive(
                commandFactory.paceSetAt(
                    algorithm = algorithm.protocol.oidBytes,
                    keyReference = Iso7816.KeyRef.CAN,
                ),
            ).getOrElse { return Result.failure(it) }
            .getData()
            .getOrElse { return Result.failure(it) }

        logger.i(TAG, "GENERAL AUTHENTICATE (step 1: encrypted nonce)")

        val response: UByteArray =
            nfcSession
                .transceive(
                    commandFactory.generalAuthenticate(
                        tlvList {
                            tlv(
                                Iso7816.Tags.DynamicAuthenticationData,
                                ubyteArrayOf(),
                            )
                        },
                    ),
                ).getOrElse { return Result.failure(it) }
                .getData()
                .getOrElse { return Result.failure(it) }

        val dynAuth: TLVList =
            response
                .parseDynamicAuthData()
                .getOrElse { return Result.failure(it) }

        val encryptedNonce: UByteArray =
            (dynAuth.find(Iso7816.Tags.Nonce.toInt())?.value as? ByteArray)?.toUByteArray()
                ?: return Result.failure(
                    IllegalStateException("Could not find nonce in dynamic auth data"),
                )

        logger.d(TAG, "Encrypted nonce: ${encryptedNonce.toPrettyHex()}")

        val canBytes: UByteArray =
            can
                .toByteArray(Charsets.US_ASCII)
                .toUByteArray()

        val kPi: UByteArray =
            runCatching {
                cryptoEngine.deriveKeyFromSecret(
                    algorithm = algorithm,
                    secret = canBytes,
                    nonce = ubyteArrayOf(),
                    counter = 3,
                )
            }.getOrElse {
                return Result.failure(it)
            }

        val decryptedNonce: Result<UByteArray> =
            runCatching {
                cryptoEngine.decryptSymmetric(
                    algorithm = algorithm,
                    key = kPi,
                    data = encryptedNonce,
                )
            }.onSuccess { decryptedNonce ->
                logger.d(TAG, "Decrypted nonce: ${decryptedNonce.toPrettyHex()}")
            }

        return decryptedNonce
    }
}
