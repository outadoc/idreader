@file:OptIn(ExperimentalUnsignedTypes::class)

package fr.outadoc.eidas.crypto

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
        decryptedNonce: UByteArray,
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
        decryptedNonce: UByteArray,
    ): EcPoint {
        val params = algorithm.parameter.ecParams()
        val d = (mappingPrivateKey as AndroidPrivateKey).scalar
        val chipPub =
            params.curve.createPoint(
                BigInteger(1, chipMappingPublicPoint.x.toByteArray()),
                BigInteger(1, chipMappingPublicPoint.y.toByteArray()),
            )

        val h = chipPub.multiply(d).normalize()
        val s = BigInteger(1, decryptedNonce.toByteArray()).mod(params.n)
        val gPrime = h.add(params.g.multiply(s)).normalize()

        return EcPoint(
            x = gPrime.xCoord.encoded.toUByteArray(),
            y = gPrime.yCoord.encoded.toUByteArray(),
        )
    }

    override fun computeSharedSecret(
        algorithm: Algorithm,
        privateKey: PrivateKey,
        chipPublicPoint: EcPoint,
    ): UByteArray =
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
    ): UByteArray {
        val params = algorithm.parameter.ecParams()
        val d = (privateKey as AndroidPrivateKey).scalar
        val chipPub =
            params.curve.createPoint(
                BigInteger(1, chipPublicPoint.x.toByteArray()),
                BigInteger(1, chipPublicPoint.y.toByteArray()),
            )

        val shared = chipPub.multiply(d).normalize()
        return shared.xCoord.encoded.toUByteArray()
    }

    override fun computeCmac(
        algorithm: Algorithm,
        key: UByteArray,
        data: UByteArray,
    ): UByteArray =
        when (algorithm.protocol) {
            Protocol.PACE_ECDH_GM_AES_CBC_CMAC_256 -> {
                computeCmacAes(key, data)
            }

            else -> {
                throw NotImplementedError()
            }
        }

    private fun computeCmacAes(
        key: UByteArray,
        data: UByteArray,
    ): UByteArray {
        val mac = CMac(AESEngine.newInstance())
        mac.init(KeyParameter(key.toByteArray()))
        mac.update(data.toByteArray(), 0, data.size)
        val out = ByteArray(mac.macSize)
        mac.doFinal(out, 0)
        return out.toUByteArray()
    }

    override fun deriveKeyFromSecret(
        algorithm: Algorithm,
        secret: UByteArray,
        nonce: UByteArray,
        counter: Int,
    ): UByteArray {
        val message =
            ubyteArrayOf(
                *secret,
                *nonce,
                *counter.toByteArrayBe(),
            )

        return MessageDigest
            .getInstance(algorithm.protocol.getHashFunctionName())
            .apply { update(message.toByteArray()) }
            .digest()
            .toUByteArray()
    }

    override fun encryptSymmetric(
        algorithm: Algorithm,
        key: UByteArray,
        iv: UByteArray,
        data: UByteArray,
    ): UByteArray =
        when (algorithm.protocol) {
            Protocol.PACE_ECDH_GM_AES_CBC_CMAC_256 -> {
                aesCbc(
                    encrypt = true,
                    key = key.toByteArray(),
                    iv = iv.toByteArray(),
                    data = data.toByteArray(),
                ).toUByteArray()
            }

            else -> {
                throw NotImplementedError()
            }
        }

    override fun decryptSymmetric(
        algorithm: Algorithm,
        key: UByteArray,
        data: UByteArray,
    ): UByteArray =
        when (algorithm.protocol) {
            Protocol.PACE_ECDH_GM_AES_CBC_CMAC_256 -> {
                aesCbc(
                    encrypt = false,
                    key = key.toByteArray(),
                    iv = ByteArray(16),
                    data = data.toByteArray(),
                ).toUByteArray()
            }

            else -> {
                throw NotImplementedError()
            }
        }

    override fun decryptSymmetricWithIv(
        algorithm: Algorithm,
        key: UByteArray,
        iv: UByteArray,
        data: UByteArray,
    ): UByteArray =
        when (algorithm.protocol) {
            Protocol.PACE_ECDH_GM_AES_CBC_CMAC_256 -> {
                aesCbc(
                    encrypt = false,
                    key = key.toByteArray(),
                    iv = iv.toByteArray(),
                    data = data.toByteArray(),
                ).toUByteArray()
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

    override fun computeSha1(message: UByteArray): UByteArray =
        MessageDigest
            .getInstance("SHA-1")
            .apply { update(message.toByteArray()) }
            .digest()
            .toUByteArray()

    private fun Protocol.getHashFunctionName(): String =
        when (this) {
            Protocol.PACE_ECDH_GM_AES_CBC_CMAC_256 -> "SHA-256"
            else -> throw NotImplementedError()
        }
}
