package fr.outadoc.eidas.nfc.commands

import fr.outadoc.eidas.nfc.CApdu
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.nfc.tlvList

/**
 * The command MSE:Set AT is used to select and initialize the following protocols: PACE, Chip
 * Authentication, Terminal Authentication, and Restricted Identification.
 */

class MseSetAtCommand {
    companion object {
        val CLA: UByte = 0x00u
        val INS: UByte = 0x22u
    }

    fun paceSetAt(): CApdu =
        CApdu(
            cla = CLA,
            ins = INS,
            p1 = 0xC1u,
            p2 = 0xA4u,
            data =
                tlvList {
                    tlv(
                        Iso7816.Tags.CryptographicMechanismReference,
                        Iso7816.AlgorithmOID.PACE_AES256_GM_ECDH_BRAINPOOLP256R1.bytes,
                    )
                    tlv(
                        Iso7816.Tags.ReferenceOfAPublicKeySecretKey,
                        Iso7816.KeyRef.CAN,
                    )
                },
            le = null,
        )
}
