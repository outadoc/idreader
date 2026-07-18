@file:OptIn(ExperimentalForeignApi::class, ExperimentalUnsignedTypes::class)

package fr.outadoc.eidas.nfc

import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.d
import fr.outadoc.eidas.utils.toNSData
import fr.outadoc.eidas.utils.toPrettyHex
import fr.outadoc.eidas.utils.toUByteArray
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreNFC.NFCISO7816APDU
import platform.CoreNFC.NFCISO7816TagProtocol
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class IosNfcSession(
    private val tag: NFCISO7816TagProtocol,
    private val logger: Logger,
) : NfcSession {
    private companion object {
        const val TAG = "IosNfcSession"
    }

    private val _commandCount = MutableStateFlow(0)
    override val commandCount = _commandCount.asStateFlow()

    override suspend fun transceive(command: CApdu): Result<RApdu> =
        runCatching {
            if (!tag.available) {
                throw NfcException("Tag is no longer available")
            }

            val commandBytes = command.serialize()

            logger.d(TAG, "SEND > ${commandBytes.toPrettyHex()}")

            _commandCount.value += 1

            val apdu = NFCISO7816APDU(data = commandBytes.toNSData())

            suspendCancellableCoroutine { continuation ->
                tag.sendCommandAPDU(apdu) { responseData, sw1, sw2, error ->
                    if (error != null) {
                        // NSError is not a Throwable; embed its description instead.
                        continuation.resumeWithException(
                            NfcException(
                                "Failed to communicate with tag: ${error.localizedDescription}",
                            ),
                        )
                    } else {
                        val response =
                            ubyteArrayOf(
                                *(responseData?.toUByteArray() ?: ubyteArrayOf()),
                                sw1,
                                sw2,
                            )

                        logger.d(TAG, "RECV < ${response.toPrettyHex()}")

                        _commandCount.value += 1

                        continuation.resume(RApdu.parse(response))
                    }
                }
            }
        }
}
