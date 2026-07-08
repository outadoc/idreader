package fr.outadoc.eidas.lds

import fr.outadoc.eidas.tlv.TlvNode
import fr.outadoc.eidas.tlv.firstWithTag
import fr.outadoc.eidas.tlv.parseTlv

@OptIn(ExperimentalUnsignedTypes::class)
class ParseDG1UseCase {
    suspend operator fun invoke(rawData: UByteArray): Result<MrzInfo> {
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
                    .firstWithTag(0x5F1Fu)
                    ?: return Result.failure(IllegalStateException("Missing 0x5F1F tag"))
            ).value

        return Result.success(
            MrzInfo(
                format = TODO(),
                documentCode = TODO(),
                issuingState = TODO(),
                documentNumber = TODO(),
                documentNumberCheckDigit = TODO(),
                optionalData1 = TODO(),
                birthDateRaw = TODO(),
                birthDate = TODO(),
                birthDateCheckDigit = TODO(),
                sex = TODO(),
                expiryDateRaw = TODO(),
                expiryDate = TODO(),
                expiryDateCheckDigit = TODO(),
                nationality = TODO(),
                optionalData2 = TODO(),
                compositeCheckDigit = TODO(),
                surname = TODO(),
                givenNames = TODO(),
            ),
        )
    }
}
