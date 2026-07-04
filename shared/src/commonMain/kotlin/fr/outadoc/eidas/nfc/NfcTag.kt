package fr.outadoc.eidas.nfc

/**
 * A contactless tag detected by an [NfcTagReader].
 *
 * This is a plain value; all communication with the physical tag
 * goes through the [NfcTagReader] that emitted it.
 */
data class NfcTag(
    /** The unique identifier (UID) of the tag. */
    val id: ByteArray,
    /** Human-readable, platform-provided details about the tag. */
    val description: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NfcTag) return false
        return id.contentEquals(other.id) && description == other.description
    }

    override fun hashCode(): Int {
        return 31 * id.contentHashCode() + description.hashCode()
    }
}
