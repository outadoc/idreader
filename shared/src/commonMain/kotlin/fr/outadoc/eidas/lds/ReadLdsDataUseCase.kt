package fr.outadoc.eidas.lds

import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.logging.w
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.nfc.NfcSession
import fr.outadoc.eidas.nfc.commands.CommandFactory
import fr.outadoc.eidas.utils.flatMap
import fr.outadoc.eidas.utils.toPrettyHex

private val TAG = "ReadLdsDataUseCase"

@OptIn(ExperimentalUnsignedTypes::class)
class ReadLdsDataUseCase(
    private val commandFactory: CommandFactory,
    private val logger: Logger,
    private val readComFile: ReadComFileUseCase,
    private val readDataGroup: ReadDataGroupUseCase,
) {
    suspend operator fun invoke(nfcSession: NfcSession): Result<LdsDump> {
        logger.i(TAG, "Select MRTD application")

        nfcSession
            .transceive(
                commandFactory.selectAid(
                    Iso7816.Aid.MRTD.hexToUByteArray(),
                ),
            ).flatMap { it.getData() }
            .getOrElse { return Result.failure(it) }

        val comData: ComData =
            readComFile(
                nfcSession = nfcSession,
            ).getOrElse {
                return Result.failure(it)
            }

        logger.i(TAG, "COM data: $comData")

        val dataGroupContents: Map<UByte, UByteArray?> =
            comData
                .dataGroupNumbers
                .associateWith { dgNumber ->
                    readDataGroup(
                        nfcSession = nfcSession,
                        dataGroupNumber = dgNumber,
                    ).onFailure {
                        logger.w(TAG, "Failed to read DG #${dgNumber.toPrettyHex()}")
                    }.getOrNull()
                }

        return Result.success(
            LdsDump(),
        )
    }
}
