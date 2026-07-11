package fr.outadoc.eidas.nfc

import kotlinx.coroutines.flow.StateFlow

interface NfcSession {
    val commandCount: StateFlow<Int>

    /**
     * Sends a raw APDU command to the given tag and returns the response,
     * including the SW1/SW2 status bytes.
     *
     * Returns [Result.failure] wrapping an [NfcException] if the tag has
     * moved out of range or communication fails.
     */
    suspend fun transceive(command: CApdu): Result<RApdu>
}
