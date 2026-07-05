package fr.outadoc.eidas.nfc.commands

import fr.outadoc.eidas.nfc.CApdu
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.nfc.tlvList

class GeneralAuthenticateCommand {
    fun generalAuthenticate(): CApdu =
        CApdu(
            cla = 0x10u,
            ins = 0x86u,
            p1 = 0x00u,
            p2 = 0x00u,
            data =
                tlvList {
                    tlv(
                        Iso7816.Tags.DynamicAuthenticationData,
                        ubyteArrayOf(),
                    )
                },
            le = 0x00u,
        )
}
