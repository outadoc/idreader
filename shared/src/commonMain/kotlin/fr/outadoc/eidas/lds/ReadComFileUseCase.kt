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
            .flatMap { nodes ->
                nodes
                    .firstOrNull()
                    ?.let { rootNode -> Result.success(rootNode) }
                    ?: Result.failure(IllegalStateException("EF.COM is empty"))
            }.flatMap { rootNode -> rootNode.children() }
            .flatMap { children -> children.firstWithTag(0x5Cu) }
            .map { dgListNode ->
                ComData(
                    dataGroupNumbers =
                        dgListNode.value.mapNotNull { tag ->
                            DG_TAG_TO_NUMBER[tag.toUInt()]
                        },
                )
            }
    }

    private companion object {
        val DG_TAG_TO_NUMBER: Map<UInt, UByte> =
            mapOf(
                Icao9303.Tags.DG1 to Icao9303.DataGroup.DG1,
                Icao9303.Tags.DG2 to Icao9303.DataGroup.DG2,
                Icao9303.Tags.DG3 to Icao9303.DataGroup.DG3,
                Icao9303.Tags.DG4 to Icao9303.DataGroup.DG4,
                Icao9303.Tags.DG5 to Icao9303.DataGroup.DG5,
                Icao9303.Tags.DG6 to Icao9303.DataGroup.DG6,
                Icao9303.Tags.DG7 to Icao9303.DataGroup.DG7,
                Icao9303.Tags.DG8 to Icao9303.DataGroup.DG8,
                Icao9303.Tags.DG9 to Icao9303.DataGroup.DG9,
                Icao9303.Tags.DG10 to Icao9303.DataGroup.DG10,
                Icao9303.Tags.DG11 to Icao9303.DataGroup.DG11,
                Icao9303.Tags.DG12 to Icao9303.DataGroup.DG12,
                Icao9303.Tags.DG13 to Icao9303.DataGroup.DG13,
                Icao9303.Tags.DG14 to Icao9303.DataGroup.DG14,
                Icao9303.Tags.DG15 to Icao9303.DataGroup.DG15,
                Icao9303.Tags.DG16 to Icao9303.DataGroup.DG16,
            )
    }
}
