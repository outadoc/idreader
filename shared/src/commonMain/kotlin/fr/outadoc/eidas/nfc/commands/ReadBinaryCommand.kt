package fr.outadoc.eidas.nfc.commands

import fr.outadoc.eidas.nfc.CApdu

class ReadBinaryCommand {
    fun readBinary(): CApdu =
        CApdu(
            cla = 0x00u,
            ins = 0xB0u,
            p1 = 0x00u,
            p2 = 0x00u,
            le = 0x00u,
        )
}
