package fr.outadoc.eidas.crypto

import org.bouncycastle.util.Arrays
import java.math.BigInteger
import java.security.interfaces.ECPublicKey

@OptIn(ExperimentalUnsignedTypes::class)
class AndroidPublicKey(
    private val publicKey: java.security.PublicKey,
) : PublicKey {
    override val encoded: UByteArray
        get() = publicKey.encoded.toUByteArray()

    override val uncompressedPublicPoint: UByteArray
        get() {
            val pk = publicKey as ECPublicKey
            val pk2 =
                EcPoint(
                    x = pk.w.affineX.toUByteArray(),
                    y = pk.w.affineY.toUByteArray(),
                )

            return pk2.serializeUncompressed()
        }

    private fun BigInteger.toUByteArray(): UByteArray {
        val array = toByteArray()
        return if (array.first() == 0.toByte()) {
            Arrays.copyOfRange(array, 1, array.size).toUByteArray()
        } else {
            array.toUByteArray()
        }
    }
}
