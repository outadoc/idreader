package fr.outadoc.eidas.nfc

import kotlinx.coroutines.flow.Flow

/**
 * Platform-agnostic access to contactless tags supporting
 * APDU-based communication (ISO 14443-4 / ISO 7816-4).
 */
interface NfcTagReader {
    /**
     * Tags detected near the device.
     *
     * This is a cold flow: collecting it starts NFC discovery,
     * and cancelling the collection stops it.
     *
     * @throws NfcException if NFC is not available on this device.
     */
    val detectedTags: Flow<NfcTag>
}
