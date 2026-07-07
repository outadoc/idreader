package fr.outadoc.eidas.lds

import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.nfc.NfcSession
import fr.outadoc.eidas.nfc.commands.CommandFactory

private const val TAG = "ReadComUseCase"

@OptIn(ExperimentalUnsignedTypes::class)
class ReadComFileUseCase(
    private val commandFactory: CommandFactory,
    private val logger: Logger,
) {
    suspend operator fun invoke(nfcSession: NfcSession): Result<UByteArray> {
        logger.i(TAG, "SELECT FILE EF.COM")

        nfcSession
            .transceive(commandFactory.selectFile(Iso7816.File.COM.fileId))
            .getOrElse { return Result.failure(it) }
            .getData()
            .getOrElse { return Result.failure(it) }

        logger.i(TAG, "READ BINARY EF.COM")

        return nfcSession
            .transceive(commandFactory.readBinary())
            .getOrElse { return Result.failure(it) }
            .getData()
    }
}
