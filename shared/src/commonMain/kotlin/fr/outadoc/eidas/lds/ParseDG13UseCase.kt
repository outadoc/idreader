package fr.outadoc.eidas.lds

import fr.outadoc.eidas.lds.model.Centimeters
import fr.outadoc.eidas.lds.model.OptionalDetails
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.tlv.TlvNode
import fr.outadoc.eidas.tlv.firstWithTag
import fr.outadoc.eidas.tlv.parseTlv

@OptIn(ExperimentalUnsignedTypes::class)
class ParseDG13UseCase {
    operator fun invoke(rawData: UByteArray): Result<OptionalDetails> {
        val tagList: List<TlvNode> =
            rawData
                .parseTlv()
                .getOrElse {
                    return Result.failure(it)
                }

        val rootNode: TlvNode =
            tagList
                .firstWithTag(Iso7816.Tags.DG13)
                .getOrElse { return Result.failure(it) }

        val info: List<TlvNode> =
            rootNode.value
                .parseTlv()
                .getOrElse { return Result.failure(it) }

        return Result.success(
            OptionalDetails(
                height =
                    info
                        .firstWithTag(Iso7816.Tags.Height)
                        .getOrNull()
                        ?.value
                        ?.toByteArray()
                        ?.decodeToString()
                        ?.toInt()
                        ?.let { Centimeters(it) },
            ),
        )
    }
}
