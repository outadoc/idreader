package fr.outadoc.eidas.lds

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
                .getOrElse { return Result.failure(it) }
                .firstWithTag(0x5Cu)
                ?.value
                ?: return Result.failure(
                    IllegalStateException("Could not find DG list in EF.COM"),
                )

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
                0x61u to 1u,
                0x75u to 2u,
                0x63u to 3u,
                0x76u to 4u,
                0x65u to 5u,
                0x66u to 6u,
                0x67u to 7u,
                0x68u to 8u,
                0x69u to 9u,
                0x6Au to 10u,
                0x6Bu to 11u,
                0x6Cu to 12u,
                0x6Du to 13u,
                0x6Eu to 14u,
                0x6Fu to 15u,
                0x70u to 16u,
            )
    }
}
