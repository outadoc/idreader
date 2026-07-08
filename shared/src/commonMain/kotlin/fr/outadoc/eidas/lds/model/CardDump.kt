package fr.outadoc.eidas.lds.model

import androidx.compose.runtime.Immutable

@Immutable
data class CardDump(
    val mrzInfo: MrzInfo?,
    val additionalPersonalDetails: AdditionalPersonalDetails?,
    val picture: DocumentPicture?,
)
