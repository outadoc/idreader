package fr.outadoc.eidas.settings

import fr.outadoc.eidas.utils.KmpResult

interface SettingsEncryptor {
    fun encrypt(clearText: String): KmpResult<String>

    fun decrypt(cipherText: String): KmpResult<String>
}
