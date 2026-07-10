@file:OptIn(ExperimentalUnsignedTypes::class)

package fr.outadoc.eidas.pace

import fr.outadoc.eidas.nfc.Icao9303
import fr.outadoc.eidas.tlv.TlvNode
import fr.outadoc.eidas.tlv.firstWithTag
import fr.outadoc.eidas.tlv.parseTlv
import fr.outadoc.eidas.utils.flatMap

internal fun UByteArray.parseDynamicAuthData(): Result<List<TlvNode>> =
    parseTlv()
        .flatMap { outer -> outer.firstWithTag(Icao9303.Tags.DynamicAuthenticationData) }
        .flatMap { node -> node.children() }
