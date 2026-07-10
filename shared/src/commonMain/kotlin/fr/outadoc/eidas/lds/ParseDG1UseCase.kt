package fr.outadoc.eidas.lds

import fr.outadoc.eidas.lds.model.MrzInfo
import fr.outadoc.eidas.nfc.Iso7816
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
            .flatMap { tagList -> tagList.firstWithTag(Iso7816.Tags.DG1) }
            .flatMap { rootNode -> rootNode.value.parseTlv() }
            .flatMap { nodes -> nodes.firstWithTag(Iso7816.Tags.MRZ) }
            .flatMap { mrzNode ->
                parseMrzUseCase(
                    mrz =
                        mrzNode.value
                            .toByteArray()
                            .decodeToString(),
                )
            }
}
