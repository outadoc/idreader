package fr.outadoc.eidas.crypto

import org.bouncycastle.util.Arrays
import java.math.BigInteger
import java.security.interfaces.ECPublicKey
import java.security.spec.ECPoint

class AndroidPublicKey(
    private val publicKey: java.security.PublicKey,
) : PublicKey {
    override val encoded: UByteArray
        get() = publicKey.encoded.toUByteArray()

    override val uncompressedPublicPoint: UByteArray
        get() {
            val pk = publicKey as ECPublicKey
            return pk.w.serializeUncompressed()
        }

    private fun ECPoint.serializeUncompressed(): UByteArray =
        ubyteArrayOf(
            0x04u,
            *affineX.toUByteArray(),
            *affineY.toUByteArray(),
        )

    private fun BigInteger.toUByteArray(): UByteArray {
        val array = toByteArray()
        return if (array.first() == 0.toByte()) {
            Arrays.copyOfRange(array, 1, array.size).toUByteArray()
        } else {
            array.toUByteArray()
        }
    }
}
