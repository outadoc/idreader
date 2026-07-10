package fr.outadoc.eidas.lds

import fr.outadoc.eidas.lds.model.AdditionalPersonalDetails
import fr.outadoc.eidas.nfc.Icao9303
import fr.outadoc.eidas.tlv.TlvNode
import fr.outadoc.eidas.tlv.firstWithTag
import fr.outadoc.eidas.tlv.parseTlv
import fr.outadoc.eidas.utils.flatMap

@OptIn(ExperimentalUnsignedTypes::class)
class ParseDG11UseCase(
    private val parseMrzName: ParseMrzNameUseCase,
    private val parse8DigitDate: Parse8DigitDateUseCase,
) {
    operator fun invoke(rawData: UByteArray): Result<AdditionalPersonalDetails> {
        val info: List<TlvNode> =
            rawData
                .parseTlv()
                .flatMap { tagList -> tagList.firstWithTag(Icao9303.Tags.DG11) }
                .flatMap { rootNode -> rootNode.value.parseTlv() }
                .getOrElse { return Result.failure(it) }

        return Result.success(
            AdditionalPersonalDetails(
                fullNameNationalCharacters =
                    info
                        .firstWithTag(Icao9303.Tags.FullNameNationalCharacters)
                        .getOrNull()
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString()
                        ?.let { parseMrzName(it) },
                personalNumber =
                    info
                        .firstWithTag(Icao9303.Tags.PersonalNumber)
                        .getOrNull()
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString(),
                fullDateOfBirth =
                    info
                        .firstWithTag(Icao9303.Tags.FullDateOfBirth)
                        .getOrNull()
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString()
                        ?.let { parse8DigitDate(it).getOrNull() },
                placeOfBirth =
                    info
                        .firstWithTag(Icao9303.Tags.PlaceOfBirth)
                        .getOrNull()
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString()
                        ?.split('<')
                        ?.filterNot { it.isEmpty() },
                permanentAddress =
                    info
                        .firstWithTag(Icao9303.Tags.PermanentAddress)
                        .getOrNull()
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString()
                        ?.split('<')
                        ?.filterNot { it.isEmpty() },
                telephone =
                    info
                        .firstWithTag(Icao9303.Tags.Telephone)
                        .getOrNull()
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString(),
                profession =
                    info
                        .firstWithTag(Icao9303.Tags.Profession)
                        .getOrNull()
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString(),
                title =
                    info
                        .firstWithTag(Icao9303.Tags.Title)
                        .getOrNull()
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString(),
                personalSummary =
                    info
                        .firstWithTag(Icao9303.Tags.PersonalSummary)
                        .getOrNull()
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString(),
                otherValidTdNumbers =
                    info
                        .firstWithTag(Icao9303.Tags.OtherValidTdNumbers)
                        .getOrNull()
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString(),
                custodyInformation =
                    info
                        .firstWithTag(Icao9303.Tags.CustodyInformation)
                        .getOrNull()
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString(),
            ),
        )
    }
}
