package fr.outadoc.eidas.lds

import fr.outadoc.eidas.lds.model.ComData
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.nfc.NfcSession
import fr.outadoc.eidas.nfc.commands.CommandFactory
import fr.outadoc.eidas.tlv.TlvNode
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
            .transceive(commandFactory.selectFile(Iso7816.File.COM.fileId))
            .flatMap { it.getData() }
            .getOrElse { return Result.failure(it) }

        logger.i(TAG, "READ BINARY EF.COM")

        val comBytes: UByteArray =
            nfcSession
                .transceive(commandFactory.readBinary())
                .flatMap { it.getData() }
                .getOrElse { return Result.failure(it) }

        val rootNode: TlvNode =
            comBytes
                .parseTlv()
                .getOrElse { return Result.failure(it) }
                .firstOrNull()
                ?: return Result.failure(IllegalStateException("EF.COM is empty"))

        val dgList: UByteArray =
            rootNode
                .children()
                .flatMap { nodes -> nodes.firstWithTag(0x5Cu) }
                .getOrElse { return Result.failure(it) }
                .value

        return Result.success(
            ComData(
                dataGroupNumbers =
                    dgList.mapNotNull { tag ->
                        DG_TAG_TO_NUMBER[tag.toUInt()]
                    },
            ),
        )
    }

    private companion object {
        val DG_TAG_TO_NUMBER: Map<UInt, UByte> =
            mapOf(
                Iso7816.Tags.DG1 to Iso7816.DataGroup.DG1,
                Iso7816.Tags.DG2 to Iso7816.DataGroup.DG2,
                Iso7816.Tags.DG3 to Iso7816.DataGroup.DG3,
                Iso7816.Tags.DG4 to Iso7816.DataGroup.DG4,
                Iso7816.Tags.DG5 to Iso7816.DataGroup.DG5,
                Iso7816.Tags.DG6 to Iso7816.DataGroup.DG6,
                Iso7816.Tags.DG7 to Iso7816.DataGroup.DG7,
                Iso7816.Tags.DG8 to Iso7816.DataGroup.DG8,
                Iso7816.Tags.DG9 to Iso7816.DataGroup.DG9,
                Iso7816.Tags.DG10 to Iso7816.DataGroup.DG10,
                Iso7816.Tags.DG11 to Iso7816.DataGroup.DG11,
                Iso7816.Tags.DG12 to Iso7816.DataGroup.DG12,
                Iso7816.Tags.DG13 to Iso7816.DataGroup.DG13,
                Iso7816.Tags.DG14 to Iso7816.DataGroup.DG14,
                Iso7816.Tags.DG15 to Iso7816.DataGroup.DG15,
                Iso7816.Tags.DG16 to Iso7816.DataGroup.DG16,
            )
    }
}
