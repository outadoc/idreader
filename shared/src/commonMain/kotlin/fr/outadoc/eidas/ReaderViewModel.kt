package fr.outadoc.eidas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.eidas.crypto.Algorithm
import fr.outadoc.eidas.crypto.CryptoEngine
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.e
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.nfc.NfcTagReader
import fr.outadoc.eidas.nfc.asn1.SecurityInfo
import fr.outadoc.eidas.nfc.asn1.SecurityInfosParser
import fr.outadoc.eidas.nfc.commands.CommandFactory
import fr.outadoc.eidas.settings.AppSettings
import fr.outadoc.eidas.settings.SettingsRepository
import fr.outadoc.eidas.utils.toPrettyHex
import io.github.rafaelrabeloit.bertlv.TLVList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okio.ByteString.Companion.encode

private const val TAG = "ReaderViewModel"

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
                        .transceive(
                            tag,
                            commandFactory.selectFile(
                                Iso7816.File.CardAccess.FILE_ID,
                            ),
                        ).getDataOrThrow()

                    logger.i(TAG, "READ BINARY EF.CardAccess")

                    val securityInfos =
                        tagReader
                            .transceive(tag, commandFactory.readBinary())
                            .getDataOrThrow()

                    val infos: List<SecurityInfo> =
                        securityInfosParser.parse(securityInfos)

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
                                algorithm =
                                    algorithm.oid
                                        .bytes
                                        .toUByteArray(),
                                keyReference = Iso7816.KeyRef.CAN,
                            ),
                        ).getDataOrThrow()

                    logger.i(TAG, "GENERAL AUTHENTICATE")

                    val generalAuthResponse =
                        tagReader
                            .transceive(tag, commandFactory.generalAuthenticate())
                            .getDataOrThrow()

                    val dynamicAuthData: TLVList? =
                        TLVList
                            .fromTlvListBuffer(generalAuthResponse.toByteArray())
                            .find(Iso7816.Tags.DynamicAuthenticationData.toInt())
                            ?.value as? TLVList

                    checkNotNull(dynamicAuthData) {
                        "Could not find dynamic auth data in reponse"
                    }

                    val encryptedNonce: UByteArray? =
                        (
                            dynamicAuthData
                                .find(Iso7816.Tags.Nonce.toInt())
                                ?.value
                                as? ByteArray
                        )?.toUByteArray()

                    checkNotNull(encryptedNonce) {
                        "Could not find nonce in dynamic auth data"
                    }

                    logger.i(TAG, "Encrypted nonce: ${encryptedNonce.toPrettyHex()}")

                    // Read the CAN from preferences and encode it to bytes
                    val canBytes: UByteArray =
                        settings.can
                            .encode(Charsets.US_ASCII)
                            .toByteArray()
                            .toUByteArray()

                    // Derive the key to decrypt the nonce from the CAN
                    val kPi: UByteArray =
                        cryptoEngine.kdf(
                            algorithm = algorithm,
                            secret = canBytes,
                            nonce = ubyteArrayOf(),
                            counter = 3,
                        )

                    // Decrypt the nonce with the key derived from the CAN
                    val decryptedNonce: UByteArray =
                        cryptoEngine.decryptSymmetric(
                            algorithm = algorithm,
                            key = kPi,
                            data = encryptedNonce,
                        )

                    logger.i(TAG, "Decrypted nonce: ${decryptedNonce.toPrettyHex()}")
                }
            } catch (e: Exception) {
                logger.e(TAG, "Error", e)
            }
        }
    }
}
