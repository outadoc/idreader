package fr.outadoc.eidas.settings

import at.asitplus.KmmResult

interface SettingsEncryptor {
    fun encrypt(clearText: String): KmmResult<String>

    fun decrypt(cipherText: String): KmmResult<String>
}
