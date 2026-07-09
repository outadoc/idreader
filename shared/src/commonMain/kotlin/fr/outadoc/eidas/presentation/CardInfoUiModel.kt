package fr.outadoc.eidas.presentation

import androidx.compose.runtime.Immutable
import fr.outadoc.eidas.lds.model.CardDump
import fr.outadoc.eidas.lds.model.CardHolderName
import fr.outadoc.eidas.lds.model.Date
import fr.outadoc.eidas.lds.model.DocumentPicture

/**
 * Display-ready card data for the view layer, consolidated from [CardDump].
 *
 * Fields present in both DG11 and the MRZ (holder name, date of birth) prefer
 * the DG11 value, falling back to the MRZ.
 */
@Immutable
data class CardInfoUiModel(
    val picture: DocumentPicture?,
    // Document
    val format: String?,
    val documentCode: String?,
    val documentNumber: String?,
    val issuingState: String?,
    val expiryDate: String?,
    val optionalData1: String?,
    val optionalData2: String?,
    // Holder
    val title: String?,
    val surname: String?,
    val givenNames: String?,
    val nationality: String?,
    val birthDate: String?,
    val placeOfBirth: String?,
    val sex: String?,
    val height: String?,
    val personalNumber: String?,
    val permanentAddress: String?,
    val telephone: String?,
    val profession: String?,
    val personalSummary: String?,
    val otherValidTdNumbers: String?,
    val custodyInformation: String?,
)

fun CardDump.toCardInfoUiModel(): CardInfoUiModel {
    val name: CardHolderName? =
        additionalPersonalDetails?.fullNameNationalCharacters ?: mrzInfo?.cardHolderName
    val birthDate: Date? =
        additionalPersonalDetails?.fullDateOfBirth ?: mrzInfo?.birthDate

    return CardInfoUiModel(
        picture = picture,
        format = mrzInfo?.format,
        documentCode = mrzInfo?.documentCode,
        documentNumber = mrzInfo?.documentNumber,
        issuingState = mrzInfo?.issuingState,
        expiryDate = mrzInfo?.expiryDate?.toString(),
        optionalData1 = mrzInfo?.optionalData1,
        optionalData2 = mrzInfo?.optionalData2,
        title = additionalPersonalDetails?.title,
        surname = name?.surname,
        givenNames = name?.givenNames?.joinToString(separator = ", "),
        nationality = mrzInfo?.nationality,
        birthDate = birthDate?.toString(),
        placeOfBirth = additionalPersonalDetails?.placeOfBirth?.joinToString(separator = ", "),
        sex = mrzInfo?.sex,
        height = optionalDetails?.height?.let { height -> "${height.cm} cm" },
        personalNumber = additionalPersonalDetails?.personalNumber,
        permanentAddress = additionalPersonalDetails?.permanentAddress?.joinToString(separator = "\n"),
        telephone = additionalPersonalDetails?.telephone,
        profession = additionalPersonalDetails?.profession,
        personalSummary = additionalPersonalDetails?.personalSummary,
        otherValidTdNumbers = additionalPersonalDetails?.otherValidTdNumbers,
        custodyInformation = additionalPersonalDetails?.custodyInformation,
    )
}
