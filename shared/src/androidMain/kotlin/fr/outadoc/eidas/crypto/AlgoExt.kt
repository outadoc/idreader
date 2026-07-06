package fr.outadoc.eidas.crypto

import org.bouncycastle.asn1.x9.ECNamedCurveTable
import org.bouncycastle.asn1.x9.X9ECParameters

fun Algorithm.ecParams(): X9ECParameters = ECNamedCurveTable.getByName(getEcdhFunctionName())

fun Algorithm.getEcdhFunctionName(): String =
    when (parameter) {
        DomainParameter.BRAINPOOLP256R1 -> "brainpoolP256r1"
    }

fun Algorithm.getHashFunctionName(): String =
    when (protocol) {
        Protocol.PACE_ECDH_GM_AES_CBC_CMAC_256 -> "SHA-256"
    }
