package fr.outadoc.eidas.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder

@OptIn(ExperimentalUnsignedTypes::class)
actual fun Int.toByteArrayBe(byteCount: Int): UByteArray {
    val bb = ByteBuffer.allocate(byteCount)
    bb.order(ByteOrder.BIG_ENDIAN)
    bb.putInt(this)
    return bb.array().toUByteArray()
}

actual fun UInt.toByteArrayBe(byteCount: Int): UByteArray = toInt().toByteArrayBe()
