package fr.outadoc.eidas.lds

import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.tlv.TlvNode
import fr.outadoc.eidas.tlv.firstWithTag
import fr.outadoc.eidas.tlv.parseTlv

@OptIn(ExperimentalUnsignedTypes::class)
class ParseDG11UseCase(
    private val parseMrzName: ParseMrzNameUseCase,
) {
    operator fun invoke(rawData: UByteArray): Result<AdditionalPersonalDetails> {
        val tagList: List<TlvNode> =
            rawData
                .parseTlv()
                .getOrElse {
                    return Result.failure(it)
                }

        val rootNode: TlvNode =
            tagList.firstWithTag(0x6Bu)
                ?: return Result.failure(IllegalStateException("Missing 0x6B tag"))

        val info: List<TlvNode> =
            rootNode.value
                .parseTlv()
                .getOrElse { return Result.failure(it) }

        return Result.success(
            AdditionalPersonalDetails(
                fullNameNationalCharacters =
                    info
                        .firstWithTag(Iso7816.Tags.FullNameNationalCharacters)
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString()
                        ?.let { parseMrzName(it) },
                personalNumber =
                    info
                        .firstWithTag(Iso7816.Tags.PersonalNumber)
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString(),
                fullDateOfBirth =
                    info
                        .firstWithTag(Iso7816.Tags.FullDateOfBirth)
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString(),
                placeOfBirth =
                    info
                        .firstWithTag(Iso7816.Tags.PlaceOfBirth)
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString(),
                permanentAddress =
                    info
                        .firstWithTag(Iso7816.Tags.PermanentAddress)
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString(),
                telephone =
                    info
                        .firstWithTag(Iso7816.Tags.Telephone)
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString(),
                profession =
                    info
                        .firstWithTag(Iso7816.Tags.Profession)
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString(),
                title =
                    info
                        .firstWithTag(Iso7816.Tags.Title)
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString(),
                personalSummary =
                    info
                        .firstWithTag(Iso7816.Tags.PersonalSummary)
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString(),
                otherValidTdNumbers =
                    info
                        .firstWithTag(Iso7816.Tags.OtherValidTdNumbers)
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString(),
                custodyInformation =
                    info
                        .firstWithTag(Iso7816.Tags.CustodyInformation)
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString(),
            ),
        )
    }
}
