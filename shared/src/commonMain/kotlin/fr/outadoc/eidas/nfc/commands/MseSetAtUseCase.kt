package fr.outadoc.eidas.nfc.commands

import fr.outadoc.eidas.nfc.CApdu
import io.github.rafaelrabeloit.bertlv.TLV
import io.github.rafaelrabeloit.bertlv.TLVList

/**
 * The command MSE:Set AT is used to select and initialize the following protocols: PACE, Chip
 * Authentication, Terminal Authentication, and Restricted Identification.
 */

class MseSetAtUseCase {
    companion object {
        val CLA: UByte = 0x00u
        val INS: UByte = 0x22u

        object OID {
            val PACE_AES256_GM_ECDH_BRAINPOOLP256R1 =
                "04007F00070202040204".hexToUByteArray()
        }

        object KeyRef {
            val MRZ: UByte = 0x01u
            val CAN: UByte = 0x02u
            val PIN: UByte = 0x03u
            val PUK: UByte = 0x04u
        }

        object Tags {
            /**
             * Object Identifier of the protocol to select (value only, Tag
             * 0x06 is omitted). This data object is REQUIRED for all
             * protocols except Terminal Authentication in version 1.
             *
             */
            val CryptographicMechanismReference: UByte = 0x80u

            /**
             * Reference of a public key / secret key
             * This data object is REQUIRED for the following protocols:
             * – For PACE to indicate the password to be used:
             * 0x01: MRZ
             * 0x02: CAN
             * 0x03: PIN
             * 0x04: PUK
             * – For Terminal Authentication to select the public key of
             * the terminal by its ISO 8859-1 encoded name.
             */
            val ReferenceOfAPublicKeySecretKey: UByte = 0x83u
        }

        object Protocol {
            object PACE {
                val keyRef: UByte = KeyRef.CAN
            }
        }
    }

    fun paceSetAt(): CApdu =
        CApdu(
            cla = CLA,
            ins = INS,
            p1 = 0xC1u,
            p2 = 0xA4u,
            data =
                TLVList
                    .fromTlvs(
                        listOf(
                            TLV.fromTagAndBinaryValue(
                                Tags.CryptographicMechanismReference.toInt(),
                                OID.PACE_AES256_GM_ECDH_BRAINPOOLP256R1.toByteArray(),
                            ),
                            TLV.fromTagAndBinaryValue(
                                Tags.ReferenceOfAPublicKeySecretKey.toInt(),
                                byteArrayOf(KeyRef.CAN.toByte()),
                            ),
                        ),
                    ).bytes
                    .toUByteArray(),
            le = null,
        )
}
