package fr.outadoc.eidas.pace

import fr.outadoc.eidas.crypto.Algorithm
import fr.outadoc.eidas.crypto.AndroidCryptoEngine
import fr.outadoc.eidas.crypto.AndroidPrivateKey
import fr.outadoc.eidas.crypto.DomainParameter
import fr.outadoc.eidas.crypto.EcPoint
import fr.outadoc.eidas.crypto.Protocol
import fr.outadoc.eidas.crypto.ecParams
import fr.outadoc.eidas.logging.MemoryLogger
import fr.outadoc.eidas.nfc.commands.CommandFactory
import kotlinx.coroutines.test.runTest
import java.math.BigInteger
import kotlin.test.Test
import kotlin.test.assertContentEquals

@OptIn(ExperimentalUnsignedTypes::class)
class PaceKeyAgreementUseCaseTest {
    private val algorithm =
        Algorithm(
            protocol = Protocol.PACE_ECDH_GM_AES_CBC_CMAC_256,
            parameter = DomainParameter.BRAINPOOLP256R1,
        )

    private val cryptoEngine = AndroidCryptoEngine()

    private val testScalar = BigInteger("31337")

    // Mapped generator G' from real card run
    private val mappedGenerator =
        EcPoint(
            x = "0F0D69FDE03A7A7DCEDD84190CCB6E466A97BFD20CA79B2B556C5A22487FFD9A".hexToUByteArray(),
            y = "7E7B907FDD4ADAD3F57B70E3A34631EFEC15A984BD5B4589EC73C57E144BBCC0".hexToUByteArray(),
        )

    // Chip final public key from real card run
    private val chipFinalPub =
        "041AAF77535B2CD3D5D87E2A8728823C508895500143173A19990F50EDA5B3C2D10BBB64BB26EE00E4916C362FC156DF8ADCB14386AA2CEAE88B450E6A919970AB"
            .hexToUByteArray()

    @Test
    fun derivesCorrectSessionKeysFromChipPublicKey() =
        runTest {
            val step3Response =
                ubyteArrayOf(0x7Cu, 0x43u, 0x84u, 0x41u) +
                    chipFinalPub +
                    ubyteArrayOf(0x90u, 0x00u)

            val useCase =
                PaceKeyAgreementUseCase(
                    commandFactory = CommandFactory(),
                    cryptoEngine = cryptoEngine,
                    keyGenerator = FakeKeyGenerator(testScalar),
                    logger = MemoryLogger(),
                )

            val nfcSession = StubNfcSession(step3Response.toByteArray())

            val result = useCase(nfcSession, algorithm, mappedGenerator).getOrThrow()

            // Independently compute expected session keys using the same test scalar and chip pub
            val params = algorithm.parameter.ecParams()
            val chipPoint =
                params.curve.createPoint(
                    BigInteger(1, chipFinalPub.sliceArray(1..32).toByteArray()),
                    BigInteger(1, chipFinalPub.sliceArray(33..64).toByteArray()),
                )
            val sharedPoint = chipPoint.multiply(testScalar).normalize()
            val sharedSecret = sharedPoint.xCoord.encoded.toUByteArray()

            val expectedKEnc =
                cryptoEngine.deriveKeyFromSecret(algorithm, sharedSecret, ubyteArrayOf(), 1)
            val expectedKMac =
                cryptoEngine.deriveKeyFromSecret(algorithm, sharedSecret, ubyteArrayOf(), 2)

            assertContentEquals(expectedKEnc, result.kEnc)
            assertContentEquals(expectedKMac, result.kMac)
            assertContentEquals(chipFinalPub, result.chipFinalPub)
        }

    // Verifies that computeSharedSecret is commutative:
    // ECDH(terminal_priv, chip_pub) == ECDH(chip_priv, terminal_pub)
    @Test
    fun sharedSecretIsCommutative() {
        val params = algorithm.parameter.ecParams()
        val scalarA = BigInteger("99999")
        val scalarB = BigInteger("12345")

        val gPrime =
            params.curve.createPoint(
                BigInteger(1, mappedGenerator.x.toByteArray()),
                BigInteger(1, mappedGenerator.y.toByteArray()),
            )
        val pubA = gPrime.multiply(scalarA).normalize()
        val pubB = gPrime.multiply(scalarB).normalize()

        val pubAPoint =
            EcPoint(
                x = pubA.xCoord.encoded.toUByteArray(),
                y = pubA.yCoord.encoded.toUByteArray(),
            )
        val pubBPoint =
            EcPoint(
                x = pubB.xCoord.encoded.toUByteArray(),
                y = pubB.yCoord.encoded.toUByteArray(),
            )

        val secretAB =
            cryptoEngine.computeSharedSecret(algorithm, AndroidPrivateKey(scalarA), pubBPoint)
        val secretBA =
            cryptoEngine.computeSharedSecret(algorithm, AndroidPrivateKey(scalarB), pubAPoint)

        assertContentEquals(secretAB, secretBA)
    }
}

private fun UByteArray.sliceArray(range: IntRange): UByteArray = toByteArray().sliceArray(range).toUByteArray()
