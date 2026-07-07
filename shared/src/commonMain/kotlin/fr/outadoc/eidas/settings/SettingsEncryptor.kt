package fr.outadoc.eidas.settings

interface SettingsEncryptor {
    fun encrypt(clearText: String): Result<String>
    fun decrypt(cipherText: String): Result<String>
}
