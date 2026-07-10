package fr.outadoc.eidas.pace

import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.nfc.NfcSession
import fr.outadoc.eidas.nfc.asn1.SecurityInfo
import fr.outadoc.eidas.nfc.asn1.SecurityInfosParser
import fr.outadoc.eidas.nfc.commands.CommandFactory
import fr.outadoc.eidas.utils.flatMap

private const val TAG = "ReadCardAccessUseCase"

@OptIn(ExperimentalUnsignedTypes::class)
class ReadCardAccessUseCase(
    private val commandFactory: CommandFactory,
    private val securityInfosParser: SecurityInfosParser,
    private val logger: Logger,
) {
    suspend operator fun invoke(nfcSession: NfcSession): Result<List<SecurityInfo>> {
        logger.i(TAG, "SELECT FILE EF.CardAccess")

        nfcSession
            .transceive(commandFactory.selectFile(Iso7816.File.CardAccess.fileId))
            .flatMap { it.getData() }
            .getOrElse { return Result.failure(it) }

        logger.i(TAG, "READ BINARY EF.CardAccess")

        return nfcSession
            .transceive(commandFactory.readBinary())
            .flatMap { rApdu -> rApdu.getData() }
            .flatMap { data -> securityInfosParser.parse(data) }
    }
}
