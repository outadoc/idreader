package fr.outadoc.eidas.crypto

class IosCryptoEngine : CryptoEngine {
    override fun computeMappedGenerator(
        algorithm: Algorithm,
        mappingPrivateKey: PrivateKey,
        chipMappingPublicPoint: EcPoint,
        decryptedNonce: UByteArray,
    ): EcPoint {
        TODO("Not yet implemented")
    }

    override fun computeSharedSecret(
        algorithm: Algorithm,
        privateKey: PrivateKey,
        chipPublicPoint: EcPoint,
    ): UByteArray {
        TODO("Not yet implemented")
    }

    override fun computeCmac(
        algorithm: Algorithm,
        key: UByteArray,
        data: UByteArray,
    ): UByteArray {
        TODO("Not yet implemented")
    }

    override fun deriveKeyFromSecret(
        algorithm: Algorithm,
        secret: UByteArray,
        nonce: UByteArray,
        counter: Int,
    ): UByteArray {
        TODO("Not yet implemented")
    }

    override fun encryptSymmetric(
        algorithm: Algorithm,
        key: UByteArray,
        iv: UByteArray,
        data: UByteArray,
    ): UByteArray {
        TODO("Not yet implemented")
    }

    override fun decryptSymmetric(
        algorithm: Algorithm,
        key: UByteArray,
        data: UByteArray,
    ): UByteArray {
        TODO("Not yet implemented")
    }

    override fun decryptSymmetricWithIv(
        algorithm: Algorithm,
        key: UByteArray,
        iv: UByteArray,
        data: UByteArray,
    ): UByteArray {
        TODO("Not yet implemented")
    }

    override fun computeSha1(message: UByteArray): UByteArray {
        TODO("Not yet implemented")
    }
}
