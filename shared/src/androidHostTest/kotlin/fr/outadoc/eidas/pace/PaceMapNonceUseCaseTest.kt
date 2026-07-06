package fr.outadoc.eidas.pace

import fr.outadoc.eidas.crypto.Algorithm
import fr.outadoc.eidas.crypto.AndroidCryptoEngine
import fr.outadoc.eidas.crypto.DomainParameter
import fr.outadoc.eidas.crypto.EcPoint
import fr.outadoc.eidas.crypto.Protocol
import fr.outadoc.eidas.crypto.deserializedUncompressedEcPoint
import fr.outadoc.eidas.crypto.ecParams
import fr.outadoc.eidas.logging.MemoryLogger
import fr.outadoc.eidas.nfc.NfcTag
import fr.outadoc.eidas.nfc.commands.CommandFactory
import kotlinx.coroutines.test.runTest
import java.math.BigInteger
import kotlin.test.Test
import kotlin.test.assertContentEquals

@OptIn(ExperimentalUnsignedTypes::class)
class PaceMapNonceUseCaseTest {
    private val algorithm =
        Algorithm(
            protocol = Protocol.PACE_ECDH_GM_AES_CBC_CMAC_256,
            parameter = DomainParameter.BRAINPOOLP256R1,
        )

    private val cryptoEngine = AndroidCryptoEngine()
    private val tag = NfcTag(id = byteArrayOf(), description = "stub")

    private val testScalar = BigInteger("31337")

    // From real card run
    private val chipMappingPub =
        "046208ED0926CAD959A0118D2A9AE3DAFEAEF1DD6FF72203F24891C460F174A6C1510773B444766CA14AFDB2843259DCFFD9337D64A57ECEDCFF0F926CB8A451BE"
            .hexToUByteArray()

    private val decryptedNonce =
        "1A40ABB16BAC88EBBE4777CDC674A75DC4894E5BCF41ABB7A31F3D378EF59596".hexToUByteArray()

    @Test
    fun computesMappedGeneratorFromChipMappingPoint() =
        runTest {
            // Build a step-2 response with the logged chip mapping pub
            val step2Response =
                ubyteArrayOf(0x7Cu, 0x43u, 0x82u, 0x41u) +
                    chipMappingPub +
                    ubyteArrayOf(0x90u, 0x00u)

            val useCase =
                PaceMapNonceUseCase(
                    nfcSessionManager = StubNfcTagReader(step2Response.toByteArray()),
                    commandFactory = CommandFactory(),
                    cryptoEngine = cryptoEngine,
                    keyGenerator = FakeKeyGenerator(testScalar),
                    logger = MemoryLogger(),
                )

            val gPrime = useCase(tag, algorithm, decryptedNonce).getOrThrow()

            // Independently compute expected G' = d_map·chipMappingPub + s·G
            val expectedGPrime =
                computeExpectedMappedGenerator(
                    scalar = testScalar,
                    chipMappingPub = deserializedUncompressedEcPoint(chipMappingPub),
                    nonce = decryptedNonce,
                )

            assertContentEquals(expectedGPrime.x, gPrime.x)
            assertContentEquals(expectedGPrime.y, gPrime.y)
        }

    private fun computeExpectedMappedGenerator(
        scalar: BigInteger,
        chipMappingPub: EcPoint,
        nonce: UByteArray,
    ): EcPoint {
        val params = algorithm.parameter.ecParams()
        val chipPub =
            params.curve.createPoint(
                BigInteger(1, chipMappingPub.x.toByteArray()),
                BigInteger(1, chipMappingPub.y.toByteArray()),
            )
        val h = chipPub.multiply(scalar).normalize()
        val s = BigInteger(1, nonce.toByteArray()).mod(params.n)
        val gPrime = h.add(params.g.multiply(s)).normalize()
        return EcPoint(
            x = gPrime.xCoord.encoded.toUByteArray(),
            y = gPrime.yCoord.encoded.toUByteArray(),
        )
    }
}
