@file:OptIn(ExperimentalForeignApi::class, ExperimentalUnsignedTypes::class)

package fr.outadoc.eidas.nfc

import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.d
import fr.outadoc.eidas.logging.e
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreNFC.NFCPollingISO14443
import platform.CoreNFC.NFCTagProtocol
import platform.CoreNFC.NFCTagReaderSession
import platform.CoreNFC.NFCTagReaderSessionDelegateProtocol
import platform.Foundation.NSError
import platform.darwin.NSObject

private const val TAG = "IosNfcTagReader"

/**
 * [NfcTagReader] backed by CoreNFC's [NFCTagReaderSession].
 *
 * Only ISO 7816 (ISO 14443-4) tags are reported, since they are the only
 * ones supporting APDU communication.
 *
 * Unlike Android, an iOS reader session effectively yields a single tag:
 * the system NFC sheet dismisses after a read or invalidation, and sessions
 * auto-invalidate after ~60 seconds. When the session is invalidated, the
 * flow terminates; collect it again to start a new session.
 */
class IosNfcTagReader(
    private val logger: Logger,
) : NfcTagReader {
    override fun waitForTag(): Flow<NfcSession> =
        callbackFlow {
            if (!NFCTagReaderSession.readingAvailable) {
                throw NfcException("NFC is not available on this device")
            }

            // Strong reference kept for the whole collection lifetime;
            // don't rely on the session retaining its delegate.
            val delegate =
                TagReaderDelegate(
                    logger = logger,
                    onTagDetected = { session -> trySend(session) },
                    onInvalidated = { error -> close(error) },
                )

            val session =
                NFCTagReaderSession(
                    pollingOption = NFCPollingISO14443,
                    delegate = delegate,
                    queue = null,
                )

            session.alertMessage = "Hold your document near the top of the phone."
            session.beginSession()

            logger.d(TAG, "Waiting for tags…")

            awaitClose {
                // Triggers didInvalidateWithError; close() on an already
                // cancelled channel is a no-op, so this is harmless.
                session.invalidateSession()
            }
        }
}

private class TagReaderDelegate(
    private val logger: Logger,
    private val onTagDetected: (IosNfcSession) -> Unit,
    private val onInvalidated: (NfcException) -> Unit,
) : NSObject(),
    NFCTagReaderSessionDelegateProtocol {
    override fun tagReaderSessionDidBecomeActive(session: NFCTagReaderSession) {
        logger.d(TAG, "Session became active")
    }

    @ObjCSignatureOverride
    override fun tagReaderSession(
        session: NFCTagReaderSession,
        didDetectTags: List<*>,
    ) {
        val tag = didDetectTags.firstOrNull() as? NFCTagProtocol ?: return

        session.connectToTag(tag) { error ->
            if (error != null) {
                logger.e(TAG, "Failed to connect to tag: ${error.localizedDescription}")
                session.restartPolling()
                return@connectToTag
            }

            val iso7816 = tag.asNFCISO7816Tag()
            if (iso7816 == null) {
                logger.e(TAG, "Detected tag is not ISO 7816")
                session.restartPolling()
                return@connectToTag
            }

            logger.d(TAG, "Tag detected: aid=${iso7816.initialSelectedAID}")

            onTagDetected(
                IosNfcSession(
                    tag = iso7816,
                    logger = logger,
                ),
            )
        }
    }

    @ObjCSignatureOverride
    override fun tagReaderSession(
        session: NFCTagReaderSession,
        didInvalidateWithError: NSError,
    ) {
        onInvalidated(
            NfcException(
                "NFC session was invalidated: ${didInvalidateWithError.localizedDescription}",
            ),
        )
    }
}
