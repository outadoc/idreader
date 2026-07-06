package fr.outadoc.eidas.crypto

import at.asitplus.signum.indispensable.asn1.ObjectIdentifier

enum class Algorithm(
    val oid: ObjectIdentifier,
    val parameterId: Int,
) {
    PACE_AES256_GM_ECDH_BRAINPOOLP256R1(
        oid = ObjectIdentifier("0.4.0.127.0.7.2.2.4.2.4"),
        parameterId = 13,
    ),
    ;

    companion object {
        val preferredAlgorithms: List<Algorithm> =
            listOf(
                PACE_AES256_GM_ECDH_BRAINPOOLP256R1,
            )
    }
}
