package fr.outadoc.eidas.lds

import fr.outadoc.eidas.lds.model.AdditionalPersonalDetails
import fr.outadoc.eidas.lds.model.CardDump
import fr.outadoc.eidas.lds.model.ComData
import fr.outadoc.eidas.lds.model.DocumentPicture
import fr.outadoc.eidas.lds.model.MrzInfo
import fr.outadoc.eidas.lds.model.OptionalDetails
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.logging.w
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.nfc.NfcSession
import fr.outadoc.eidas.nfc.commands.CommandFactory
import fr.outadoc.eidas.utils.flatMap

private val TAG = "ReadLdsDataUseCase"

@OptIn(ExperimentalUnsignedTypes::class)
class ReadCardDataUseCase(
    private val commandFactory: CommandFactory,
    private val logger: Logger,
    private val readComFile: ReadComFileUseCase,
    private val readDataGroup: ReadDataGroupUseCase,
    private val parseDG1: ParseDG1UseCase,
    private val parseDG2: ParseDG2UseCase,
    private val parseDG11: ParseDG11UseCase,
    private val parseDG13: ParseDG13UseCase,
) {
    suspend operator fun invoke(nfcSession: NfcSession): Result<CardDump> {
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
                    ).onFailure { e ->
                        logger.w(TAG, "Failed to read DG$dgNumber", e)
                    }.getOrNull()
                }

        val mrzInfo: MrzInfo? =
            dataGroupContents[Iso7816.DataGroup.DG1]?.let { fileBytes ->
                parseDG1(fileBytes)
                    .onFailure { e -> logger.w(TAG, "Failed to parse DG1", e) }
                    .getOrNull()
            }

        val picture: DocumentPicture? =
            dataGroupContents[Iso7816.DataGroup.DG2]?.let { fileBytes ->
                parseDG2(fileBytes)
                    .onFailure { e -> logger.w(TAG, "Failed to parse DG2", e) }
                    .getOrNull()
            }

        val additionalPersonalDetails: AdditionalPersonalDetails? =
            dataGroupContents[Iso7816.DataGroup.DG11]?.let { fileBytes ->
                parseDG11(fileBytes)
                    .onFailure { e -> logger.w(TAG, "Failed to parse DG11", e) }
                    .getOrNull()
            }
        val optionalDetails: OptionalDetails? =
            dataGroupContents[Iso7816.DataGroup.DG13]?.let { fileBytes ->
                parseDG13(fileBytes)
                    .onFailure { e -> logger.w(TAG, "Failed to parse DG13", e) }
                    .getOrNull()
            }

        return Result.success(
            CardDump(
                mrzInfo = mrzInfo,
                additionalPersonalDetails = additionalPersonalDetails,
                optionalDetails = optionalDetails,
                picture = picture,
            ),
        )
    }
}
