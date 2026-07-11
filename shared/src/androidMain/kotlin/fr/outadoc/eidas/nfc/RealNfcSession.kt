package fr.outadoc.eidas.nfc

import android.nfc.TagLostException
import android.nfc.tech.IsoDep
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.d
import fr.outadoc.eidas.logging.w
import fr.outadoc.eidas.utils.toPrettyHex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _commandCount = MutableStateFlow(0)
    override val commandCount = _commandCount.asStateFlow()

    override suspend fun transceive(command: CApdu): Result<RApdu> =
        withContext(Dispatchers.IO) {
            runCatching {
                try {
                    val commandBytes: UByteArray =
                        command.serialize()

                    logger.d(TAG, "SEND > ${commandBytes.toPrettyHex()}")

                    _commandCount.value += 1

                    val response: UByteArray =
                        isoDep
                            .transceive(commandBytes.toByteArray())
                            .toUByteArray()

                    _commandCount.value += 1

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

    override fun close() {
        try {
            isoDep.close()
        } catch (e: Exception) {
            logger.w(TAG, "Error while closing session", e)
        }
    }
}
