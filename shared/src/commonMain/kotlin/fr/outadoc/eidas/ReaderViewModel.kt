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
import fr.outadoc.eidas.nfc.commands.GeneralAuthenticateCommand
import fr.outadoc.eidas.nfc.commands.MseSetAtCommand
import fr.outadoc.eidas.nfc.commands.ReadBinaryCommand
import fr.outadoc.eidas.nfc.commands.SelectCommand
import fr.outadoc.eidas.nfc.commands.SelectFileCommand
import kotlinx.coroutines.launch

private const val TAG = "ReaderViewModel"

class ReaderViewModel(
    private val logger: Logger,
    private val tagReader: NfcTagReader,
    private val selectCommand: SelectCommand,
    private val generalAuthenticateCommand: GeneralAuthenticateCommand,
    private val mseSetAtCommand: MseSetAtCommand,
    private val selectFileCommand: SelectFileCommand,
    private val readBinaryCommand: ReadBinaryCommand,
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
                            selectFileCommand.selectFile(
                                Iso7816.File.CardAccess.FILE_ID,
                            ),
                        ).assertSuccess()

                    logger.i(TAG, "READ BINARY EF.CardAccess")

                    val securityInfos =
                        tagReader.transceive(tag, readBinaryCommand.readBinary())

                    securityInfos.assertSuccess()

                    val infos: List<SecurityInfo> =
                        securityInfosParser.parse(
                            securityInfos.data.toByteArray(),
                        )

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
                        .transceive(tag, mseSetAtCommand.paceSetAt())
                        .assertSuccess()

                    logger.i(TAG, "GENERAL AUTHENTICATE")

                    tagReader
                        .transceive(tag, generalAuthenticateCommand.generalAuthenticate())
                        .assertSuccess()
                }
            } catch (e: Exception) {
                logger.e(TAG, "Error", e)
            }
        }
    }
}
