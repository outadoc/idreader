package fr.outadoc.eidas.crypto

interface CryptoEngine {
    fun generateKeyPair(algorithm: Algorithm): KeyPair

    fun generateKeyPairOnGenerator(algorithm: Algorithm, generator: EcPoint): KeyPair

    fun computeMappedGenerator(
        algorithm: Algorithm,
        mappingPrivateKey: PrivateKey,
        chipMappingPublicPoint: EcPoint,
        decryptedNonce: UByteArray,
    ): EcPoint

    fun computeSharedSecret(
        algorithm: Algorithm,
        privateKey: PrivateKey,
        chipPublicPoint: EcPoint,
    ): UByteArray

    fun computeCmac(
        algorithm: Algorithm,
        key: UByteArray,
        data: UByteArray,
    ): UByteArray

    fun deriveKeyFromSecret(
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
