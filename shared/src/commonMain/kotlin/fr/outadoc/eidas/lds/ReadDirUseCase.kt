package fr.outadoc.eidas.lds

import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.nfc.NfcSessionManager
import fr.outadoc.eidas.nfc.NfcTag
import fr.outadoc.eidas.nfc.commands.CommandFactory

private const val TAG = "ReadDirUseCase"

@OptIn(ExperimentalUnsignedTypes::class)
class ReadDirUseCase(
    private val commandFactory: CommandFactory,
    private val logger: Logger,
) {
    suspend operator fun invoke(
        tag: NfcTag,
        nfcSessionManager: NfcSessionManager,
    ): Result<UByteArray> {
        logger.i(TAG, "SELECT FILE EF.DIR")

        nfcSessionManager
            .transceive(tag, commandFactory.selectFile(Iso7816.File.DIR.fileId))
            .getOrElse { return Result.failure(it) }
            .getData()
            .getOrElse { return Result.failure(it) }

        logger.i(TAG, "READ BINARY EF.DIR")

        return nfcSessionManager
            .transceive(tag, commandFactory.readBinary())
            .getOrElse { return Result.failure(it) }
            .getData()
    }
}
