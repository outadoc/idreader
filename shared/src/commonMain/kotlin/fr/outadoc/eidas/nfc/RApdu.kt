package fr.outadoc.eidas.nfc

import fr.outadoc.eidas.utils.toPrettyHex

class RApdu private constructor(
    val data: UByteArray,
    val sw1: UByte,
    val sw2: UByte,
) {
    companion object {
        fun parse(data: UByteArray): RApdu {
            check(data.size >= 2) {
                "R-APDU must be at least 2 bytes long, was: ${data.toHexString()}"
            }

            return RApdu(
                data = data.copyOf(data.size - 2),
                sw1 = data[data.size - 2],
                sw2 = data[data.size - 1],
            )
        }
    }

    val isSuccess: Boolean
        get() = sw1 == 0x90u.toUByte() && sw2 == 0x00u.toUByte()

    fun assertSuccess() {
        check(isSuccess) { "Command failure: $this" }
    }

    override fun toString(): String = data.toPrettyHex()
}
