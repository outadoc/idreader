@file:OptIn(ExperimentalUnsignedTypes::class)

package fr.outadoc.eidas.crypto

import org.bouncycastle.asn1.x9.ECNamedCurveTable
import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.macs.CMac
import org.bouncycastle.crypto.modes.CBCBlockCipher
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec

class AndroidCryptoEngine : CryptoEngine {
    override fun generateKeyPair(algorithm: Algorithm): KeyPair {
        val kp =
            KeyPairGenerator
                .getInstance("EC", BouncyCastleProvider())
                .apply { initialize(ECGenParameterSpec(algorithm.getEcdhFunctionName())) }
                .generateKeyPair()

        val ecPub = kp.public as ECPublicKey
        return KeyPair(
            privateKey = AndroidPrivateKey((kp.private as ECPrivateKey).s),
            publicKey =
                AndroidPublicKey(
                    EcPoint(
                        x = ecPub.w.affineX.toUByteArrayStripped(),
                        y = ecPub.w.affineY.toUByteArrayStripped(),
                    ),
                ),
        )
    }

    override fun generateKeyPairOnGenerator(
        algorithm: Algorithm,
        generator: EcPoint,
    ): KeyPair {
        val params = algorithm.ecParams()
        val d =
            BigInteger(params.n.bitLength(), SecureRandom())
                .mod(params.n - BigInteger.ONE) + BigInteger.ONE

        val gPrime =
            params.curve.createPoint(
                BigInteger(1, generator.x.toByteArray()),
                BigInteger(1, generator.y.toByteArray()),
            )

        val pub = gPrime.multiply(d).normalize()

        return KeyPair(
            privateKey = AndroidPrivateKey(d),
            publicKey =
                AndroidPublicKey(
                    EcPoint(
                        x = pub.xCoord.encoded.toUByteArray(),
                        y = pub.yCoord.encoded.toUByteArray(),
                    ),
                ),
        )
    }

    override fun computeMappedGenerator(
        algorithm: Algorithm,
        mappingPrivateKey: PrivateKey,
        chipMappingPublicPoint: EcPoint,
        decryptedNonce: UByteArray,
    ): EcPoint {
        val params = algorithm.ecParams()
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
    ): UByteArray {
        val params = algorithm.ecParams()
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
        val message = ubyteArrayOf(*secret, *nonce, *counter.toByteArrayBe())
        return MessageDigest
            .getInstance(algorithm.getHashFunctionName())
            .apply { update(message.toByteArray()) }
            .digest()
            .toUByteArray()
    }

    override fun decryptSymmetric(
        algorithm: Algorithm,
        key: UByteArray,
        data: UByteArray,
    ): UByteArray =
        when (algorithm) {
            Algorithm.PACE_AES256_GM_ECDH_BRAINPOOLP256R1 -> {
                decryptAesCbc(key.toByteArray(), data.toByteArray()).toUByteArray()
            }
        }

    private fun decryptAesCbc(
        key: ByteArray,
        data: ByteArray,
    ): ByteArray {
        val cbc = CBCBlockCipher.newInstance(AESEngine.newInstance())
        cbc.init(
            false,
            ParametersWithIV(
                KeyParameter(key),
                ByteArray(cbc.blockSize),
            ),
        )
        val output = ByteArray(data.size)
        var offset = 0
        while (offset < data.size) {
            offset += cbc.processBlock(data, offset, output, offset)
        }
        return output
    }

    private fun Int.toByteArrayBe(): UByteArray {
        val bb = ByteBuffer.allocate(4)
        bb.order(ByteOrder.BIG_ENDIAN)
        bb.putInt(this)
        return bb.array().toUByteArray()
    }

    private fun BigInteger.toUByteArrayStripped(): UByteArray {
        val bytes = toByteArray()
        return if (bytes.isNotEmpty() && bytes[0] == 0.toByte()) {
            bytes.copyOfRange(1, bytes.size).toUByteArray()
        } else {
            bytes.toUByteArray()
        }
    }

    private fun Algorithm.ecParams(): X9ECParameters = ECNamedCurveTable.getByName(getEcdhFunctionName())

    private fun Algorithm.getEcdhFunctionName(): String =
        when (this) {
            Algorithm.PACE_AES256_GM_ECDH_BRAINPOOLP256R1 -> "brainpoolP256r1"
        }

    private fun Algorithm.getHashFunctionName(): String =
        when (this) {
            Algorithm.PACE_AES256_GM_ECDH_BRAINPOOLP256R1 -> "SHA-256"
        }
}
