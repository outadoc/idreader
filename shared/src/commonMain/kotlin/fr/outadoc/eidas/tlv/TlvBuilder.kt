@file:OptIn(ExperimentalUnsignedTypes::class)

package fr.outadoc.eidas.tlv

fun buildTlv(block: TlvBuilder.() -> Unit): UByteArray = TlvBuilder().apply(block).build()

class TlvBuilder {
    private val buffer = mutableListOf<UByte>()

    fun tlv(tag: UInt, value: UByteArray) {
        buffer += encodeTag(tag)
        buffer += encodeLength(value.size)
        buffer.addAll(value.toList())
    }

    fun tlv(tag: UInt, value: UByte) = tlv(tag, ubyteArrayOf(value))

    fun tlv(tag: UByte, value: UByteArray) = tlv(tag.toUInt(), value)

    fun tlv(tag: UByte, value: UByte) = tlv(tag.toUInt(), ubyteArrayOf(value))

    fun constructed(tag: UInt, block: TlvBuilder.() -> Unit) = tlv(tag, buildTlv(block))

    fun constructed(tag: UByte, block: TlvBuilder.() -> Unit) = constructed(tag.toUInt(), block)

    internal fun build(): UByteArray = buffer.toUByteArray()

    private fun encodeTag(tag: UInt): List<UByte> = when {
        tag <= 0xFFu -> listOf(tag.toUByte())
        tag <= 0xFFFFu -> listOf((tag shr 8).toUByte(), (tag and 0xFFu).toUByte())
        tag <= 0xFFFFFFu -> listOf(
            (tag shr 16).toUByte(),
            ((tag shr 8) and 0xFFu).toUByte(),
            (tag and 0xFFu).toUByte(),
        )
        else -> listOf(
            (tag shr 24).toUByte(),
            ((tag shr 16) and 0xFFu).toUByte(),
            ((tag shr 8) and 0xFFu).toUByte(),
            (tag and 0xFFu).toUByte(),
        )
    }

    private fun encodeLength(length: Int): List<UByte> = when {
        length <= 0x7F -> listOf(length.toUByte())
        length <= 0xFF -> listOf(0x81u.toUByte(), length.toUByte())
        else -> listOf(
            0x82u.toUByte(),
            ((length shr 8) and 0xFF).toUByte(),
            (length and 0xFF).toUByte(),
        )
    }
}
