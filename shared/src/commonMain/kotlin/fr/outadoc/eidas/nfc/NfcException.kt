package fr.outadoc.eidas.nfc

/**
 * Wraps platform-specific NFC errors so that common code
 * never has to deal with platform exception types.
 */
class NfcException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
