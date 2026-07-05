package fr.outadoc.eidas.crypto

interface KeyGenerator {
    fun generateKeyPair(algorithm: Algorithm): KeyPair

    fun generateKeyPairOnGenerator(
        algorithm: Algorithm,
        generator: EcPoint,
    ): KeyPair
}
