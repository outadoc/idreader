package fr.outadoc.eidas.lds

import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.nfc.NfcSession
import fr.outadoc.eidas.nfc.commands.CommandFactory
import io.github.rafaelrabeloit.bertlv.TLV
import io.github.rafaelrabeloit.bertlv.TLVList

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
            .getOrElse { return Result.failure(it) }
            .getData()
            .getOrElse { return Result.failure(it) }

        logger.i(TAG, "READ BINARY EF.COM")

        val comBytes: UByteArray =
            nfcSession
                .transceive(commandFactory.readBinary())
                .getOrElse { return Result.failure(it) }
                .getData()
                .getOrElse { return Result.failure(it) }

        val rootTagValue: TLV<ByteArray> = TLV.fromBinaryTlvBuffer(comBytes.toByteArray())
        val rootList: TLVList = TLVList.fromTlvListBuffer(rootTagValue.value)

        val dgList: UByteArray =
            (rootList.find(0x5C)?.value as ByteArray?)?.toUByteArray()
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
        val FID_RANGE_START = 0x0100
        val DG_TAG_TO_NUMBER: Map<UInt, UByte> =
            mapOf(
                0x61u to 0x01u,
                0x75u to 0x02u,
                0x63u to 0x03u,
                0x76u to 0x04u,
                0x65u to 0x05u,
                0x66u to 0x06u,
                0x67u to 0x07u,
                0x68u to 0x08u,
                0x69u to 0x09u,
                0x6Au to 0x0Au,
                0x6Bu to 0x0Bu,
                0x6Cu to 0x0Cu,
                0x6Du to 0x0Du,
                0x6Eu to 0x0Eu,
                0x6Fu to 0x0Fu,
                0x70u to 0x10u,
            )
    }
}
