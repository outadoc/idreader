package fr.outadoc.eidas.crypto

import at.asitplus.signum.indispensable.asn1.ObjectIdentifier

enum class Protocol(
    val oid: ObjectIdentifier,
) {
    PACE_ECDH_GM_AES_CBC_CMAC_256(ObjectIdentifier("0.4.0.127.0.7.2.2.4.2.4")),
    ;

    companion object {
        fun fromOid(oid: ObjectIdentifier): Protocol? =
            entries.firstOrNull { entry ->
                entry.oid == oid
            }
    }
}
