package fr.outadoc.eidas.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.modes.CBCBlockCipher
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.spec.ECGenParameterSpec
import kotlin.uuid.Uuid

class AndroidCryptoEngine : CryptoEngine {
    override fun generateKeyPair(algorithm: Algorithm): KeyPair {
        val alias = Uuid.random()

        val parameterSpec: KeyGenParameterSpec =
            KeyGenParameterSpec
                .Builder(
                    alias.toHexDashString(),
                    KeyProperties.PURPOSE_AGREE_KEY,
                ).setAlgorithmParameterSpec(
                    ECGenParameterSpec(algorithm.getEcdhFunctionName()),
                ).build()

        val kpg: KeyPairGenerator =
            KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                "AndroidKeyStore",
            )

        kpg.initialize(parameterSpec)

        val kp = kpg.generateKeyPair()

        return KeyPair(
            alias = alias,
            publicKey =
                AndroidPublicKey(
                    publicKey = kp.public,
                ),
        )
    }

    /**
     * Generic key derivation function.
     *
     * See section A.2.3
     *
     * @param secret The shared secret value K
     * @param nonce A nonce (optional)
     * @param counter An integer counter
     */
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

        val digest =
            MessageDigest
                .getInstance(algorithm.getHashFunctionName())
                .apply { update(message.toByteArray()) }
                .digest()

        return digest.toUByteArray()
    }

    override fun decryptSymmetric(
        algorithm: Algorithm,
        key: UByteArray,
        data: UByteArray,
    ): UByteArray =
        when (algorithm) {
            Algorithm.PACE_AES256_GM_ECDH_BRAINPOOLP256R1 -> {
                decryptAesCbc(
                    key = key.toByteArray(),
                    data = data.toByteArray(),
                ).toUByteArray()
            }
        }

    private fun decryptAesCbc(
        key: ByteArray,
        data: ByteArray,
    ): ByteArray {
        val cipher = AESEngine.newInstance()
        val cbc = CBCBlockCipher.newInstance(cipher)

        val params =
            ParametersWithIV(
                KeyParameter(key),
                ByteArray(cbc.blockSize),
            )

        cbc.init(false, params)

        val output = ByteArray(data.size)
        var offset = 0
        while (offset < data.size) {
            offset += cbc.processBlock(data, offset, output, offset)
        }

        return output
    }

    fun Int.toByteArrayBe(): UByteArray {
        val bb = ByteBuffer.allocate(4)
        bb.order(ByteOrder.BIG_ENDIAN)
        bb.putInt(this)
        return bb.array().toUByteArray()
    }

    private fun Algorithm.getEcdhFunctionName(): String =
        when (this) {
            Algorithm.PACE_AES256_GM_ECDH_BRAINPOOLP256R1 -> {
                "brainpoolP256r1"
            }
        }

    private fun Algorithm.getHashFunctionName(): String =
        when (this) {
            Algorithm.PACE_AES256_GM_ECDH_BRAINPOOLP256R1 -> "SHA-256"
        }
}
