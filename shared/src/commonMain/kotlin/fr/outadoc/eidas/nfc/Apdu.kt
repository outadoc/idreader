package fr.outadoc.eidas.nfc

import fr.outadoc.eidas.utils.parseHex

class CApdu(
    val cla: Byte,
    val ins: Byte,
    val p1: Byte,
    val p2: Byte,
    val data: ByteArray,
    val le: Byte?,
) {
    companion object {
        fun selectAid(aidHex: String): CApdu {
            val aid = aidHex.parseHex()
            return CApdu(
                cla = 0x00,
                ins = 0xA4.toByte(),
                p1 = 0x04,
                p2 = 0x00,
                data = aid,
                le = 0x00,
            )
        }
    }

    fun serialize(): ByteArray {
        return byteArrayOf(
            cla,
            ins,
            p1,
            p2,
            data.size.toByte(),
            *data,
            *if (le != null) {
                byteArrayOf(le)
            } else {
                byteArrayOf()
            },
        )
    }
}
