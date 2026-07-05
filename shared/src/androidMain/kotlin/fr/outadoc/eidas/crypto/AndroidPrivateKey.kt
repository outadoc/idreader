package fr.outadoc.eidas.crypto

import java.math.BigInteger

@OptIn(ExperimentalUnsignedTypes::class)
class AndroidPrivateKey(internal val scalar: BigInteger) : PrivateKey {
    override val encoded: UByteArray
        get() = scalar.toByteArray().toUByteArray()
}
