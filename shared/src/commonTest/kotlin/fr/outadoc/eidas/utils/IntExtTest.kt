package fr.outadoc.eidas.utils

import kotlin.test.Test
import kotlin.test.assertContentEquals

@OptIn(ExperimentalUnsignedTypes::class)
class IntExtTest {

    @Test
    fun positiveIntDefaultByteCount() {
        assertContentEquals(
            ubyteArrayOf(0x00u, 0x00u, 0x00u, 0x01u),
            1.toByteArrayBe(),
        )
    }

    @Test
    fun positiveIntExplicitByteCount() {
        assertContentEquals(
            ubyteArrayOf(0x00u, 0x01u),
            1.toByteArrayBe(byteCount = 2),
        )
    }

    @Test
    fun multiByteValue() {
        assertContentEquals(
            ubyteArrayOf(0x00u, 0x01u, 0x02u, 0x03u),
            0x00010203.toByteArrayBe(),
        )
    }

    @Test
    fun maxUnsignedByte() {
        assertContentEquals(
            ubyteArrayOf(0xFFu),
            0xFF.toByteArrayBe(byteCount = 1),
        )
    }

    @Test
    fun zero() {
        assertContentEquals(
            ubyteArrayOf(0x00u, 0x00u, 0x00u, 0x00u),
            0.toByteArrayBe(),
        )
    }

    @Test
    fun counterValueOne() {
        assertContentEquals(
            ubyteArrayOf(0x00u, 0x00u, 0x00u, 0x01u),
            1.toByteArrayBe(byteCount = 4),
        )
    }
}
