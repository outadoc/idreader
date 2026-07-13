package fr.outadoc.eidas.pace

import fr.outadoc.eidas.crypto.CryptoEngine
import fr.outadoc.eidas.settings.model.AuthenticationMethod

@OptIn(ExperimentalUnsignedTypes::class)
class EncodePasswordUseCase(
    private val cryptoEngine: CryptoEngine,
) {
    operator fun invoke(
        authenticationMethod: AuthenticationMethod,
        password: String,
    ): UByteArray {
        val bytes: UByteArray =
            password
                .encodeToByteArray()
                .toUByteArray()

        return when (authenticationMethod) {
            AuthenticationMethod.MRZ -> {
                cryptoEngine.computeSha1(bytes)
            }

            AuthenticationMethod.CAN,
            AuthenticationMethod.PIN,
            AuthenticationMethod.PUK,
            -> {
                bytes
            }
        }
    }
}
