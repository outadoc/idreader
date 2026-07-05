package fr.outadoc.eidas.crypto

class AndroidPrivateKey(
    private val privateKey: java.security.PrivateKey,
) : PrivateKey {
    override val encoded: UByteArray
        get() = privateKey.encoded.toUByteArray()
}
