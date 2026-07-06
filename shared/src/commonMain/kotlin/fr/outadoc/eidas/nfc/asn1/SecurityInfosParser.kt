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

@OptIn(ExperimentalUnsignedTypes::class)
class SecurityInfosParser {
    fun parse(bytes: UByteArray): List<SecurityInfo> {
        val set = Asn1Element.parseFirst(bytes.toByteArray()).first as Asn1Set
        return set.children.mapNotNull { element ->
            runCatching { parseSecurityInfo(element as Asn1Sequence) }
                .onFailure { e -> e.printStackTrace() }
                .getOrNull()
        }
    }

    private fun parseSecurityInfo(seq: Asn1Sequence): SecurityInfo {
        val children = seq.children.toList()
        val protocol = (children[0] as Asn1Primitive).readOid()
        val rest = children.drop(1)
        return when {
            protocol.hasPrefix(ID_PACE) -> {
                when (val next = rest[0]) {
                    is Asn1Primitive -> {
                        SecurityInfo.Pace(
                            protocol = protocol,
                            version = next.decodeToInt(),
                            parameterId =
                                rest
                                    .getOrNull(1)
                                    ?.let { (it as Asn1Primitive).decodeToInt() },
                        )
                    }

                    is Asn1Sequence -> {
                        SecurityInfo.PaceDomainParameter(
                            protocol = protocol,
                            domainParameter = next,
                            parameterId =
                                rest
                                    .getOrNull(1)
                                    ?.let { (it as Asn1Primitive).decodeToInt() },
                        )
                    }

                    else -> {
                        SecurityInfo.Unknown(protocol)
                    }
                }
            }

            protocol.hasPrefix(ID_CA) -> {
                when (val next = rest[0]) {
                    is Asn1Primitive -> {
                        SecurityInfo.ChipAuthentication(
                            protocol = protocol,
                            version = next.decodeToInt(),
                            keyId = rest.getOrNull(1)?.let { (it as Asn1Primitive).decodeToInt() },
                        )
                    }

                    is Asn1Sequence -> {
                        SecurityInfo.ChipAuthenticationDomainParameter(
                            protocol = protocol,
                            domainParameter = next,
                            keyId = rest.getOrNull(1)?.let { (it as Asn1Primitive).decodeToInt() },
                        )
                    }

                    else -> {
                        SecurityInfo.Unknown(protocol)
                    }
                }
            }

            protocol.hasPrefix(ID_PK) -> {
                SecurityInfo.ChipAuthenticationPublicKey(
                    protocol = protocol,
                    publicKey = rest[0] as Asn1Sequence,
                    keyId = rest.getOrNull(1)?.let { (it as Asn1Primitive).decodeToInt() },
                )
            }

            protocol.hasPrefix(ID_TA) -> {
                SecurityInfo.TerminalAuthentication(
                    protocol = protocol,
                    version = (rest[0] as Asn1Primitive).decodeToInt(),
                )
            }

            protocol == ID_CI -> {
                SecurityInfo.CardInfo(
                    protocol = protocol,
                    url = (rest[0] as Asn1Primitive).decodeToIa5String().value,
                )
            }

            else -> {
                SecurityInfo.Unknown(protocol)
            }
        }
    }

    private fun ObjectIdentifier.hasPrefix(prefix: ObjectIdentifier) =
        bytes.size >= prefix.bytes.size &&
            bytes.copyOfRange(0, prefix.bytes.size).contentEquals(prefix.bytes)

    @OptIn(ExperimentalUnsignedTypes::class)
    companion object {
        private val ID_PACE = ObjectIdentifier("0.4.0.127.0.7.2.2.4")
        private val ID_CA = ObjectIdentifier("0.4.0.127.0.7.2.2.3")
        private val ID_PK = ObjectIdentifier("0.4.0.127.0.7.2.2.1")
        private val ID_TA = ObjectIdentifier("0.4.0.127.0.7.2.2.2")
        private val ID_CI = ObjectIdentifier("0.4.0.127.0.7.2.2.6")
    }
}
