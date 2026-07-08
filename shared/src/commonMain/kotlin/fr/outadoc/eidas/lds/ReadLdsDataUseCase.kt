package fr.outadoc.eidas.lds

import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.nfc.NfcSession
import fr.outadoc.eidas.nfc.commands.CommandFactory

private val TAG = "ReadLdsDataUseCase"

@OptIn(ExperimentalUnsignedTypes::class)
class ReadLdsDataUseCase(
    private val commandFactory: CommandFactory,
    private val logger: Logger,
    private val readComFile: ReadComFileUseCase,
) {
    suspend operator fun invoke(nfcSession: NfcSession): Result<LdsDump> {
        logger.i(TAG, "Select MRTD application")

        nfcSession
            .transceive(
                commandFactory.selectAid(
                    Iso7816.Aid.MRTD.hexToUByteArray(),
                ),
            ).getOrElse { return Result.failure(it) }
            .getData()
            .getOrElse { return Result.failure(it) }

        val comData: ComData =
            readComFile(
                nfcSession = nfcSession,
            ).getOrElse {
                return Result.failure(it)
            }

        logger.i(TAG, "COM data: $comData")

        return Result.success(
            LdsDump(),
        )
    }
}
