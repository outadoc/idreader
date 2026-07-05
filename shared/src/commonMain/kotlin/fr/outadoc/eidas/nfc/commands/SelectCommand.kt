package fr.outadoc.eidas.nfc.commands

import fr.outadoc.eidas.nfc.CApdu
import fr.outadoc.eidas.utils.parseHex

class SelectCommand {
    fun selectAid(aidHex: String): CApdu {
        val aid = aidHex.parseHex()
        return CApdu(
            cla = 0x00u,
            ins = 0xA4u,
            p1 = 0x04u,
            p2 = 0x00u,
            data = aid,
            le = 0x00u,
        )
    }
}
