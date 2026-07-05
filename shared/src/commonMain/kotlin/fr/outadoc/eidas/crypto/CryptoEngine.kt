package fr.outadoc.eidas.crypto

interface CryptoEngine {
    fun generateKeyPair(algorithm: Algorithm): KeyPair

    fun kdf(
        algorithm: Algorithm,
        secret: UByteArray,
        nonce: UByteArray,
        counter: Int,
    ): UByteArray

    fun decryptSymmetric(
        algorithm: Algorithm,
        key: UByteArray,
        data: UByteArray,
    ): UByteArray
}
