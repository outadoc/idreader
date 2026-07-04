package fr.outadoc.eidas.nfc

import fr.outadoc.eidas.utils.parseHex

object Apdu {
    val AID_MRTD = "A0000002471001"

    fun selectAidCommand(aidHex: String): ByteArray {
        val aid = aidHex.parseHex()
        return byteArrayOf(
            0x00, 0xA4.toByte(), 0x04, 0x00, aid.size.toByte(),
            *aid,
            0x00,
        )
    }
}
