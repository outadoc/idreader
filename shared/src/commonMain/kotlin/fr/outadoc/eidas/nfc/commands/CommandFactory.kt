package fr.outadoc.eidas.nfc.commands

import fr.outadoc.eidas.nfc.CApdu
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.nfc.tlvList

class CommandFactory {
    fun generalAuthenticate(
        data: UByteArray,
        chained: Boolean = true,
    ): CApdu =
        CApdu(
            cla = if (chained) 0x10u else 0x00u,
            ins = 0x86u,
            p1 = 0x00u,
            p2 = 0x00u,
            data = data,
            le = 0x00u,
        )

    fun paceSetAt(
        algorithm: UByteArray,
        keyReference: UByte,
    ): CApdu =
        CApdu(
            cla = 0x00u,
            ins = 0x22u,
            p1 = 0xC1u,
            p2 = 0xA4u,
            data =
                tlvList {
                    tlv(Iso7816.Tags.CryptographicMechanismReference, algorithm)
                    tlv(Iso7816.Tags.ReferenceOfAPublicKeySecretKey, keyReference)
                },
            le = null,
        )

    fun readBinary(): CApdu =
        CApdu(
            cla = 0x00u,
            ins = 0xB0u,
            p1 = 0x00u,
            p2 = 0x00u,
            le = 0x00u,
        )

    fun selectAid(aid: UByteArray): CApdu =
        CApdu(
            cla = 0x00u,
            ins = 0xA4u,
            p1 = 0x04u,
            p2 = 0x0Cu,
            data = aid,
            le = 0x00u,
        )

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
