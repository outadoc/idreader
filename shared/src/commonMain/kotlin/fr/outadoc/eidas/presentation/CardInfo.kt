package fr.outadoc.eidas.presentation

import androidx.compose.runtime.Immutable
import fr.outadoc.eidas.lds.model.CardDump
import fr.outadoc.eidas.lds.model.DocumentPicture

/**
 * Display-ready card data for the view layer, consolidated from [CardDump].
 *
 * Fields present in both DG11 and the MRZ (holder name, date of birth) prefer
 * the DG11 value, falling back to the MRZ.
 */
@Immutable
data class CardInfo(
    val picture: DocumentPicture?,
    // Document
    val documentFormat: String?,
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
