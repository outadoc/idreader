package fr.outadoc.eidas.crypto

class AndroidPublicKey(
    private val publicKey: java.security.PublicKey,
) : PublicKey {
    override val encoded: UByteArray
        get() = publicKey.encoded.toUByteArray()
}
