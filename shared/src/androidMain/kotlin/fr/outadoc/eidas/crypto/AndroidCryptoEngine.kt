@file:OptIn(ExperimentalUnsignedTypes::class)

package fr.outadoc.eidas.crypto

import fr.outadoc.eidas.utils.KmpBytes
import fr.outadoc.eidas.utils.toByteArrayBe
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.macs.CMac
import org.bouncycastle.crypto.modes.CBCBlockCipher
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import java.math.BigInteger
import java.security.MessageDigest

class AndroidCryptoEngine : CryptoEngine {
    override fun computeMappedGenerator(
        algorithm: Algorithm,
        mappingPrivateKey: PrivateKey,
        chipMappingPublicPoint: EcPoint,
        decryptedNonce: KmpBytes,
    ): EcPoint =
        when (algorithm.protocol) {
            Protocol.PACE_ECDH_GM_AES_CBC_CMAC_256 -> {
                computeMappedGeneratorEc(
                    algorithm = algorithm,
                    mappingPrivateKey = mappingPrivateKey,
                    chipMappingPublicPoint = chipMappingPublicPoint,
                    decryptedNonce = decryptedNonce,
                )
            }

            else -> {
                throw NotImplementedError()
            }
        }

    private fun computeMappedGeneratorEc(
        algorithm: Algorithm,
        mappingPrivateKey: PrivateKey,
        chipMappingPublicPoint: EcPoint,
        decryptedNonce: KmpBytes,
    ): EcPoint {
        val params = algorithm.parameter.ecParams()
        val d = (mappingPrivateKey as AndroidPrivateKey).scalar
        val chipPub =
            params.curve.createPoint(
                BigInteger(1, chipMappingPublicPoint.x.raw),
                BigInteger(1, chipMappingPublicPoint.y.raw),
            )

        val h = chipPub.multiply(d).normalize()
        val s = BigInteger(1, decryptedNonce.raw).mod(params.n)
        val gPrime = h.add(params.g.multiply(s)).normalize()

        return EcPoint(
            x = KmpBytes(gPrime.xCoord.encoded),
            y = KmpBytes(gPrime.yCoord.encoded),
        )
    }

    override fun computeSharedSecret(
        algorithm: Algorithm,
        privateKey: PrivateKey,
        chipPublicPoint: EcPoint,
    ): KmpBytes =
        when (algorithm.protocol) {
            Protocol.PACE_ECDH_GM_AES_CBC_CMAC_256 -> {
                computeSharedSecretEc(
                    algorithm = algorithm,
                    privateKey = privateKey,
                    chipPublicPoint = chipPublicPoint,
                )
            }

            else -> {
                throw NotImplementedError()
            }
        }

    private fun computeSharedSecretEc(
        algorithm: Algorithm,
        privateKey: PrivateKey,
        chipPublicPoint: EcPoint,
    ): KmpBytes {
        val params = algorithm.parameter.ecParams()
        val d = (privateKey as AndroidPrivateKey).scalar
        val chipPub =
            params.curve.createPoint(
                BigInteger(1, chipPublicPoint.x.raw),
                BigInteger(1, chipPublicPoint.y.raw),
            )

        val shared = chipPub.multiply(d).normalize()
        return KmpBytes(shared.xCoord.encoded)
    }

    override fun computeCmac(
        algorithm: Algorithm,
        key: KmpBytes,
        data: KmpBytes,
    ): KmpBytes =
        when (algorithm.protocol) {
            Protocol.PACE_ECDH_GM_AES_CBC_CMAC_256 -> {
                computeCmacAes(key, data)
            }

            else -> {
                throw NotImplementedError()
            }
        }

    private fun computeCmacAes(
        key: KmpBytes,
        data: KmpBytes,
    ): KmpBytes {
        val mac = CMac(AESEngine.newInstance())
        mac.init(KeyParameter(key.raw))
        mac.update(data.raw, 0, data.raw.size)
        val out = ByteArray(mac.macSize)
        mac.doFinal(out, 0)
        return KmpBytes(out)
    }

    override fun deriveKeyFromSecret(
        algorithm: Algorithm,
        secret: KmpBytes,
        nonce: KmpBytes,
        counter: Int,
    ): KmpBytes {
        val message: ByteArray = secret.raw + nonce.raw + counter.toByteArrayBe().toByteArray()

        return KmpBytes(
            MessageDigest
                .getInstance(algorithm.protocol.getHashFunctionName())
                .apply { update(message) }
                .digest(),
        )
    }

    override fun encryptSymmetric(
        algorithm: Algorithm,
        key: KmpBytes,
        iv: KmpBytes,
        data: KmpBytes,
    ): KmpBytes =
        when (algorithm.protocol) {
            Protocol.PACE_ECDH_GM_AES_CBC_CMAC_256 -> {
                KmpBytes(
                    aesCbc(
                        encrypt = true,
                        key = key.raw,
                        iv = iv.raw,
                        data = data.raw,
                    ),
                )
            }

            else -> {
                throw NotImplementedError()
            }
        }

    override fun decryptSymmetric(
        algorithm: Algorithm,
        key: KmpBytes,
        data: KmpBytes,
    ): KmpBytes =
        when (algorithm.protocol) {
            Protocol.PACE_ECDH_GM_AES_CBC_CMAC_256 -> {
                KmpBytes(
                    aesCbc(
                        encrypt = false,
                        key = key.raw,
                        iv = ByteArray(16),
                        data = data.raw,
                    ),
                )
            }

            else -> {
                throw NotImplementedError()
            }
        }

    override fun decryptSymmetricWithIv(
        algorithm: Algorithm,
        key: KmpBytes,
        iv: KmpBytes,
        data: KmpBytes,
    ): KmpBytes =
        when (algorithm.protocol) {
            Protocol.PACE_ECDH_GM_AES_CBC_CMAC_256 -> {
                KmpBytes(
                    aesCbc(
                        encrypt = false,
                        key = key.raw,
                        iv = iv.raw,
                        data = data.raw,
                    ),
                )
            }

            else -> {
                throw NotImplementedError()
            }
        }

    private fun aesCbc(
        encrypt: Boolean,
        key: ByteArray,
        iv: ByteArray,
        data: ByteArray,
    ): ByteArray {
        val cbc = CBCBlockCipher.newInstance(AESEngine.newInstance())
        cbc.init(encrypt, ParametersWithIV(KeyParameter(key), iv))
        val output = ByteArray(data.size)
        var offset = 0
        while (offset < data.size) {
            offset += cbc.processBlock(data, offset, output, offset)
        }
        return output
    }

    override fun computeSha1(message: KmpBytes): KmpBytes =
        KmpBytes(
            MessageDigest
                .getInstance("SHA-1")
                .apply { update(message.raw) }
                .digest(),
        )

    private fun Protocol.getHashFunctionName(): String =
        when (this) {
            Protocol.PACE_ECDH_GM_AES_CBC_CMAC_256 -> "SHA-256"
            else -> throw NotImplementedError()
        }
}
