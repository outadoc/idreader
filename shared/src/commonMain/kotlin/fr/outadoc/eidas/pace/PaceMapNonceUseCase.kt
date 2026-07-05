package fr.outadoc.eidas.pace

import fr.outadoc.eidas.crypto.Algorithm
import fr.outadoc.eidas.crypto.CryptoEngine
import fr.outadoc.eidas.crypto.EcPoint
import fr.outadoc.eidas.crypto.deserializedUncompressedEcPoint
import fr.outadoc.eidas.crypto.serializeUncompressed
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.d
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.nfc.NfcTag
import fr.outadoc.eidas.nfc.NfcTagReader
import fr.outadoc.eidas.nfc.commands.CommandFactory
import fr.outadoc.eidas.nfc.tlvList
import fr.outadoc.eidas.utils.toPrettyHex

private const val TAG = "PaceMapNonceUseCase"

@OptIn(ExperimentalUnsignedTypes::class)
class PaceMapNonceUseCase(
    private val tagReader: NfcTagReader,
    private val commandFactory: CommandFactory,
    private val cryptoEngine: CryptoEngine,
    private val logger: Logger,
) {
    suspend operator fun invoke(
        tag: NfcTag,
        algorithm: Algorithm,
        nonce: UByteArray,
    ): EcPoint {
        val mappingKeyPair = cryptoEngine.generateKeyPair(algorithm)

        logger.i(TAG, "GENERAL AUTHENTICATE (step 2: generic mapping)")

        val response =
            tagReader
                .transceive(
                    tag,
                    commandFactory.generalAuthenticate(
                        tlvList {
                            tlv(
                                Iso7816.Tags.DynamicAuthenticationData,
                                tlvList {
                                    tlv(Iso7816.Tags.MappingData, mappingKeyPair.publicKey.uncompressedPublicPoint)
                                },
                            )
                        },
                    ),
                ).getDataOrThrow()

        val dynAuth = response.parseDynamicAuthData()

        val chipMappingData =
            checkNotNull(
                (dynAuth.find(Iso7816.Tags.ChipMappingData.toInt())?.value as? ByteArray)?.toUByteArray(),
            ) { "Could not find mapping data in dynamic auth data" }

        logger.d(TAG, "Chip mapping point: ${chipMappingData.toPrettyHex()}")

        val mappedGenerator =
            cryptoEngine.computeMappedGenerator(
                algorithm = algorithm,
                mappingPrivateKey = mappingKeyPair.privateKey,
                chipMappingPublicPoint = deserializedUncompressedEcPoint(chipMappingData),
                decryptedNonce = nonce,
            )

        logger.d(TAG, "Mapped generator G': ${mappedGenerator.serializeUncompressed().toPrettyHex()}")
        return mappedGenerator
    }
}
