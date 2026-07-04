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

    /**
     * Sends a raw APDU command to the given tag and returns the response,
     * including the SW1/SW2 status bytes.
     *
     * @throws NfcException if the tag has moved out of range or communication fails.
     */
    suspend fun transceive(tag: NfcTag, command: ByteArray): ByteArray
}
