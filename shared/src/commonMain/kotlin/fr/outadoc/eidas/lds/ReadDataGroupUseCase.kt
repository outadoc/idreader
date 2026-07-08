package fr.outadoc.eidas.lds

import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.NfcSession
import fr.outadoc.eidas.nfc.commands.CommandFactory
import fr.outadoc.eidas.utils.flatMap

private val TAG = "ReadDataGroupUseCase"

@OptIn(ExperimentalUnsignedTypes::class)
class ReadDataGroupUseCase(
    private val logger: Logger,
    private val commandFactory: CommandFactory,
) {
    suspend operator fun invoke(
        nfcSession: NfcSession,
        dataGroupNumber: UByte,
    ): Result<UByteArray> {
        logger.i(TAG, "SELECT FILE DG$dataGroupNumber")

        nfcSession
            .transceive(
                commandFactory.selectFile(
                    ubyteArrayOf(FID_RANGE_START, dataGroupNumber),
                ),
            ).flatMap { it.getData() }
            .getOrElse { return Result.failure(it) }

        logger.i(TAG, "READ BINARY DG$dataGroupNumber")

        val comBytes: UByteArray =
            nfcSession
                .transceive(commandFactory.readBinary())
                .flatMap { it.getData() }
                .getOrElse { return Result.failure(it) }

        return Result.success(comBytes)
    }

    private companion object {
        const val FID_RANGE_START: UByte = 0x01u
    }
}
