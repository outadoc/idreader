package fr.outadoc.eidas.lds

import fr.outadoc.eidas.lds.model.Centimeters
import fr.outadoc.eidas.lds.model.OptionalDetails
import fr.outadoc.eidas.nfc.Icao9303
import fr.outadoc.eidas.tlv.firstWithTag
import fr.outadoc.eidas.tlv.parseTlv
import fr.outadoc.eidas.utils.flatMap

@OptIn(ExperimentalUnsignedTypes::class)
class ParseDG13UseCase {
    operator fun invoke(rawData: UByteArray): Result<OptionalDetails> =
        rawData
            .parseTlv()
            .flatMap { tagList -> tagList.firstWithTag(Icao9303.DataGroup.DG13.tag) }
            .flatMap { rootNode -> rootNode.value.parseTlv() }
            .mapCatching { info ->
                OptionalDetails(
                    height =
                        info
                            .firstWithTag(Icao9303.Tags.Height)
                            .getOrNull()
                            ?.value
                            ?.toByteArray()
                            ?.decodeToString()
                            ?.toInt()
                            ?.let { Centimeters(it) },
                )
            }
}
