package fr.outadoc.eidas.pace

import fr.outadoc.eidas.crypto.Algorithm
import fr.outadoc.eidas.crypto.AndroidCryptoEngine
import fr.outadoc.eidas.crypto.DomainParameter
import fr.outadoc.eidas.crypto.Protocol
import fr.outadoc.eidas.logging.MemoryLogger
import fr.outadoc.eidas.nfc.NfcTag
import fr.outadoc.eidas.nfc.commands.CommandFactory
import fr.outadoc.eidas.settings.model.AuthenticationMethod
import fr.outadoc.eidas.utils.toKmpBytes
import kotlinx.coroutines.test.runTest
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.modes.CBCBlockCipher
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import kotlin.test.Test
import kotlin.test.assertContentEquals

@OptIn(ExperimentalUnsignedTypes::class)
class PaceGetNonceUseCaseTest {
    private val algorithm =
        Algorithm(
            protocol = Protocol.PACE_ECDH_GM_AES_CBC_CMAC_256,
            parameter = DomainParameter.BRAINPOOLP256R1,
        )

    private val cryptoEngine = AndroidCryptoEngine()
    private val tag = NfcTag(id = byteArrayOf(), description = "stub")

    @Test
    fun decryptsChipNonceCorrectly() =
        runTest {
            val testCan = "123456"
            val plainNonce = ByteArray(32) { it.toByte() }

            val canBytes = testCan.toByteArray(Charsets.US_ASCII).toUByteArray()
            val kPi =
                cryptoEngine.deriveKeyFromSecret(
                    algorithm = algorithm,
                    secret = canBytes.toKmpBytes(),
                    nonce = ubyteArrayOf().toKmpBytes(),
                    counter = 3,
                )

            val encryptedNonce =
                encryptAesCbc(
                    key = kPi.raw,
                    data = plainNonce,
                )

            val mseResponse = ubyteArrayOf(0x90u, 0x00u)
            val step1Response =
                ubyteArrayOf(0x7Cu, 0x22u, 0x80u, 0x20u) +
                    encryptedNonce.toUByteArray() +
                    ubyteArrayOf(0x90u, 0x00u)

            val nfcSession =
                StubNfcSession(
                    mseResponse.toByteArray(),
                    step1Response.toByteArray(),
                )

            val useCase =
                PaceGetNonceUseCase(
                    commandFactory = CommandFactory(),
                    cryptoEngine = cryptoEngine,
                    logger = MemoryLogger(),
                )

            val result = useCase(nfcSession, algorithm, AuthenticationMethod.CAN, testCan).getOrThrow()
            assertContentEquals(plainNonce.toUByteArray(), result)
        }

    private fun encryptAesCbc(
        key: ByteArray,
        data: ByteArray,
    ): ByteArray {
        val cbc = CBCBlockCipher.newInstance(AESEngine.newInstance())
        cbc.init(true, ParametersWithIV(KeyParameter(key), ByteArray(cbc.blockSize)))
        val output = ByteArray(data.size)
        var offset = 0
        while (offset < data.size) {
            offset += cbc.processBlock(data, offset, output, offset)
        }
        return output
    }
}
