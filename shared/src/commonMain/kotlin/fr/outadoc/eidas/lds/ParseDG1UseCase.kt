package fr.outadoc.eidas.lds

import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.tlv.TlvNode
import fr.outadoc.eidas.tlv.firstWithTag
import fr.outadoc.eidas.tlv.parseTlv

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
            tagList.firstWithTag(0x61u)
                ?: return Result.failure(IllegalStateException("Missing 0x61 tag"))

        val mrzBytes: UByteArray =
            (
                rootNode.value
                    .parseTlv()
                    .getOrElse { return Result.failure(it) }
                    .firstWithTag(Iso7816.Tags.MRZ)
                    ?: return Result.failure(IllegalStateException("Missing 0x5F1F tag"))
            ).value

        val mrz: String = mrzBytes.toByteArray().decodeToString()

        return parseMrzUseCase(mrz)
    }
}
