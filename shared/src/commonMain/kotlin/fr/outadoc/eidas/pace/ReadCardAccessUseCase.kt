package fr.outadoc.eidas.pace

import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.nfc.NfcTag
import fr.outadoc.eidas.nfc.NfcTagReader
import fr.outadoc.eidas.nfc.asn1.SecurityInfo
import fr.outadoc.eidas.nfc.asn1.SecurityInfosParser
import fr.outadoc.eidas.nfc.commands.CommandFactory

private const val TAG = "ReadCardAccessUseCase"

@OptIn(ExperimentalUnsignedTypes::class)
class ReadCardAccessUseCase(
    private val tagReader: NfcTagReader,
    private val commandFactory: CommandFactory,
    private val securityInfosParser: SecurityInfosParser,
    private val logger: Logger,
) {
    suspend operator fun invoke(tag: NfcTag): Result<List<SecurityInfo>> = runCatching {
        logger.i(TAG, "SELECT FILE EF.CardAccess")

        tagReader
            .transceive(tag, commandFactory.selectFile(Iso7816.File.CardAccess.FILE_ID))
            .getOrThrow()
            .getDataOrThrow()

        logger.i(TAG, "READ BINARY EF.CardAccess")

        val data =
            tagReader
                .transceive(tag, commandFactory.readBinary())
                .getOrThrow()
                .getDataOrThrow()

        securityInfosParser.parse(data)
    }
}
