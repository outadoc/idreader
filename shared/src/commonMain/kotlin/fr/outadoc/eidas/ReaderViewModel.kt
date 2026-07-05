package fr.outadoc.eidas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.eidas.crypto.Algorithm
import fr.outadoc.eidas.crypto.CryptoEngine
import fr.outadoc.eidas.crypto.deserializedUncompressedEcPoint
import fr.outadoc.eidas.crypto.serializeUncompressed
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.d
import fr.outadoc.eidas.logging.e
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.nfc.NfcTagReader
import fr.outadoc.eidas.nfc.asn1.SecurityInfo
import fr.outadoc.eidas.nfc.asn1.SecurityInfosParser
import fr.outadoc.eidas.nfc.commands.CommandFactory
import fr.outadoc.eidas.nfc.tlvList
import fr.outadoc.eidas.settings.AppSettings
import fr.outadoc.eidas.settings.SettingsRepository
import fr.outadoc.eidas.utils.toPrettyHex
import io.github.rafaelrabeloit.bertlv.TLVList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okio.ByteString.Companion.encode

private const val TAG = "ReaderViewModel"

@OptIn(ExperimentalUnsignedTypes::class)
class ReaderViewModel(
    private val logger: Logger,
    private val cryptoEngine: CryptoEngine,
    private val tagReader: NfcTagReader,
    private val commandFactory: CommandFactory,
    private val securityInfosParser: SecurityInfosParser,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val algorithm: Algorithm = Algorithm.PACE_AES256_GM_ECDH_BRAINPOOLP256R1

    fun startListening() {
        viewModelScope.launch {
            try {
                tagReader.detectedTags.collect { tag ->
                    val settings: AppSettings = settingsRepository.settings.first()

                    logger.i(TAG, "Tag detected: ${tag.description}")

                    logger.i(TAG, "SELECT FILE EF.CardAccess")

                    tagReader
                        .transceive(tag, commandFactory.selectFile(Iso7816.File.CardAccess.FILE_ID))
                        .getDataOrThrow()

                    logger.i(TAG, "READ BINARY EF.CardAccess")

                    val securityInfos =
                        tagReader
                            .transceive(tag, commandFactory.readBinary())
                            .getDataOrThrow()

                    val infos: List<SecurityInfo> = securityInfosParser.parse(securityInfos)

                    infos.forEach { info ->
                        logger.i(TAG, "$info")
                    }

                    check(
                        infos.any { info ->
                            info is SecurityInfo.Pace &&
                                info.protocol == algorithm.oid &&
                                info.parameterId == algorithm.parameterId
                        },
                    ) {
                        "Chip does not support expected PACE algorithm."
                    }

                    logger.i(TAG, "MSE:Set AT")

                    tagReader
                        .transceive(
                            tag,
                            commandFactory.paceSetAt(
                                algorithm = algorithm.oid.bytes.toUByteArray(),
                                keyReference = Iso7816.KeyRef.CAN,
                            ),
                        ).getDataOrThrow()

                    // Step 1: get encrypted nonce
                    logger.i(TAG, "GENERAL AUTHENTICATE (step 1: encrypted nonce)")

                    val step1Response =
                        tagReader
                            .transceive(
                                tag,
                                commandFactory.generalAuthenticate(
                                    tlvList {
                                        tlv(
                                            Iso7816.Tags.DynamicAuthenticationData,
                                            ubyteArrayOf(),
                                        )
                                    },
                                ),
                            ).getDataOrThrow()

                    val step1DynAuth = step1Response.parseDynamicAuthData()

                    val encryptedNonce: UByteArray? =
                        (step1DynAuth.find(Iso7816.Tags.Nonce.toInt())?.value as? ByteArray)
                            ?.toUByteArray()

                    checkNotNull(encryptedNonce) {
                        "Could not find nonce in dynamic auth data"
                    }

                    logger.d(TAG, "Encrypted nonce: ${encryptedNonce.toPrettyHex()}")

                    val canBytes: UByteArray =
                        settings.can
                            .encode(Charsets.US_ASCII)
                            .toByteArray()
                            .toUByteArray()

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

                    // Step 2: generic mapping — send terminal mapping pub key, receive chip's
                    val mappingKeyPair = cryptoEngine.generateKeyPair(algorithm)

                    logger.i(TAG, "GENERAL AUTHENTICATE (step 2: generic mapping)")

                    val step2Response =
                        tagReader
                            .transceive(
                                tag,
                                commandFactory.generalAuthenticate(
                                    tlvList {
                                        tlv(
                                            Iso7816.Tags.DynamicAuthenticationData,
                                            tlvList {
                                                tlv(
                                                    Iso7816.Tags.MappingData,
                                                    mappingKeyPair.publicKey.uncompressedPublicPoint,
                                                )
                                            },
                                        )
                                    },
                                ),
                            ).getDataOrThrow()

                    val step2DynAuth = step2Response.parseDynamicAuthData()

                    val chipMappingData: UByteArray? =
                        (step2DynAuth.find(Iso7816.Tags.ChipMappingData.toInt())?.value as? ByteArray)
                            ?.toUByteArray()

                    checkNotNull(chipMappingData) {
                        "Could not find mapping data in dynamic auth data"
                    }

                    logger.d(TAG, "Chip mapping point: ${chipMappingData.toPrettyHex()}")

                    // Step 2 (crypto): G' = ECDH(d_map, chip_pub) + s·G
                    val chipMappingPoint = deserializedUncompressedEcPoint(chipMappingData)

                    val mappedGenerator =
                        cryptoEngine.computeMappedGenerator(
                            algorithm = algorithm,
                            mappingPrivateKey = mappingKeyPair.privateKey,
                            chipMappingPublicPoint = chipMappingPoint,
                            decryptedNonce = decryptedNonce,
                        )

                    logger.d(
                        TAG,
                        "Mapped generator G': ${
                            mappedGenerator.serializeUncompressed().toPrettyHex()
                        }",
                    )

                    // Step 3: final key exchange using G' as base point
                    val finalKeyPair =
                        cryptoEngine.generateKeyPairOnGenerator(algorithm, mappedGenerator)

                    logger.i(TAG, "GENERAL AUTHENTICATE (step 3: final key exchange)")

                    val step3Response =
                        tagReader
                            .transceive(
                                tag,
                                commandFactory.generalAuthenticate(
                                    tlvList {
                                        tlv(
                                            Iso7816.Tags.DynamicAuthenticationData,
                                            tlvList {
                                                tlv(
                                                    Iso7816.Tags.EphemeralPublicKey,
                                                    finalKeyPair.publicKey.uncompressedPublicPoint,
                                                )
                                            },
                                        )
                                    },
                                ),
                            ).getDataOrThrow()

                    val step3DynAuth = step3Response.parseDynamicAuthData()

                    val chipFinalPubData: UByteArray? =
                        (
                            step3DynAuth
                                .find(Iso7816.Tags.ChipPublicKey.toInt())
                                ?.value as? ByteArray
                        )?.toUByteArray()

                    checkNotNull(chipFinalPubData) {
                        "Could not find chip final public key in dynamic auth data"
                    }

                    val chipFinalPubPoint = deserializedUncompressedEcPoint(chipFinalPubData)

                    // Shared secret K = x-coordinate of (d_final · chip_pub_final)
                    val sharedSecret =
                        cryptoEngine.computeSharedSecret(
                            algorithm = algorithm,
                            privateKey = finalKeyPair.privateKey,
                            chipPublicPoint = chipFinalPubPoint,
                        )

                    // Step 4: key derivation
                    val kEnc =
                        cryptoEngine.deriveKeyFromSecret(
                            algorithm = algorithm,
                            secret = sharedSecret,
                            nonce = ubyteArrayOf(),
                            counter = 1,
                        )

                    val kMac =
                        cryptoEngine.deriveKeyFromSecret(
                            algorithm = algorithm,
                            secret = sharedSecret,
                            nonce = ubyteArrayOf(),
                            counter = 2,
                        )

                    logger.d(TAG, "K_enc: ${kEnc.toPrettyHex()}")
                    logger.d(TAG, "K_mac: ${kMac.toPrettyHex()}")

                    // Step 5: mutual authentication
                    val terminalToken =
                        cryptoEngine
                            .computeCmac(
                                algorithm = algorithm,
                                key = kMac,
                                data =
                                    paceTokenInput(
                                        oid = algorithm.oid.bytes.toUByteArray(),
                                        pubKey = chipFinalPubData,
                                    ),
                            ).copyOfRange(0, 8)

                    logger.i(TAG, "GENERAL AUTHENTICATE (step 4: mutual authentication)")

                    val step4Response =
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
                            ).getDataOrThrow()

                    val step4DynAuth = step4Response.parseDynamicAuthData()

                    val chipToken: UByteArray =
                        checkNotNull(
                            (step4DynAuth.find(Iso7816.Tags.ChipAuthenticationToken.toInt())?.value as? ByteArray)
                                ?.toUByteArray(),
                        ) {
                            "Could not find chip authentication token"
                        }

                    val expectedChipToken =
                        cryptoEngine
                            .computeCmac(
                                algorithm = algorithm,
                                key = kMac,
                                data =
                                    paceTokenInput(
                                        oid = algorithm.oid.bytes.toUByteArray(),
                                        pubKey = finalKeyPair.publicKey.uncompressedPublicPoint,
                                    ),
                            ).copyOfRange(0, 8)

                    check(chipToken.contentEquals(expectedChipToken)) {
                        "Chip authentication token mismatch: got ${chipToken.toPrettyHex()}, expected ${expectedChipToken.toPrettyHex()}"
                    }

                    logger.i(TAG, "PACE authentication successful")
                }
            } catch (e: Exception) {
                logger.e(TAG, "Error", e)
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

    private fun UByteArray.parseDynamicAuthData(): TLVList {
        val outer =
            TLVList
                .fromTlvListBuffer(this.toByteArray())
                .find(Iso7816.Tags.DynamicAuthenticationData.toInt())
                ?.value as? TLVList
        return checkNotNull(outer) { "Could not find dynamic auth data in response" }
    }
}
