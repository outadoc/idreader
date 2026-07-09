package fr.outadoc.eidas.lds.model

data class AdditionalPersonalDetails(
    val title: String?,
    val fullNameNationalCharacters: CardHolderName?,
    val personalNumber: String?,
    val fullDateOfBirth: Date?,
    val placeOfBirth: List<String>?,
    val permanentAddress: List<String>?,
    val telephone: String?,
    val profession: String?,
    val personalSummary: String?,
    val otherValidTdNumbers: String?,
    val custodyInformation: String?,
)
