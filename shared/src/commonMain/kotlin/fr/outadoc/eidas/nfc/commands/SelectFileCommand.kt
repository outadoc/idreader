package fr.outadoc.eidas.nfc.commands

import fr.outadoc.eidas.nfc.CApdu

class SelectFileCommand {
    fun selectFile(fileId: UByteArray): CApdu =
        CApdu(
            cla = 0x00u,
            ins = 0xA4u,
            p1 = 0x02u,
            p2 = 0x0Cu,
            data = fileId,
            le = 0x00u,
        )
}
