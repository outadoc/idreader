package fr.outadoc.eidas.lds

data class MrzInfo(
    val format: String,
    val documentCode: String,
    val issuingState: String,
    val documentNumber: String,
    val documentNumberCheckDigit: String,
    val optionalData1: String,
    val optionalData2: String,
    val birthDateRaw: String,
    val birthDate: String,
    val birthDateCheckDigit: String,
    val sex: String,
    val expiryDate: String,
    val expiryDateCheckDigit: String,
    val nationality: String,
    val compositeCheckDigit: String,
    val surname: String,
    val givenNames: List<String>,
)
