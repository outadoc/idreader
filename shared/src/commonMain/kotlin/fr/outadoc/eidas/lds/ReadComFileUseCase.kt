package fr.outadoc.eidas.lds

import fr.outadoc.eidas.lds.model.ComData
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.Icao9303
import fr.outadoc.eidas.nfc.NfcSession
import fr.outadoc.eidas.nfc.commands.CommandFactory
import fr.outadoc.eidas.tlv.firstWithTag
import fr.outadoc.eidas.tlv.parseTlv
import fr.outadoc.eidas.utils.flatMap

private const val TAG = "ReadComUseCase"

@OptIn(ExperimentalUnsignedTypes::class)
class ReadComFileUseCase(
    private val commandFactory: CommandFactory,
    private val logger: Logger,
) {
    suspend operator fun invoke(nfcSession: NfcSession): Result<ComData> {
        logger.i(TAG, "SELECT FILE EF.COM")

        nfcSession
            .transceive(commandFactory.selectFile(Icao9303.File.COM.fileId))
            .flatMap { it.getData() }
            .getOrElse { return Result.failure(it) }

        logger.i(TAG, "READ BINARY EF.COM")

        return nfcSession
            .transceive(commandFactory.readBinary())
            .flatMap { rApdu -> rApdu.getData() }
            .flatMap { comBytes -> comBytes.parseTlv() }
            .flatMap { rootNode -> rootNode.first().children() }
            .flatMap { children -> children.firstWithTag(Icao9303.Tags.TagList) }
            .map { dgListNode ->
                ComData(
                    dataGroups =
                        dgListNode.value.mapNotNull { tag ->
                            Icao9303.DataGroup.fromTag(tag.toUInt())
                        },
                )
            }
    }
}
