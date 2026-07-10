@file:OptIn(ExperimentalUnsignedTypes::class)

package fr.outadoc.eidas.tlv

fun UByteArray.parseTlv(): Result<List<TlvNode>> =
    runCatching {
        val nodes = mutableListOf<TlvNode>()
        var i = 0
        while (i < size) {
            if (this[i] == 0x00u.toUByte()) {
                i++
                continue
            }
            val (tag, tagBytes) = readTag(i)
            i += tagBytes
            require(i < size) { "Truncated TLV: tag 0x${tag.toString(16)} at end of buffer" }
            val (length, lengthBytes) = readLength(i)
            i += lengthBytes
            require(i + length <= size) { "Truncated TLV value: need $length bytes, have ${size - i}" }
            nodes += TlvNode(tag, copyOfRange(i, i + length))
            i += length
        }
        nodes
    }

fun List<TlvNode>.firstWithTag(tag: UInt): Result<TlvNode> {
    val node = firstOrNull { it.tag == tag }
    return if (node == null) {
        Result.failure(
            NoSuchElementException("No TLV node with tag 0x${tag.toString(16)}"),
        )
    } else {
        Result.success(node)
    }
}

fun List<TlvNode>.firstWithTag(tag: UByte): Result<TlvNode> = firstWithTag(tag.toUInt())

private fun UByteArray.readTag(offset: Int): Pair<UInt, Int> {
    val first = this[offset].toUInt()
    if (first and 0x1Fu != 0x1Fu) return Pair(first, 1)
    var tag = first
    var consumed = 1
    do {
        require(offset + consumed < size) { "Truncated multi-byte tag at offset $offset" }
        val next = this[offset + consumed].toUInt()
        tag = (tag shl 8) or next
        consumed++
    } while (next and 0x80u != 0u)
    return Pair(tag, consumed)
}

private fun UByteArray.readLength(offset: Int): Pair<Int, Int> {
    val first = this[offset].toInt() and 0xFF
    return when {
        first <= 0x7F -> {
            Pair(first, 1)
        }

        first == 0x81 -> {
            require(offset + 1 < size) { "Truncated long-form length at offset $offset" }
            Pair(this[offset + 1].toInt() and 0xFF, 2)
        }

        first == 0x82 -> {
            require(offset + 2 < size) { "Truncated long-form length at offset $offset" }
            val len =
                ((this[offset + 1].toInt() and 0xFF) shl 8) or (this[offset + 2].toInt() and 0xFF)
            Pair(len, 3)
        }

        else -> {
            throw IllegalArgumentException(
                "Unsupported BER-TLV length form 0x${
                    first.toString(
                        16,
                    )
                } at offset $offset",
            )
        }
    }
}
