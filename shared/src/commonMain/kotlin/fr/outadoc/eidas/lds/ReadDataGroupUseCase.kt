package fr.outadoc.eidas.lds

import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.NfcSession
import fr.outadoc.eidas.nfc.commands.CommandFactory
import fr.outadoc.eidas.utils.flatMap
import fr.outadoc.eidas.utils.toPrettyHex

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
        logger.i(TAG, "SELECT FILE DG ${dataGroupNumber.toPrettyHex()}")

        nfcSession
            .transceive(commandFactory.selectFile(ubyteArrayOf(0x01u, dataGroupNumber)))
            .flatMap { it.getData() }
            .getOrElse { return Result.failure(it) }

        logger.i(TAG, "READ BINARY DG ${dataGroupNumber.toPrettyHex()}")

        val comBytes: UByteArray =
            nfcSession
                .transceive(commandFactory.readBinary())
                .flatMap { it.getData() }
                .getOrElse { return Result.failure(it) }

        return Result.success(comBytes)
    }
}
