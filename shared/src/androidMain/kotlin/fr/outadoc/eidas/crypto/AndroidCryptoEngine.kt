package fr.outadoc.eidas.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import kotlin.uuid.Uuid

class AndroidCryptoEngine : CryptoEngine {
    fun generateKeyPair(algorithm: Algorithm): KeyPair {
        val alias = Uuid.random()

        val parameterSpec: KeyGenParameterSpec =
            KeyGenParameterSpec
                .Builder(
                    alias.toHexDashString(),
                    KeyProperties.PURPOSE_AGREE_KEY,
                ).setIsStrongBoxBacked(true)
                .setAlgorithmParameterSpec(
                    ECGenParameterSpec(algorithm.toStdJavaName()),
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

    private fun Algorithm.toStdJavaName(): String =
        when (this) {
            Algorithm.PACE_AES256_GM_ECDH_BRAINPOOLP256R1 -> {
                "brainpoolP256r1"
            }
        }
}
