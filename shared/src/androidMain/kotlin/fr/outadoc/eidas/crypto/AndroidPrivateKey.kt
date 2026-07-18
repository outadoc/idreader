package fr.outadoc.eidas.crypto

import fr.outadoc.eidas.utils.KmpBytes
import java.math.BigInteger

class AndroidPrivateKey(
    internal val scalar: BigInteger,
) : PrivateKey {
    override val encoded: KmpBytes
        get() = KmpBytes(scalar.toByteArray())
}
