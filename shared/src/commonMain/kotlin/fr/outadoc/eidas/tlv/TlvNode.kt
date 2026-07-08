@file:OptIn(ExperimentalUnsignedTypes::class)

package fr.outadoc.eidas.tlv

class TlvNode(
    val tag: UInt,
    val value: UByteArray,
) {
    fun children(): Result<List<TlvNode>> = value.parseTlv()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TlvNode) return false
        return tag == other.tag && value.contentEquals(other.value)
    }

    override fun hashCode(): Int = 31 * tag.hashCode() + value.contentHashCode()

    override fun toString(): String = "TlvNode(tag=0x${tag.toString(16).uppercase()}, value=[${value.size} bytes])"
}
