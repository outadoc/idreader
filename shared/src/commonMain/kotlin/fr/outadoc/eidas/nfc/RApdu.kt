package fr.outadoc.eidas.nfc

import fr.outadoc.eidas.utils.toPrettyHex

@JvmInline
value class RApdu private constructor(
    val data: ByteArray,
) {
    companion object {
        fun parse(data: ByteArray): RApdu {
            check(data.size >= 2) {
                "R-APDU must be at least 2 bytes long, was: ${data.toHexString()}"
            }

            return RApdu(data)
        }
    }

    val sw1: Byte get() = data[data.size - 2]
    val sw2: Byte get() = data[data.size - 1]

    val isSuccess: Boolean
        get() = sw1 == 0x90.toByte() && sw2 == 0x00.toByte()

    override fun toString(): String = data.toPrettyHex()
}
