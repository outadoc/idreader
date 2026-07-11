@file:OptIn(ExperimentalForeignApi::class, ExperimentalUnsignedTypes::class)

package fr.outadoc.eidas.nfc

import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.d
import fr.outadoc.eidas.utils.toNSData
import fr.outadoc.eidas.utils.toPrettyHex
import fr.outadoc.eidas.utils.toUByteArray
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
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

    private val _events = Channel<NfcSession.Event>(Channel.BUFFERED)
    override val events: Flow<NfcSession.Event> =
        _events.receiveAsFlow()

    override suspend fun transceive(command: CApdu): Result<RApdu> =
        runCatching {
            if (!tag.available) {
                throw NfcException("Tag is no longer available")
            }

            val commandBytes = command.serialize()
            logger.d(TAG, "SEND > ${commandBytes.toPrettyHex()}")

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
                        continuation.resume(RApdu.parse(response))
                    }
                }
            }
        }
}
