package fr.outadoc.eidas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.e
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.nfc.NfcTagReader
import fr.outadoc.eidas.nfc.asn1.SecurityInfo
import fr.outadoc.eidas.nfc.asn1.SecurityInfosParser
import fr.outadoc.eidas.nfc.commands.CommandFactory
import kotlinx.coroutines.launch

private const val TAG = "ReaderViewModel"

class ReaderViewModel(
    private val logger: Logger,
    private val tagReader: NfcTagReader,
    private val commandFactory: CommandFactory,
    private val securityInfosParser: SecurityInfosParser,
) : ViewModel() {
    fun startListening() {
        viewModelScope.launch {
            try {
                tagReader.detectedTags.collect { tag ->
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
                                info.protocol == Iso7816.AlgorithmOID.PACE_AES256_GM_ECDH_BRAINPOOLP256R1 &&
                                info.parameterId == Iso7816.ParameterId.BRAINPOOL_P256R1
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
                                    Iso7816.AlgorithmOID.PACE_AES256_GM_ECDH_BRAINPOOLP256R1
                                        .bytes
                                        .toUByteArray(),
                                keyReference = Iso7816.KeyRef.CAN,
                            ),
                        ).getDataOrThrow()

                    logger.i(TAG, "GENERAL AUTHENTICATE")

                    tagReader
                        .transceive(tag, commandFactory.generalAuthenticate())
                        .getDataOrThrow()
                }
            } catch (e: Exception) {
                logger.e(TAG, "Error", e)
            }
        }
    }
}
