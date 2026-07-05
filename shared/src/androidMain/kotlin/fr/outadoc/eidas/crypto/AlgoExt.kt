package fr.outadoc.eidas.crypto

import org.bouncycastle.asn1.x9.ECNamedCurveTable
import org.bouncycastle.asn1.x9.X9ECParameters

fun Algorithm.ecParams(): X9ECParameters = ECNamedCurveTable.getByName(getEcdhFunctionName())

fun Algorithm.getEcdhFunctionName(): String =
    when (this) {
        Algorithm.PACE_AES256_GM_ECDH_BRAINPOOLP256R1 -> "brainpoolP256r1"
    }

fun Algorithm.getHashFunctionName(): String =
    when (this) {
        Algorithm.PACE_AES256_GM_ECDH_BRAINPOOLP256R1 -> "SHA-256"
    }
