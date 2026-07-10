package fr.outadoc.eidas.lds

import fr.outadoc.eidas.lds.model.AdditionalPersonalDetails
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.tlv.TlvNode
import fr.outadoc.eidas.tlv.firstWithTag
import fr.outadoc.eidas.tlv.parseTlv

@OptIn(ExperimentalUnsignedTypes::class)
class ParseDG11UseCase(
    private val parseMrzName: ParseMrzNameUseCase,
    private val parse8DigitDate: Parse8DigitDateUseCase,
) {
    operator fun invoke(rawData: UByteArray): Result<AdditionalPersonalDetails> {
        val tagList: List<TlvNode> =
            rawData
                .parseTlv()
                .getOrElse {
                    return Result.failure(it)
                }

        val rootNode: TlvNode =
            tagList
                .firstWithTag(Iso7816.Tags.DG11)
                .getOrElse { return Result.failure(it) }

        val info: List<TlvNode> =
            rootNode.value
                .parseTlv()
                .getOrElse { return Result.failure(it) }

        return Result.success(
            AdditionalPersonalDetails(
                fullNameNationalCharacters =
                    info
                        .firstWithTag(Iso7816.Tags.FullNameNationalCharacters)
                        .getOrNull()
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString()
                        ?.let { parseMrzName(it) },
                personalNumber =
                    info
                        .firstWithTag(Iso7816.Tags.PersonalNumber)
                        .getOrNull()
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString(),
                fullDateOfBirth =
                    info
                        .firstWithTag(Iso7816.Tags.FullDateOfBirth)
                        .getOrNull()
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString()
                        ?.let { parse8DigitDate(it).getOrNull() },
                placeOfBirth =
                    info
                        .firstWithTag(Iso7816.Tags.PlaceOfBirth)
                        .getOrNull()
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString()
                        ?.split('<')
                        ?.filterNot { it.isEmpty() },
                permanentAddress =
                    info
                        .firstWithTag(Iso7816.Tags.PermanentAddress)
                        .getOrNull()
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString()
                        ?.split('<')
                        ?.filterNot { it.isEmpty() },
                telephone =
                    info
                        .firstWithTag(Iso7816.Tags.Telephone)
                        .getOrNull()
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString(),
                profession =
                    info
                        .firstWithTag(Iso7816.Tags.Profession)
                        .getOrNull()
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString(),
                title =
                    info
                        .firstWithTag(Iso7816.Tags.Title)
                        .getOrNull()
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString(),
                personalSummary =
                    info
                        .firstWithTag(Iso7816.Tags.PersonalSummary)
                        .getOrNull()
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString(),
                otherValidTdNumbers =
                    info
                        .firstWithTag(Iso7816.Tags.OtherValidTdNumbers)
                        .getOrNull()
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString(),
                custodyInformation =
                    info
                        .firstWithTag(Iso7816.Tags.CustodyInformation)
                        .getOrNull()
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString(),
            ),
        )
    }
}
