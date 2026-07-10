package fr.outadoc.eidas.pace

import fr.outadoc.eidas.crypto.Algorithm
import fr.outadoc.eidas.crypto.CryptoEngine
import fr.outadoc.eidas.crypto.EcPoint
import fr.outadoc.eidas.crypto.KeyGenerator
import fr.outadoc.eidas.crypto.KeyPair
import fr.outadoc.eidas.crypto.deserializedUncompressedEcPoint
import fr.outadoc.eidas.crypto.serializeUncompressed
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.d
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.Icao9303
import fr.outadoc.eidas.nfc.NfcSession
import fr.outadoc.eidas.nfc.commands.CommandFactory
import fr.outadoc.eidas.nfc.tlvList
import fr.outadoc.eidas.tlv.firstWithTag
import fr.outadoc.eidas.utils.flatMap
import fr.outadoc.eidas.utils.toPrettyHex

private const val TAG = "PaceMapNonceUseCase"

@OptIn(ExperimentalUnsignedTypes::class)
class PaceMapNonceUseCase(
    private val commandFactory: CommandFactory,
    private val cryptoEngine: CryptoEngine,
    private val keyGenerator: KeyGenerator,
    private val logger: Logger,
) {
    suspend operator fun invoke(
        nfcSession: NfcSession,
        algorithm: Algorithm,
        nonce: UByteArray,
    ): Result<EcPoint> {
        val mappingKeyPair: KeyPair =
            runCatching { keyGenerator.generateKeyPair(algorithm) }
                .getOrElse { return Result.failure(it) }

        logger.i(TAG, "GENERAL AUTHENTICATE (step 2: generic mapping)")

        return nfcSession
            .transceive(
                commandFactory.generalAuthenticate(
                    tlvList {
                        tlv(
                            Icao9303.Tags.DynamicAuthenticationData,
                            tlvList {
                                tlv(
                                    Icao9303.Tags.MappingData,
                                    mappingKeyPair.publicKey.uncompressedPublicPoint,
                                )
                            },
                        )
                    },
                ),
            ).flatMap { rApdu -> rApdu.getData() }
            .flatMap { data -> data.parseDynamicAuthData() }
            .flatMap { dynAuth -> dynAuth.firstWithTag(Icao9303.Tags.ChipMappingData) }
            .mapCatching { chipMappingNode ->
                val chipMappingData: UByteArray = chipMappingNode.value

                logger.d(TAG, "Chip mapping point: ${chipMappingData.toPrettyHex()}")

                cryptoEngine.computeMappedGenerator(
                    algorithm = algorithm,
                    mappingPrivateKey = mappingKeyPair.privateKey,
                    chipMappingPublicPoint = deserializedUncompressedEcPoint(chipMappingData),
                    decryptedNonce = nonce,
                )
            }.onSuccess { mappedGenerator ->
                logger.d(
                    TAG,
                    "Mapped generator G': ${mappedGenerator.serializeUncompressed().toPrettyHex()}",
                )
            }
    }
}
