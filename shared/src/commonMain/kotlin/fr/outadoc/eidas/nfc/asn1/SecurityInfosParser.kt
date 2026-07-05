package fr.outadoc.eidas.nfc.asn1

import at.asitplus.signum.indispensable.asn1.Asn1Element
import at.asitplus.signum.indispensable.asn1.Asn1Primitive
import at.asitplus.signum.indispensable.asn1.Asn1Sequence
import at.asitplus.signum.indispensable.asn1.Asn1Set
import at.asitplus.signum.indispensable.asn1.ObjectIdentifier
import at.asitplus.signum.indispensable.asn1.encoding.decodeToIa5String
import at.asitplus.signum.indispensable.asn1.encoding.decodeToInt
import at.asitplus.signum.indispensable.asn1.encoding.parseFirst
import at.asitplus.signum.indispensable.asn1.readOid

class SecurityInfosParser {

    fun parse(bytes: ByteArray): List<SecurityInfo> {
        val set = Asn1Element.parseFirst(bytes).first as Asn1Set
        return set.children.mapNotNull { element ->
            runCatching { parseSecurityInfo(element as Asn1Sequence) }.getOrNull()
        }
    }

    private fun parseSecurityInfo(seq: Asn1Sequence): SecurityInfo {
        val children = seq.children.toList()
        val protocol = (children[0] as Asn1Primitive).readOid()
        val rest = children.drop(1)
        return when {
            protocol.hasPrefix(ID_PACE) -> when (val next = rest[0]) {
                is Asn1Primitive -> SecurityInfo.Pace(
                    protocol,
                    next.decodeToInt(),
                    rest.getOrNull(1)?.let { (it as Asn1Primitive).decodeToInt() },
                )
                is Asn1Sequence -> SecurityInfo.PaceDomainParameter(
                    protocol,
                    next,
                    rest.getOrNull(1)?.let { (it as Asn1Primitive).decodeToInt() },
                )
                else -> SecurityInfo.Unknown(protocol)
            }
            protocol.hasPrefix(ID_CA) -> when (val next = rest[0]) {
                is Asn1Primitive -> SecurityInfo.ChipAuthentication(
                    protocol,
                    next.decodeToInt(),
                    rest.getOrNull(1)?.let { (it as Asn1Primitive).decodeToInt() },
                )
                is Asn1Sequence -> SecurityInfo.ChipAuthenticationDomainParameter(
                    protocol,
                    next,
                    rest.getOrNull(1)?.let { (it as Asn1Primitive).decodeToInt() },
                )
                else -> SecurityInfo.Unknown(protocol)
            }
            protocol.hasPrefix(ID_PK) -> SecurityInfo.ChipAuthenticationPublicKey(
                protocol,
                rest[0] as Asn1Sequence,
                rest.getOrNull(1)?.let { (it as Asn1Primitive).decodeToInt() },
            )
            protocol.hasPrefix(ID_TA) -> SecurityInfo.TerminalAuthentication(
                protocol,
                (rest[0] as Asn1Primitive).decodeToInt(),
            )
            protocol == ID_CI -> SecurityInfo.CardInfo(
                protocol,
                (rest[0] as Asn1Primitive).decodeToIa5String().value,
            )
            else -> SecurityInfo.Unknown(protocol)
        }
    }

    private fun ObjectIdentifier.hasPrefix(prefix: ObjectIdentifier) =
        bytes.size >= prefix.bytes.size &&
            bytes.copyOfRange(0, prefix.bytes.size).contentEquals(prefix.bytes)

    @OptIn(ExperimentalUnsignedTypes::class)
    companion object {
        // bsi-de = 0.4.0.127.0.7 (itu-t(0) identified-organization(4) etsi(0) reserved(127) etsi-identified-organization(0) 7)
        private val ID_PACE = ObjectIdentifier(0u, 4u, 0u, 127u, 0u, 7u, 2u, 2u, 4u)
        private val ID_CA   = ObjectIdentifier(0u, 4u, 0u, 127u, 0u, 7u, 2u, 2u, 3u)
        private val ID_PK   = ObjectIdentifier(0u, 4u, 0u, 127u, 0u, 7u, 2u, 2u, 1u)
        private val ID_TA   = ObjectIdentifier(0u, 4u, 0u, 127u, 0u, 7u, 2u, 2u, 2u)
        private val ID_CI   = ObjectIdentifier(0u, 4u, 0u, 127u, 0u, 7u, 2u, 2u, 6u)
    }
}
