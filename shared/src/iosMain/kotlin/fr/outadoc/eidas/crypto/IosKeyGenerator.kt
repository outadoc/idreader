package fr.outadoc.eidas.crypto

class IosKeyGenerator : KeyGenerator {
    override fun generateKeyPair(algorithm: Algorithm): KeyPair {
        TODO("Not yet implemented")
    }

    override fun generateKeyPairOnGenerator(
        algorithm: Algorithm,
        generator: EcPoint,
    ): KeyPair {
        TODO("Not yet implemented")
    }
}
