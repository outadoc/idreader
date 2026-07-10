package fr.outadoc.eidas.lds

import fr.outadoc.eidas.lds.model.MrzInfo
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.tlv.TlvNode
import fr.outadoc.eidas.tlv.firstWithTag
import fr.outadoc.eidas.tlv.parseTlv
import fr.outadoc.eidas.utils.flatMap

@OptIn(ExperimentalUnsignedTypes::class)
class ParseDG1UseCase(
    private val parseMrzUseCase: ParseMrzUseCase,
) {
    operator fun invoke(rawData: UByteArray): Result<MrzInfo> {
        val tagList: List<TlvNode> =
            rawData
                .parseTlv()
                .getOrElse {
                    return Result.failure(it)
                }

        val rootNode: TlvNode =
            tagList
                .firstWithTag(Iso7816.Tags.DG1)
                .getOrElse { return Result.failure(it) }

        val mrzBytes: UByteArray =
            rootNode.value
                .parseTlv()
                .flatMap { nodes -> nodes.firstWithTag(Iso7816.Tags.MRZ) }
                .getOrElse { return Result.failure(it) }
                .value

        val mrz: String = mrzBytes.toByteArray().decodeToString()

        return parseMrzUseCase(mrz)
    }
}
