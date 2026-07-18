package fr.outadoc.eidas.crypto

import fr.outadoc.eidas.utils.KmpBytes

interface CryptoEngine {
    fun computeMappedGenerator(
        algorithm: Algorithm,
        mappingPrivateKey: PrivateKey,
        chipMappingPublicPoint: EcPoint,
        decryptedNonce: KmpBytes,
    ): EcPoint

    fun computeSharedSecret(
        algorithm: Algorithm,
        privateKey: PrivateKey,
        chipPublicPoint: EcPoint,
    ): KmpBytes

    fun computeCmac(
        algorithm: Algorithm,
        key: KmpBytes,
        data: KmpBytes,
    ): KmpBytes

    fun deriveKeyFromSecret(
        algorithm: Algorithm,
        secret: KmpBytes,
        nonce: KmpBytes,
        counter: Int,
    ): KmpBytes

    fun encryptSymmetric(
        algorithm: Algorithm,
        key: KmpBytes,
        iv: KmpBytes,
        data: KmpBytes,
    ): KmpBytes

    fun decryptSymmetric(
        algorithm: Algorithm,
        key: KmpBytes,
        data: KmpBytes,
    ): KmpBytes

    fun decryptSymmetricWithIv(
        algorithm: Algorithm,
        key: KmpBytes,
        iv: KmpBytes,
        data: KmpBytes,
    ): KmpBytes

    fun computeSha1(message: KmpBytes): KmpBytes
}
