package fr.outadoc.eidas.lds.model

import androidx.compose.runtime.Immutable

@Immutable
data class MrzInfo(
    val format: String,
    val documentCode: String,
    val documentNumber: String,
    val issuingState: String,
    val nationality: String,
    val cardHolderName: String,
    val birthDate: String,
    val sex: String,
    val expiryDate: String,
    val optionalData1: String?,
    val optionalData2: String?,
)
