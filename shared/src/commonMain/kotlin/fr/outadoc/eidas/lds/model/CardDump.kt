package fr.outadoc.eidas.lds.model

data class CardDump(
    val mrzInfo: MrzInfo?,
    val additionalPersonalDetails: AdditionalPersonalDetails?,
    val picture: Picture?,
)
