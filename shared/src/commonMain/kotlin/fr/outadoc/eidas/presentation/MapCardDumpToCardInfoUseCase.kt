package fr.outadoc.eidas.presentation

import fr.outadoc.eidas.lds.model.CardDump
import fr.outadoc.eidas.lds.model.CardHolderName
import fr.outadoc.eidas.lds.model.Date

class MapCardDumpToCardInfoUseCase {
    operator fun invoke(cardDump: CardDump): CardInfo =
        with(cardDump) {
            val name: CardHolderName? =
                additionalPersonalDetails
                    ?.fullNameNationalCharacters
                    ?: mrzInfo?.cardHolderName

            val birthDate: Date? =
                additionalPersonalDetails
                    ?.fullDateOfBirth
                    ?: mrzInfo?.birthDate

            return CardInfo(
                picture = picture,
                documentFormat = mrzInfo?.format,
                documentCode = mrzInfo?.documentCode,
                documentNumber = mrzInfo?.documentNumber,
                issuingState = mrzInfo?.issuingState,
                expiryDate = mrzInfo?.expiryDate?.toString(),
                optionalData1 = mrzInfo?.optionalData1,
                optionalData2 = mrzInfo?.optionalData2,
                title = additionalPersonalDetails?.title,
                surname = name?.surname,
                givenNames =
                    name
                        ?.givenNames
                        ?.joinToString(separator = ", "),
                nationality = mrzInfo?.nationality,
                birthDate = birthDate?.toString(),
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
