package fr.outadoc.eidas.lds

import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.nfc.NfcSessionManager
import fr.outadoc.eidas.nfc.NfcTag
import fr.outadoc.eidas.nfc.commands.CommandFactory

private val TAG = "ReadLdsDataUseCase"

@OptIn(ExperimentalUnsignedTypes::class)
class ReadLdsDataUseCase(
    private val commandFactory: CommandFactory,
    private val logger: Logger,
    private val readDir: ReadDirUseCase,
) {
    suspend operator fun invoke(
        tag: NfcTag,
        secureSession: NfcSessionManager,
    ): Result<LdsDump> {
        secureSession.transceive(
            tag,
            commandFactory.selectAid(
                Iso7816.Aid.MRTD.hexToUByteArray(),
            ),
        )

        readDir(
            tag = tag,
            nfcSessionManager = secureSession,
        )

        return Result.failure(NotImplementedError())
    }
}
