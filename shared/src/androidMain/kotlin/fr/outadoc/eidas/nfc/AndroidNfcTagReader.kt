package fr.outadoc.eidas.nfc

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.TagLostException
import android.nfc.tech.IsoDep
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.d
import fr.outadoc.eidas.logging.e
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

private const val TAG = "AndroidNfcTagReader"

/**
 * [NfcTagReader] backed by Android's reader mode API.
 *
 * Only IsoDep (ISO 14443-4) tags are reported, since they are the only
 * ones supporting APDU communication.
 */
class AndroidNfcTagReader(
    private val activity: Activity,
    private val logger: Logger,
) : NfcTagReader {

    private var currentConnection: Pair<NfcTag, IsoDep>? = null

    override val detectedTags: Flow<NfcTag> = callbackFlow {
        val adapter = NfcAdapter.getDefaultAdapter(activity)
            ?: throw NfcException("NFC is not available on this device")

        val callback = NfcAdapter.ReaderCallback { tag ->
            val isoDep = IsoDep.get(tag)
                ?: return@ReaderCallback

            try {
                isoDep.connect()
            } catch (e: IOException) {
                logger.e(TAG, "Failed to connect to tag", e)
                return@ReaderCallback
            }

            val nfcTag = NfcTag(
                id = tag.id,
                description = buildString {
                    append("Tech: ${tag.techList.joinToString()}")
                    isoDep.historicalBytes?.let { bytes ->
                        append("\nHistorical bytes: ${bytes.toHexString()}")
                    }
                    append("\nMax transceive length: ${isoDep.maxTransceiveLength}")
                },
            )

            currentConnection?.second?.closeQuietly()
            currentConnection = nfcTag to isoDep

            logger.d(TAG, "Tag detected: uid=${nfcTag.id.toHexString()}")
            trySend(nfcTag)
        }

        adapter.enableReaderMode(
            activity,
            callback,
            NfcAdapter.FLAG_READER_NFC_A
                or NfcAdapter.FLAG_READER_NFC_B
                or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null,
        )

        awaitClose {
            adapter.disableReaderMode(activity)
            currentConnection?.second?.closeQuietly()
            currentConnection = null
        }
    }

    override suspend fun transceive(tag: NfcTag, command: ByteArray): ByteArray {
        val isoDep = currentConnection
            ?.takeIf { (currentTag, _) -> currentTag == tag }
            ?.second
            ?: throw NfcException("Tag is no longer available")

        return withContext(Dispatchers.IO) {
            try {
                isoDep.transceive(command)
            } catch (e: TagLostException) {
                throw NfcException("Tag was lost during communication", e)
            } catch (e: IOException) {
                throw NfcException("Failed to communicate with tag", e)
            }
        }
    }

    private fun IsoDep.closeQuietly() {
        try {
            close()
        } catch (_: IOException) {
        }
    }
}
