package fr.outadoc.eidas.pace

import fr.outadoc.eidas.crypto.Algorithm
import fr.outadoc.eidas.crypto.CryptoEngine
import fr.outadoc.eidas.crypto.EcPoint
import fr.outadoc.eidas.crypto.KeyGenerator
import fr.outadoc.eidas.crypto.KeyPair
import fr.outadoc.eidas.crypto.deserializedUncompressedEcPoint
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.d
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.nfc.NfcSession
import fr.outadoc.eidas.nfc.commands.CommandFactory
import fr.outadoc.eidas.utils.flatMap
import fr.outadoc.eidas.nfc.tlvList
import fr.outadoc.eidas.utils.toPrettyHex
import io.github.rafaelrabeloit.bertlv.TLVList

private const val TAG = "PaceKeyAgreementUseCase"

@OptIn(ExperimentalUnsignedTypes::class)
class PaceKeyAgreementUseCase(
    private val commandFactory: CommandFactory,
    private val cryptoEngine: CryptoEngine,
    private val keyGenerator: KeyGenerator,
    private val logger: Logger,
) {
    suspend operator fun invoke(
        nfcSession: NfcSession,
        algorithm: Algorithm,
        mappedGenerator: EcPoint,
    ): Result<PaceKeyAgreementResult> {
        val finalKeyPair: KeyPair =
            runCatching { keyGenerator.generateKeyPairOnGenerator(algorithm, mappedGenerator) }
                .getOrElse {
                    return Result.failure(it)
                }

        val terminalFinalPub: UByteArray = finalKeyPair.publicKey.uncompressedPublicPoint

        logger.i(TAG, "GENERAL AUTHENTICATE (step 3: final key exchange)")

        val response: UByteArray =
            nfcSession
                .transceive(
                    commandFactory.generalAuthenticate(
                        tlvList {
                            tlv(
                                Iso7816.Tags.DynamicAuthenticationData,
                                tlvList {
                                    tlv(Iso7816.Tags.EphemeralPublicKey, terminalFinalPub)
                                },
                            )
                        },
                    ),
                ).flatMap { it.getData() }
                .getOrElse { return Result.failure(it) }

        logger.d(TAG, "Step 3 raw response: ${response.toPrettyHex()}")

        val dynAuth: TLVList =
            response
                .parseDynamicAuthData()
                .getOrElse { return Result.failure(it) }

        return runCatching {
            val chipFinalPub =
                (dynAuth.find(Iso7816.Tags.ChipPublicKey.toInt())?.value as? ByteArray)
                    ?.toUByteArray()
                    ?: return Result.failure(
                        IllegalStateException("Could not find chip final public key in dynamic auth data"),
                    )

            logger.d(TAG, "Terminal final pub: ${terminalFinalPub.toPrettyHex()}")
            logger.d(TAG, "Chip final pub: ${chipFinalPub.toPrettyHex()}")

            val sharedSecret: UByteArray =
                cryptoEngine.computeSharedSecret(
                    algorithm = algorithm,
                    privateKey = finalKeyPair.privateKey,
                    chipPublicPoint = deserializedUncompressedEcPoint(chipFinalPub),
                )

            logger.d(TAG, "Shared secret K: ${sharedSecret.toPrettyHex()}")

            val kEnc: UByteArray =
                cryptoEngine.deriveKeyFromSecret(
                    algorithm = algorithm,
                    secret = sharedSecret,
                    nonce = ubyteArrayOf(),
                    counter = 1,
                )

            val kMac: UByteArray =
                cryptoEngine.deriveKeyFromSecret(
                    algorithm = algorithm,
                    secret = sharedSecret,
                    nonce = ubyteArrayOf(),
                    counter = 2,
                )

            logger.d(TAG, "K_enc: ${kEnc.toPrettyHex()}")
            logger.d(TAG, "K_mac: ${kMac.toPrettyHex()}")

            PaceKeyAgreementResult(
                kEnc = kEnc,
                kMac = kMac,
                terminalFinalPub = terminalFinalPub,
                chipFinalPub = chipFinalPub,
            )
        }
    }
}
