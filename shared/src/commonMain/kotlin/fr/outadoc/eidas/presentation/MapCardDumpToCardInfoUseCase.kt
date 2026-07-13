package fr.outadoc.eidas.presentation

import fr.outadoc.eidas.lds.Parse6DigitDateUseCase
import fr.outadoc.eidas.lds.ParseMrzNameUseCase
import fr.outadoc.eidas.lds.model.CardDump
import fr.outadoc.eidas.lds.model.CardHolderName
import fr.outadoc.eidas.lds.model.Date

class MapCardDumpToCardInfoUseCase(
    private val parse6DigitDateUseCase: Parse6DigitDateUseCase,
    private val parseMrzName: ParseMrzNameUseCase,
) {
    operator fun invoke(cardDump: CardDump): CardInfo =
        with(cardDump) {
            val name: CardHolderName? =
                additionalPersonalDetails?.fullNameNationalCharacters
                    ?: mrzInfo?.cardHolderName?.let { parseMrzName(it) }

            val birthDate: Date? =
                additionalPersonalDetails?.fullDateOfBirth
                    ?: mrzInfo?.birthDate?.let {
                        parse6DigitDateUseCase(
                            date = it,
                            dateIsIn = Parse6DigitDateUseCase.DateIsIn.PAST,
                        ).getOrNull()
                    }

            return CardInfo(
                picture = picture,
                documentFormat = mrzInfo?.format,
                documentCode = mrzInfo?.documentCode,
                documentNumber = mrzInfo?.documentNumber,
                issuingState = mrzInfo?.issuingState,
                expiryDate =
                    mrzInfo?.expiryDate?.let {
                        parse6DigitDateUseCase(
                            date = it,
                            dateIsIn = Parse6DigitDateUseCase.DateIsIn.FUTURE,
                        ).getOrNull()
                    },
                optionalData1 = mrzInfo?.optionalData1,
                optionalData2 = mrzInfo?.optionalData2,
                title = additionalPersonalDetails?.title,
                surname = name?.surname,
                givenNames =
                    name
                        ?.givenNames
                        ?.joinToString(separator = ", "),
                nationality = mrzInfo?.nationality,
                birthDate = birthDate,
                placeOfBirth =
                    additionalPersonalDetails
                        ?.placeOfBirth
                        ?.joinToString(separator = ", "),
                sex = mrzInfo?.sex,
                height =
                    optionalDetails
                        ?.height
                        ?.let { height -> "${height.cm} cm" },
                personalNumber = additionalPersonalDetails?.personalNumber,
                permanentAddress =
                    additionalPersonalDetails
                        ?.permanentAddress
                        ?.joinToString(
                            separator = "\n",
                        ),
                telephone = additionalPersonalDetails?.telephone,
                profession = additionalPersonalDetails?.profession,
                personalSummary = additionalPersonalDetails?.personalSummary,
                otherValidTdNumbers = additionalPersonalDetails?.otherValidTdNumbers,
                custodyInformation = additionalPersonalDetails?.custodyInformation,
            )
        }
}
