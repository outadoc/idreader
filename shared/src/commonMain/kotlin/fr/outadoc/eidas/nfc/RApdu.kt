package fr.outadoc.eidas.nfc

import fr.outadoc.eidas.utils.toPrettyHex

@OptIn(ExperimentalUnsignedTypes::class)
class RApdu private constructor(
    val raw: UByteArray,
) {
    companion object {
        fun parse(rapdu: UByteArray): RApdu {
            check(rapdu.size >= 2) {
                "R-APDU must be at least 2 bytes long, was: ${rapdu.toHexString()}"
            }

            return RApdu(rapdu)
        }
    }

    private val data: UByteArray = raw.copyOf(raw.size - 2)
    val sw1: UByte = raw[raw.size - 2]
    val sw2: UByte = raw[raw.size - 1]

    val isSuccess: Boolean
        get() = sw1 == 0x90u.toUByte() && sw2 == 0x00u.toUByte()

    fun getData(): Result<UByteArray> =
        if (isSuccess) {
            Result.success(data)
        } else {
            Result.failure(
                IllegalStateException("Non-success APDU"),
            )
        }

    override fun toString(): String = data.toPrettyHex()
}
