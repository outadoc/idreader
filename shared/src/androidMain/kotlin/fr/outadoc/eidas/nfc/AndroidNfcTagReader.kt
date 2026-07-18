package fr.outadoc.eidas.nfc

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.tech.IsoDep
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.d
import fr.outadoc.eidas.logging.e
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.IOException

private const val TAG = "AndroidNfcTagReader"

/**
 * [NfcTagReader] backed by Android's reader mode API.
 *
 * Only IsoDep (ISO 14443-4) tags are reported, since they are the only
 * ones supporting APDU communication.
 */
@OptIn(ExperimentalUnsignedTypes::class)
class AndroidNfcTagReader(
    private val activity: Activity,
    private val logger: Logger,
) : NfcTagReader {
    private var currentConnection: AndroidNfcSession? = null

    override val detectedTags: Flow<NfcSession> =
        callbackFlow {
            val adapter =
                NfcAdapter.getDefaultAdapter(activity)
                    ?: throw NfcException("NFC is not available on this device")

            val callback =
                NfcAdapter.ReaderCallback { tag ->
                    val isoDep =
                        IsoDep.get(tag)
                            ?: return@ReaderCallback

                    try {
                        isoDep.connect()
                    } catch (e: IOException) {
                        logger.e(TAG, "Failed to connect to tag", e)
                        return@ReaderCallback
                    }

                    val nfcTag =
                        NfcTag(
                            id = tag.id,
                            description =
                                buildString {
                                    append("Tech: ${tag.techList.joinToString()}")
                                    isoDep.historicalBytes?.let { bytes ->
                                        append("\nHistorical bytes: ${bytes.toHexString()}")
                                    }
                                    append("\nMax transceive length: ${isoDep.maxTransceiveLength}")
                                },
                        )

                    logger.d(
                        TAG,
                        "Tag detected: uid=${nfcTag.id.toHexString()}, ${nfcTag.description}",
                    )

                    val session =
                        AndroidNfcSession(
                            isoDep = isoDep,
                            logger = logger,
                        )

                    currentConnection?.close()
                    currentConnection = session

                    trySend(session)
                }

            adapter.enableReaderMode(
                activity,
                callback,
                NfcAdapter.FLAG_READER_NFC_A
                    or NfcAdapter.FLAG_READER_NFC_B
                    or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                null,
            )

            logger.d(TAG, "Waiting for document…")

            awaitClose {
                logger.d(TAG, "Disabling reader mode")
                adapter.disableReaderMode(activity)
                currentConnection?.close()
                currentConnection = null
            }
        }
}
