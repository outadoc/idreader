package fr.outadoc.eidas.lds.model

import fr.outadoc.eidas.nfc.Icao9303

data class ComData(
    val dataGroups: List<Icao9303.DataGroup>,
)
