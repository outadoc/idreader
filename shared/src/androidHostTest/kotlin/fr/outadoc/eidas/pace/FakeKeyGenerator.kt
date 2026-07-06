package fr.outadoc.eidas.pace

import fr.outadoc.eidas.crypto.Algorithm
import fr.outadoc.eidas.crypto.AndroidPrivateKey
import fr.outadoc.eidas.crypto.AndroidPublicKey
import fr.outadoc.eidas.crypto.EcPoint
import fr.outadoc.eidas.crypto.KeyGenerator
import fr.outadoc.eidas.crypto.KeyPair
import fr.outadoc.eidas.crypto.ecParams
import java.math.BigInteger

@OptIn(ExperimentalUnsignedTypes::class)
class FakeKeyGenerator(
    private val scalar: BigInteger,
) : KeyGenerator {
    override fun generateKeyPair(algorithm: Algorithm): KeyPair {
        val params = algorithm.parameter.ecParams()
        val baseG =
            EcPoint(
                x =
                    params.g.xCoord.encoded
                        .toUByteArray(),
                y =
                    params.g.yCoord.encoded
                        .toUByteArray(),
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
                BigInteger(1, generator.x.toByteArray()),
                BigInteger(1, generator.y.toByteArray()),
            )
        val pub = g.multiply(scalar).normalize()
        return KeyPair(
            privateKey = AndroidPrivateKey(scalar),
            publicKey =
                AndroidPublicKey(
                    EcPoint(
                        x = pub.xCoord.encoded.toUByteArray(),
                        y = pub.yCoord.encoded.toUByteArray(),
                    ),
                ),
        )
    }
}
