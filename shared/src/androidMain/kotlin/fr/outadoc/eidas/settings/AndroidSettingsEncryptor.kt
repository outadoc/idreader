package fr.outadoc.eidas.settings

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import at.asitplus.KmmResult.Companion.wrap
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
class AndroidSettingsEncryptor : SettingsEncryptor {

    override fun encrypt(clearText: String) = runCatching {
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(clearText.encodeToByteArray())
        Base64.encode(iv + encryptedBytes)
    }.wrap()

    override fun decrypt(cipherText: String) = runCatching {
        val combined = Base64.decode(cipherText)
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val encryptedBytes = combined.copyOfRange(GCM_IV_LENGTH, combined.size)

        Cipher
            .getInstance(CIPHER_TRANSFORMATION)
            .apply {
                init(
                    Cipher.DECRYPT_MODE,
                    getOrCreateKey(),
                    GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
                )
            }
            .doFinal(encryptedBytes)
            .decodeToString()
    }.wrap()

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore
            .getInstance(KEYSTORE_PROVIDER)
            .apply { load(null) }

        keyStore
            .getKey(ENCRYPTION_KEY_ALIAS, null)
            ?.let { return it as SecretKey }

        return KeyGenerator
            .getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE_PROVIDER
            )
            .apply {
                init(
                    KeyGenParameterSpec.Builder(
                        ENCRYPTION_KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(AES_KEY_SIZE)
                        .build(),
                )
            }
            .generateKey()
    }

    private companion object {
        const val ENCRYPTION_KEY_ALIAS = "0f457983-cf72-49f8-9c57-4e76436de169"
        const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_IV_LENGTH = 12
        const val GCM_TAG_LENGTH_BITS = 128
        const val AES_KEY_SIZE = 256
    }
}
