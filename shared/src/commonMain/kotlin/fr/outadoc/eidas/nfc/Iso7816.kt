package fr.outadoc.eidas.nfc

import at.asitplus.signum.indispensable.asn1.ObjectIdentifier

object Iso7816 {
    object Aid {
        /**
         * Machine Readable Travel Documents (MRTD)	Electronic (Biometric) Passport.
         *
         * Issuer stored data application (The last three digits of the PIX shall be used to denote future version levels.)
         */
        const val MRTD = "A0000002471001"
    }

    object AlgorithmOID {
        val PACE_AES256_GM_ECDH_BRAINPOOLP256R1 =
            ObjectIdentifier("0.4.0.127.0.7.2.2.4.2.4")
    }

    object ParameterId {
        val BRAINPOOL_P256R1 = 13
    }

    object File {
        object CardAccess {
            val FILE_ID = ubyteArrayOf(0x01u, 0x1Cu)
            val SHORT_FILE_ID: UByte = 0x1Cu
        }
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
}
