package fr.outadoc.eidas.pace

import fr.outadoc.eidas.crypto.Algorithm
import fr.outadoc.eidas.crypto.CryptoEngine
import fr.outadoc.eidas.crypto.oidBytes
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.d
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.Icao9303
import fr.outadoc.eidas.nfc.NfcSession
import fr.outadoc.eidas.nfc.commands.CommandFactory
import fr.outadoc.eidas.nfc.tlvList
import fr.outadoc.eidas.settings.model.AuthenticationMethod
import fr.outadoc.eidas.tlv.firstWithTag
import fr.outadoc.eidas.utils.KmpBytes
import fr.outadoc.eidas.utils.flatMap
import fr.outadoc.eidas.utils.toKmpBytes
import fr.outadoc.eidas.utils.toPrettyHex

private const val TAG = "PaceGetNonceUseCase"

@OptIn(ExperimentalUnsignedTypes::class)
class PaceGetNonceUseCase(
    private val commandFactory: CommandFactory,
    private val cryptoEngine: CryptoEngine,
    private val logger: Logger,
) {
    suspend operator fun invoke(
        nfcSession: NfcSession,
        algorithm: Algorithm,
        authenticationMethod: AuthenticationMethod,
        password: UByteArray,
    ): Result<UByteArray> {
        logger.i(TAG, "MSE:Set AT")

        nfcSession
            .transceive(
                commandFactory.paceSetAt(
                    algorithm = algorithm.protocol.oidBytes,
                    keyReference =
                        when (authenticationMethod) {
                            AuthenticationMethod.CAN -> Icao9303.KeyRef.CAN
                            AuthenticationMethod.MRZ -> Icao9303.KeyRef.MRZ
                            AuthenticationMethod.PIN -> Icao9303.KeyRef.PIN
                            AuthenticationMethod.PUK -> Icao9303.KeyRef.PUK
                        },
                ),
            ).flatMap { it.getData() }
            .getOrElse { return Result.failure(it) }

        logger.i(TAG, "GENERAL AUTHENTICATE (step 1: encrypted nonce)")

        return nfcSession
            .transceive(
                commandFactory.generalAuthenticate(
                    tlvList {
                        tlv(
                            Icao9303.Tags.DynamicAuthenticationData,
                            ubyteArrayOf(),
                        )
                    },
                ),
            ).flatMap { rApdu -> rApdu.getData() }
            .flatMap { data -> data.parseDynamicAuthData() }
            .flatMap { dynAuth -> dynAuth.firstWithTag(Icao9303.Tags.Nonce) }
            .mapCatching { nonceNode ->
                val encryptedNonce: UByteArray = nonceNode.value

                logger.d(TAG, "Encrypted nonce: ${encryptedNonce.toPrettyHex()}")

                val kPi: KmpBytes =
                    cryptoEngine.deriveKeyFromSecret(
                        algorithm = algorithm,
                        secret = password.toKmpBytes(),
                        nonce = ubyteArrayOf().toKmpBytes(),
                        counter = 3,
                    )

                cryptoEngine
                    .decryptSymmetric(
                        algorithm = algorithm,
                        key = kPi,
                        data = encryptedNonce.toKmpBytes(),
                    ).toUByteArray()
            }.onSuccess { decryptedNonce ->
                logger.d(TAG, "Decrypted nonce: ${decryptedNonce.toPrettyHex()}")
            }
    }
}
