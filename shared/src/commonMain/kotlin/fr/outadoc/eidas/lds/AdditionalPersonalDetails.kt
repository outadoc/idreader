package fr.outadoc.eidas.lds

data class AdditionalPersonalDetails(
    val title: String?,
    val fullNameNationalCharacters: CardHolderName?,
    val personalNumber: String?,
    val fullDateOfBirth: String?,
    val placeOfBirth: String?,
    val permanentAddress: String?,
    val telephone: String?,
    val profession: String?,
    val personalSummary: String?,
    val otherValidTdNumbers: String?,
    val custodyInformation: String?,
)
