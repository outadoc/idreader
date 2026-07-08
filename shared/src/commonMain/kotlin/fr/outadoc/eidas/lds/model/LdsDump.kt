package fr.outadoc.eidas.lds.model

data class LdsDump(
    val mrzInfo: MrzInfo?,
    val additionalPersonalDetails: AdditionalPersonalDetails?,
    val picture: Picture?,
)
