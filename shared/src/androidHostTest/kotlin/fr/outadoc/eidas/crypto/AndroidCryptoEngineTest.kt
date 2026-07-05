package fr.outadoc.eidas.crypto

import kotlin.test.Test
import kotlin.test.assertContentEquals

@OptIn(ExperimentalUnsignedTypes::class)
class AndroidCryptoEngineTest {

    private val algorithm = Algorithm.PACE_AES256_GM_ECDH_BRAINPOOLP256R1
    private val cryptoEngine = AndroidCryptoEngine()

    // Test vectors from a real card run
    private val sharedSecret =
        "24B98B1CAB1BA8987E36F0FB14AB117D102A9CC94CA0A4B9DE03E6E76DB5DAFE".hexBytes()

    private val expectedKEnc =
        "329376CE32D140C51861BDD3878ED0B1823AC9144241975AD8A8D018624F2FBD".hexBytes()

    private val expectedKMac =
        "7CFBDBB1859B666FF20F13A7C17F1850DD4B32871B335D4BDA239C48F5EE52BF".hexBytes()

    private val chipFinalPub =
        "041AAF77535B2CD3D5D87E2A8728823C508895500143173A19990F50EDA5B3C2D10BBB64BB26EE00E4916C362FC156DF8ADCB14386AA2CEAE88B450E6A919970AB".hexBytes()

    private val expectedTerminalToken = "86201849E47220D4".hexBytes()

    @Test
    fun kdfWithCounter1ProducesKEnc() {
        val kEnc = cryptoEngine.deriveKeyFromSecret(algorithm, sharedSecret, ubyteArrayOf(), 1)
        assertContentEquals(expectedKEnc, kEnc)
    }

    @Test
    fun kdfWithCounter2ProducesKMac() {
        val kMac = cryptoEngine.deriveKeyFromSecret(algorithm, sharedSecret, ubyteArrayOf(), 2)
        assertContentEquals(expectedKMac, kMac)
    }

    @Test
    fun cmacOfChipPubUnderKMacMatchesLoggedTerminalToken() {
        val oid = algorithm.oid.bytes.toUByteArray()
        val tokenInput = paceTokenInput(oid, chipFinalPub)
        val token = cryptoEngine.computeCmac(algorithm, expectedKMac, tokenInput).copyOfRange(0, 8)
        assertContentEquals(expectedTerminalToken, token)
    }

    private fun paceTokenInput(oid: UByteArray, pubKey: UByteArray): UByteArray {
        val oidTlv = ubyteArrayOf(0x06u, oid.size.toUByte(), *oid)
        val pubKeyTlv = ubyteArrayOf(0x86u, pubKey.size.toUByte(), *pubKey)
        val inner = oidTlv + pubKeyTlv
        return ubyteArrayOf(0x7Fu, 0x49u, inner.size.toUByte(), *inner)
    }
}

private fun String.hexBytes(): UByteArray =
    chunked(2).map { it.toInt(16).toUByte() }.toUByteArray()
