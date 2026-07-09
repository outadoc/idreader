package fr.outadoc.eidas.nfc

@OptIn(ExperimentalUnsignedTypes::class)
object Iso7816 {
    object Aid {
        /**
         * Machine Readable Travel Documents (MRTD)	Electronic (Biometric) Passport.
         *
         * Issuer stored data application (The last three digits of the PIX shall be used to denote future version levels.)
         */
        const val MRTD = "A0000002471001"
    }

    enum class File(
        val fileId: UByteArray,
    ) {
        CardAccess(fileId = ubyteArrayOf(0x01u, 0x1Cu)),
        COM(fileId = ubyteArrayOf(0x01u, 0x1Eu)),
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
        val PaddingContentIndicatorFollowedByCryptogram: UByte = 0x87u
        val ProtectedLe: UByte = 0x97u
        val ProcessingStatus: UByte = 0x99u
        val CryptographicChecksum: UByte = 0x8Eu

        /**
         * Full name of document holder in national characters. Encoded per Doc 9303 rules.
         */
        val FullNameNationalCharacters: UInt = 0x5F0Eu

        /**
         * Content-specific class. Contains:
         * - Tag `02`, length `01`: Number of other names.
         * - Tag `5F0F` (repeating): Other name, once per name counted by `02`.
         */
        val ContentSpecificClass: UByte = 0xA0u

        /** Other name formatted per Doc 9303. Repeats as many times as indicated by tag `02`.
         */
        val OtherName: UInt = 0x5F0Fu

        /**
         *  Personal number.
         */
        val PersonalNumber: UInt = 0x5F10u

        /**
         *  Full date of birth, encoded as yyyymmdd.
         */
        val FullDateOfBirth: UInt = 0x5F2Bu

        /**
         * Place of birth. Fields separated by `<`.
         */
        val PlaceOfBirth: UInt = 0x5F11u

        /**
         * Permanent address. Fields separated by `<`.
         */
        val PermanentAddress: UInt = 0x5F42u

        /**
         * Telephone.
         */
        val Telephone: UInt = 0x5F12u

        /**
         *  Profession.
         */
        val Profession: UInt = 0x5F13u

        /**
         * Title.
         */
        val Title: UInt = 0x5F14u

        /**
         * Personal summary.
         */
        val PersonalSummary: UInt = 0x5F15u

        /**
         * Proof of citizenship. Compressed image per [ISO/IEC 10918].
         */
        val ProofOfCitizenship: UInt = 0x5F16u

        /**
         * Other valid TD numbers. Separated by `<`.
         */
        val OtherValidTdNumbers: UInt = 0x5F17u

        /**
         * Custody information.
         */
        val CustodyInformation: UInt = 0x5F18u

        val MRZ: UInt = 0x5F1Fu

        val Height: UInt = 0x9F01u

        val DG1: UInt = 0x61u
        val DG2: UInt = 0x75u
        val DG3: UInt = 0x63u
        val DG4: UInt = 0x76u
        val DG5: UInt = 0x65u
        val DG6: UInt = 0x66u
        val DG7: UInt = 0x67u
        val DG8: UInt = 0x68u
        val DG9: UInt = 0x69u
        val DG10: UInt = 0x6Au
        val DG11: UInt = 0x6Bu
        val DG12: UInt = 0x6Cu
        val DG13: UInt = 0x6Du
        val DG14: UInt = 0x6Eu
        val DG15: UInt = 0x6Fu
        val DG16: UInt = 0x70u
    }

    object DataGroup {
        /**
         * Machine Readable Zone Information
         */
        val DG1: UByte = 1u

        /**
         * Encoded Identification Features (Face)
         */
        val DG2: UByte = 2u

        /**
         * Additional Identification Feature (Finger(s))
         */
        val DG3: UByte = 3u

        /**
         * Additional Identification Feature (Iris(es))
         */
        val DG4: UByte = 4u

        /**
         * Displayed Portrait
         */
        val DG5: UByte = 5u

        /**
         * RFU
         */
        val DG6: UByte = 6u

        /**
         * Displayed Signature or Usual Mark
         */
        val DG7: UByte = 7u

        /**
         * Data Features
         */
        val DG8: UByte = 8u

        /**
         * Structure Feature(s)
         */
        val DG9: UByte = 9u

        /**
         * Substance Feature(s)
         */
        val DG10: UByte = 10u

        /**
         * Additional Personal Detail(s)
         */
        val DG11: UByte = 11u

        /**
         * Additional Document Detail(s)
         */
        val DG12: UByte = 12u

        /**
         * Optional Detail(s)
         */
        val DG13: UByte = 13u

        /**
         * Security Options
         */
        val DG14: UByte = 14u

        /**
         * Active Authentication PKI
         */
        val DG15: UByte = 15u

        /**
         * Person(s) to Notify
         */
        val DG16: UByte = 16u
    }
}
