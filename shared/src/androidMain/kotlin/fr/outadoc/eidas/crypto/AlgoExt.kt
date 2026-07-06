package fr.outadoc.eidas.crypto

import org.bouncycastle.asn1.x9.ECNamedCurveTable
import org.bouncycastle.asn1.x9.X9ECParameters

fun DomainParameter.ecParamsOrNull(): X9ECParameters? = ECNamedCurveTable.getByName(getEcdhFunctionName())

fun DomainParameter.ecParams(): X9ECParameters = ecParamsOrNull() ?: error("Parameter not available: $this")

fun DomainParameter.getEcdhFunctionName(): String =
    when (this) {
        DomainParameter.SECP192R1 -> "secp192r1"

        DomainParameter.SECP224R1 -> "secp224r1"

        DomainParameter.SECP256R1 -> "secp256r1"

        DomainParameter.SECP384R1 -> "secp384r1"

        DomainParameter.SECP521R1 -> "secp521r1"

        DomainParameter.BRAINPOOLP192R1 -> "brainpoolP192r1"

        DomainParameter.BRAINPOOLP224R1 -> "brainpoolP224r1"

        DomainParameter.BRAINPOOLP256R1 -> "brainpoolP256r1"

        DomainParameter.BRAINPOOLP320R1 -> "brainpoolP320r1"

        DomainParameter.BRAINPOOLP384R1 -> "brainpoolP384r1"

        DomainParameter.BRAINPOOLP512R1 -> "brainpoolP512r1"

        DomainParameter.GFP_1024_160,
        DomainParameter.GFP_2048_224,
        DomainParameter.GFP_2048_256,
        -> throw NotImplementedError()
    }
