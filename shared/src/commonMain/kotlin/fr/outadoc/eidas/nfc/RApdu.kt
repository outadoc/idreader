package fr.outadoc.eidas.nfc

import fr.outadoc.eidas.utils.toPrettyHex

@JvmInline
value class RApdu private constructor(
    val data: UByteArray,
) {
    companion object {
        fun parse(data: UByteArray): RApdu {
            check(data.size >= 2) {
                "R-APDU must be at least 2 bytes long, was: ${data.toHexString()}"
            }

            return RApdu(data)
        }
    }

    val sw1: UByte get() = data[data.size - 2]
    val sw2: UByte get() = data[data.size - 1]

    val isSuccess: Boolean
        get() = sw1 == 0x90u.toUByte() && sw2 == 0x00u.toUByte()

    fun assertSuccess() {
        check(isSuccess) { "Command failure: $this" }
    }

    override fun toString(): String = data.toPrettyHex()
}
