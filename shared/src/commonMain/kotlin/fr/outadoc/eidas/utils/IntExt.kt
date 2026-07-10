@file:OptIn(ExperimentalUnsignedTypes::class)

package fr.outadoc.eidas.utils

fun Int.toByteArrayBe(byteCount: Int = 4): UByteArray {
    val result = UByteArray(byteCount)
    for (i in 0 until byteCount) {
        result[byteCount - 1 - i] = ((this shr (i * 8)) and 0xFF).toUByte()
    }
    return result
}

fun UInt.toByteArrayBe(byteCount: Int = 4): UByteArray = toInt().toByteArrayBe(byteCount)
