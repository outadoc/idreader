package fr.outadoc.eidas.lds

import fr.outadoc.eidas.lds.model.MrzInfo
import fr.outadoc.eidas.nfc.Icao9303
import fr.outadoc.eidas.tlv.firstWithTag
import fr.outadoc.eidas.tlv.parseTlv
import fr.outadoc.eidas.utils.flatMap

@OptIn(ExperimentalUnsignedTypes::class)
class ParseDG1UseCase(
    private val parseMrzUseCase: ParseMrzUseCase,
) {
    operator fun invoke(rawData: UByteArray): Result<MrzInfo> =
        rawData
            .parseTlv()
            .flatMap { tagList -> tagList.firstWithTag(Icao9303.DataGroup.DG1.tag) }
            .flatMap { rootNode -> rootNode.value.parseTlv() }
            .flatMap { nodes -> nodes.firstWithTag(Icao9303.Tags.MRZ) }
            .flatMap { mrzNode ->
                parseMrzUseCase(
                    mrz =
                        mrzNode.value
                            .toByteArray()
                            .decodeToString(),
                )
            }
}
