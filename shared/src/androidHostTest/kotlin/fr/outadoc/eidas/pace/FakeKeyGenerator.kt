package fr.outadoc.eidas.pace

import fr.outadoc.eidas.crypto.Algorithm
import fr.outadoc.eidas.crypto.AndroidPrivateKey
import fr.outadoc.eidas.crypto.AndroidPublicKey
import fr.outadoc.eidas.crypto.EcPoint
import fr.outadoc.eidas.crypto.KeyGenerator
import fr.outadoc.eidas.crypto.KeyPair
import fr.outadoc.eidas.crypto.ecParams
import fr.outadoc.eidas.utils.KmpBytes
import java.math.BigInteger

class FakeKeyGenerator(
    private val scalar: BigInteger,
) : KeyGenerator {
    override fun generateKeyPair(algorithm: Algorithm): KeyPair {
        val params = algorithm.parameter.ecParams()
        val baseG =
            EcPoint(
                x = KmpBytes(params.g.xCoord.encoded),
                y = KmpBytes(params.g.yCoord.encoded),
            )
        return generateKeyPairOnGenerator(algorithm, baseG)
    }

    override fun generateKeyPairOnGenerator(
        algorithm: Algorithm,
        generator: EcPoint,
    ): KeyPair {
        val params = algorithm.parameter.ecParams()
        val g =
            params.curve.createPoint(
                BigInteger(1, generator.x.raw),
                BigInteger(1, generator.y.raw),
            )
        val pub = g.multiply(scalar).normalize()
        return KeyPair(
            privateKey = AndroidPrivateKey(scalar),
            publicKey =
                AndroidPublicKey(
                    EcPoint(
                        x = KmpBytes(pub.xCoord.encoded),
                        y = KmpBytes(pub.yCoord.encoded),
                    ),
                ),
        )
    }
}
