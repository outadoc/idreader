package fr.outadoc.eidas.pace

import fr.outadoc.eidas.crypto.Algorithm
import fr.outadoc.eidas.crypto.AndroidCryptoEngine
import fr.outadoc.eidas.crypto.DomainParameter
import fr.outadoc.eidas.crypto.Protocol
import fr.outadoc.eidas.crypto.oidBytes
import fr.outadoc.eidas.logging.MemoryLogger
import fr.outadoc.eidas.nfc.NfcTag
import fr.outadoc.eidas.nfc.commands.CommandFactory
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalUnsignedTypes::class)
class PaceMutualAuthUseCaseTest {
    private val algorithm =
        Algorithm(
            protocol = Protocol.PACE_ECDH_GM_AES_CBC_CMAC_256,
            parameter = DomainParameter.BRAINPOOLP256R1,
        )

    private val cryptoEngine = AndroidCryptoEngine()
    private val tag = NfcTag(id = byteArrayOf(), description = "stub")

    // All values from real card run
    private val kMac =
        "7CFBDBB1859B666FF20F13A7C17F1850DD4B32871B335D4BDA239C48F5EE52BF".hexToUByteArray()

    private val terminalFinalPub =
        "0443F0F927F822FAA6396C2FE9019EB033CD8793C9106B0FD515E8468156FFD3700C1C0FB2D6B27759CAD24D43A34072251BF80D0247FDEF943D9C9049D15BCD15"
            .hexToUByteArray()

    private val chipFinalPub =
        "041AAF77535B2CD3D5D87E2A8728823C508895500143173A19990F50EDA5B3C2D10BBB64BB26EE00E4916C362FC156DF8ADCB14386AA2CEAE88B450E6A919970AB"
            .hexToUByteArray()

    private val expectedTerminalToken = "86201849E47220D4".hexToUByteArray()

    @Test
    fun terminalTokenMatchesLoggedValue() {
        // Verify our CMAC computation produces the same token recorded in the real card run.
        // This is the key diagnostic test for the 63 00 failure.
        val oid = algorithm.protocol.oidBytes
        val tokenInput = paceTokenInput(oid, chipFinalPub)
        val token = cryptoEngine.computeCmac(algorithm, kMac, tokenInput).copyOfRange(0, 8)
        assertContentEquals(expectedTerminalToken, token)
    }

    @Test
    fun mutualAuthSucceedsWithCorrectChipToken() =
        runTest {
            val chipToken = computeChipToken()
            val response = buildChipTokenResponse(chipToken)

            val useCase = buildUseCase(StubNfcTagReader(response))
            useCase(tag, algorithm, kMac, terminalFinalPub, chipFinalPub).getOrThrow()
        }

    @Test
    fun mutualAuthFailsWithWrongChipToken() =
        runTest {
            val wrongToken = UByteArray(8)
            val response = buildChipTokenResponse(wrongToken)

            val useCase = buildUseCase(StubNfcTagReader(response))
            val result = useCase(tag, algorithm, kMac, terminalFinalPub, chipFinalPub)
            assertFailsWith<IllegalStateException> { result.getOrThrow() }
        }

    private fun computeChipToken(): UByteArray {
        val oid = algorithm.protocol.oidBytes
        return cryptoEngine
            .computeCmac(algorithm, kMac, paceTokenInput(oid, terminalFinalPub))
            .copyOfRange(0, 8)
    }

    private fun buildChipTokenResponse(chipToken: UByteArray): ByteArray =
        (
            ubyteArrayOf(0x7Cu, 0x0Au, 0x86u, 0x08u) + chipToken +
                ubyteArrayOf(
                    0x90u,
                    0x00u,
                )
        ).toByteArray()

    private fun buildUseCase(stubReader: StubNfcTagReader): PaceMutualAuthUseCase =
        PaceMutualAuthUseCase(
            nfcSessionManager = stubReader,
            commandFactory = CommandFactory(),
            cryptoEngine = cryptoEngine,
            logger = MemoryLogger(),
        )

    private fun paceTokenInput(
        oid: UByteArray,
        pubKey: UByteArray,
    ): UByteArray {
        val oidTlv = ubyteArrayOf(0x06u, oid.size.toUByte(), *oid)
        val pubKeyTlv = ubyteArrayOf(0x86u, pubKey.size.toUByte(), *pubKey)
        val inner = oidTlv + pubKeyTlv
        return ubyteArrayOf(0x7Fu, 0x49u, inner.size.toUByte(), *inner)
    }
}
