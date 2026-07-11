package fr.outadoc.eidas.nfc

import android.nfc.TagLostException
import android.nfc.tech.IsoDep
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.d
import fr.outadoc.eidas.logging.w
import fr.outadoc.eidas.nfc.NfcSession.Event
import fr.outadoc.eidas.utils.toPrettyHex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext
import java.io.IOException

@OptIn(ExperimentalUnsignedTypes::class)
class RealNfcSession(
    private val isoDep: IsoDep,
    private val logger: Logger,
) : NfcSession,
    AutoCloseable {
    private companion object {
        const val TAG = "RealNfcSession"
    }

    private val _events = Channel<Event>(Channel.BUFFERED)
    override val events: Flow<Event> =
        _events.receiveAsFlow()

    override suspend fun transceive(command: CApdu): Result<RApdu> =
        withContext(Dispatchers.IO) {
            runCatching {
                try {
                    val commandBytes: UByteArray =
                        command.serialize()

                    logger.d(TAG, "SEND > ${commandBytes.toPrettyHex()}")

                    sendBlip()

                    val response: UByteArray =
                        isoDep
                            .transceive(commandBytes.toByteArray())
                            .toUByteArray()

                    sendBlip()

                    logger.d(TAG, "RECV < ${response.toPrettyHex()}")

                    RApdu.parse(response)
                } catch (e: TagLostException) {
                    throw NfcException(
                        "Tag was lost during communication",
                        e,
                    )
                } catch (e: IOException) {
                    throw NfcException("Failed to communicate with tag", e)
                }
            }
        }

    private suspend fun sendBlip() {
        _events.send(Event.Blip)
    }

    override fun close() {
        try {
            isoDep.close()
        } catch (e: Exception) {
            logger.w(TAG, "Error while closing session", e)
        }
    }
}
