package fr.outadoc.eidas.crypto

import at.asitplus.signum.indispensable.asn1.ObjectIdentifier

enum class Protocol(
    val oid: ObjectIdentifier,
) {
    /**
     * id-PACE-DH-GM-3DES-CBC-CBC
     */
    PACE_DH_GM_3DES_CBC_CBC(ObjectIdentifier("0.4.0.127.0.7.2.2.4.1.1")),

    /**
     * id-PACE-DH-GM-AES-CBC-CMAC-256
     */
    PACE_DH_GM_AES_CBC_CMAC_256(ObjectIdentifier("0.4.0.127.0.7.2.2.4.1.4")),

    /**
     * id-PACE-ECDH-GM-3DES-CBC-CBC
     */
    PACE_ECDH_GM_3DES_CBC_CBC(ObjectIdentifier("0.4.0.127.0.7.2.2.4.2.1")),

    /**
     * id-PACE-ECDH-GM-AES-CBC-CMAC-256
     */
    PACE_ECDH_GM_AES_CBC_CMAC_256(ObjectIdentifier("0.4.0.127.0.7.2.2.4.2.4")),

    /**
     * id-PACE-DH-IM-3DES-CBC-CBC
     */
    PACE_DH_IM_3DES_CBC_CBC(ObjectIdentifier("0.4.0.127.0.7.2.2.4.3.1")),

    /**
     * id-PACE-DH-IM-AES-CBC-CMAC-256
     */
    PACE_DH_IM_AES_CBC_CMAC_256(ObjectIdentifier("0.4.0.127.0.7.2.2.4.3.4")),

    /**
     * id-PACE-ECDH-IM-3DES-CBC-CBC
     */
    PACE_ECDH_IM_3DES_CBC_CBC(ObjectIdentifier("0.4.0.127.0.7.2.2.4.4.1")),

    /**
     * id-PACE-ECDH-IM-AES-CBC-CMAC-256
     */
    PACE_ECDH_IM_AES_CBC_CMAC_256(ObjectIdentifier("0.4.0.127.0.7.2.2.4.4.4")),

    /**
     * id-PACE-ECDH-CAM-AES-CBC-CMAC-256
     */
    PACE_ECDH_CAM_AES_CBC_CMAC_256(ObjectIdentifier("0.4.0.127.0.7.2.2.4.6.4")),
    ;

    companion object {
        fun fromOid(oid: ObjectIdentifier): Protocol? =
            entries.firstOrNull { entry ->
                entry.oid == oid
            }
    }
}
