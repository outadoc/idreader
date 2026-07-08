package fr.outadoc.eidas.lds

import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.NfcSession
import fr.outadoc.eidas.nfc.commands.CommandFactory

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
        logger.i(TAG, "SELECT FILE DG ${dataGroupNumber.toHexString()}")

        nfcSession
            .transceive(commandFactory.selectFile(ubyteArrayOf(0x01u, dataGroupNumber)))
            .getOrElse { return Result.failure(it) }
            .getData()
            .getOrElse { return Result.failure(it) }

        logger.i(TAG, "READ BINARY DG ${dataGroupNumber.toHexString()}")

        val comBytes: UByteArray =
            nfcSession
                .transceive(commandFactory.readBinary())
                .getOrElse { return Result.failure(it) }
                .getData()
                .getOrElse { return Result.failure(it) }

        return Result.success(comBytes)
    }
}
