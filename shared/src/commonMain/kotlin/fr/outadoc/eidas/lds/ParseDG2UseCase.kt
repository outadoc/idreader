package fr.outadoc.eidas.lds

import fr.outadoc.eidas.lds.model.Picture
import fr.outadoc.eidas.tlv.TlvNode
import fr.outadoc.eidas.tlv.firstWithTag
import fr.outadoc.eidas.tlv.parseTlv

@OptIn(ExperimentalUnsignedTypes::class)
class ParseDG2UseCase {
    operator fun invoke(rawData: UByteArray): Result<Picture> {
        val tagList: List<TlvNode> =
            rawData
                .parseTlv()
                .getOrElse { return Result.failure(it) }

        val rootNode: TlvNode =
            tagList.firstWithTag(0x7F61u)
                ?: return Result.failure(IllegalStateException("Missing 0x7F61 tag"))

        val biometricInformation: TlvNode =
            rootNode.value
                .parseTlv()
                .getOrElse { return Result.failure(it) }
                .firstWithTag(0x7F60u)
                ?: return Result.failure(IllegalStateException("Missing 0x7F60 tag"))

        val biometricData: TlvNode =
            biometricInformation.value
                .parseTlv()
                .getOrElse { return Result.failure(it) }
                .firstWithTag(0x5F2Eu)
                ?: return Result.failure(IllegalStateException("Missing 0x5F2E tag"))

        return Result.success(
            Picture(
                biometricData.value,
            ),
        )
    }
}
