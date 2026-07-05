package fr.outadoc.eidas.nfc

object Iso7816 {
    object Aid {
        /**
         * Machine Readable Travel Documents (MRTD)	Electronic (Biometric) Passport.
         *
         * Issuer stored data application (The last three digits of the PIX shall be used to denote future version levels.)
         */
        const val MRTD = "A0000002471001"
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

        /**
         * Dynamic Authentication Data
         *
         * Protocol specific data objects.
         */
        val DynamicAuthenticationData: UByte = 0x7Cu

        val Nonce: UByte = 0x80u
        val MappingData: UByte = 0x81u
        val ChipMappingData: UByte = 0x82u
        val EphemeralPublicKey: UByte = 0x83u
        val ChipPublicKey: UByte = 0x84u
        val AuthenticationToken: UByte = 0x85u
        val ChipAuthenticationToken: UByte = 0x86u
    }
}
