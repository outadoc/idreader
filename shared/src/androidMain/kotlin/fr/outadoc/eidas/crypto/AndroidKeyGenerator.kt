package fr.outadoc.eidas.crypto

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec

@OptIn(ExperimentalUnsignedTypes::class)
class AndroidKeyGenerator : KeyGenerator {
    override fun generateKeyPair(algorithm: Algorithm): KeyPair {
        val kp =
            KeyPairGenerator
                .getInstance("EC", BouncyCastleProvider())
                .apply {
                    initialize(
                        ECGenParameterSpec(
                            algorithm.parameter.getEcdhFunctionName(),
                        ),
                    )
                }.generateKeyPair()

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
        val params = algorithm.parameter.ecParams()
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

    private fun BigInteger.toUByteArrayStripped(): UByteArray {
        val bytes = toByteArray()
        return if (bytes.isNotEmpty() && bytes[0] == 0.toByte()) {
            bytes.copyOfRange(1, bytes.size).toUByteArray()
        } else {
            bytes.toUByteArray()
        }
    }
}
