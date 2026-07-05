package fr.outadoc.eidas.nfc.asn1

import at.asitplus.signum.indispensable.asn1.Asn1Sequence
import at.asitplus.signum.indispensable.asn1.ObjectIdentifier

sealed class SecurityInfo {
    abstract val protocol: ObjectIdentifier

    data class Pace(
        override val protocol: ObjectIdentifier,
        val version: Int,
        val parameterId: Int?,
    ) : SecurityInfo()

    data class PaceDomainParameter(
        override val protocol: ObjectIdentifier,
        val domainParameter: Asn1Sequence,
        val parameterId: Int?,
    ) : SecurityInfo()

    data class ChipAuthentication(
        override val protocol: ObjectIdentifier,
        val version: Int,
        val keyId: Int?,
    ) : SecurityInfo()

    data class ChipAuthenticationDomainParameter(
        override val protocol: ObjectIdentifier,
        val domainParameter: Asn1Sequence,
        val keyId: Int?,
    ) : SecurityInfo()

    data class ChipAuthenticationPublicKey(
        override val protocol: ObjectIdentifier,
        val publicKey: Asn1Sequence,
        val keyId: Int?,
    ) : SecurityInfo()

    data class TerminalAuthentication(
        override val protocol: ObjectIdentifier,
        val version: Int,
    ) : SecurityInfo()

    data class CardInfo(
        override val protocol: ObjectIdentifier,
        val url: String,
    ) : SecurityInfo()

    data class Unknown(
        override val protocol: ObjectIdentifier,
    ) : SecurityInfo()
}
