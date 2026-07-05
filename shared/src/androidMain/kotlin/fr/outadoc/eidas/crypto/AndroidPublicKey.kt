package fr.outadoc.eidas.crypto

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
            *affineX
                .toByteArray()
                .toUByteArray(),
            *affineY
                .toByteArray()
                .toUByteArray(),
        )
}
