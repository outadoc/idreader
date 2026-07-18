@file:OptIn(ExperimentalUnsignedTypes::class)

package fr.outadoc.eidas.utils

class KmpBytes(
    val raw: ByteArray,
) {
    fun toUByteArray(): UByteArray = raw.toUByteArray()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as KmpBytes
        return raw.contentEquals(other.raw)
    }

    override fun hashCode(): Int = raw.contentHashCode()
}

fun UByteArray.toKmpBytes(): KmpBytes = KmpBytes(toByteArray())
