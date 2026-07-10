@file:OptIn(ExperimentalUnsignedTypes::class)

package fr.outadoc.eidas.tlv

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TlvParserTest {
    @Test
    fun parseSinglePrimitiveTlv() {
        val bytes = ubyteArrayOf(0x80u, 0x04u, 0x01u, 0x02u, 0x03u, 0x04u)
        val nodes = bytes.parseTlv().getOrThrow()
        assertEquals(1, nodes.size)
        assertEquals(0x80u, nodes[0].tag)
        assertContentEquals(ubyteArrayOf(0x01u, 0x02u, 0x03u, 0x04u), nodes[0].value)
    }

    @Test
    fun parseMultipleSequentialTlvs() {
        val bytes = ubyteArrayOf(0x80u, 0x01u, 0xAAu, 0x81u, 0x01u, 0xBBu)
        val nodes = bytes.parseTlv().getOrThrow()
        assertEquals(2, nodes.size)
        assertEquals(0x80u, nodes[0].tag)
        assertContentEquals(ubyteArrayOf(0xAAu), nodes[0].value)
        assertEquals(0x81u, nodes[1].tag)
        assertContentEquals(ubyteArrayOf(0xBBu), nodes[1].value)
    }

    @Test
    fun parseLongFormLengthOneByte() {
        val payload = UByteArray(200) { it.toUByte() }
        val wire = ubyteArrayOf(0x80u, 0x81u, 0xC8u) + payload
        val nodes = wire.parseTlv().getOrThrow()
        assertEquals(1, nodes.size)
        assertContentEquals(payload, nodes[0].value)
    }

    @Test
    fun parseLongFormLengthTwoBytes() {
        val payload = UByteArray(300) { it.toUByte() }
        val wire = ubyteArrayOf(0x80u, 0x82u, 0x01u, 0x2Cu) + payload
        val nodes = wire.parseTlv().getOrThrow()
        assertEquals(1, nodes.size)
        assertEquals(300, nodes[0].value.size)
        assertContentEquals(payload, nodes[0].value)
    }

    @Test
    fun parseMultiByteTag() {
        // Tag 0x7F49: first byte 0x7F has low 5 bits = 0x1F (multi-byte), second byte 0x49 has high bit = 0 (last)
        val payload = ubyteArrayOf(0xDEu, 0xADu)
        val wire = ubyteArrayOf(0x7Fu, 0x49u, 0x02u) + payload
        val nodes = wire.parseTlv().getOrThrow()
        assertEquals(1, nodes.size)
        assertEquals(0x7F49u, nodes[0].tag)
        assertContentEquals(payload, nodes[0].value)
    }

    @Test
    fun firstWithTagUIntFound() {
        val bytes = ubyteArrayOf(0x80u, 0x01u, 0xAAu, 0x83u, 0x01u, 0xBBu)
        val nodes = bytes.parseTlv().getOrThrow()
        val node = nodes.firstWithTag(0x83u).getOrThrow()
        assertContentEquals(ubyteArrayOf(0xBBu), node.value)
    }

    @Test
    fun firstWithTagUIntNotFound() {
        val bytes = ubyteArrayOf(0x80u, 0x01u, 0xAAu)
        val nodes = bytes.parseTlv().getOrThrow()
        assertTrue(nodes.firstWithTag(0x99u).isFailure)
    }

    @Test
    fun firstWithTagUByteFound() {
        val bytes = ubyteArrayOf(0x82u, 0x02u, 0x01u, 0x02u)
        val nodes = bytes.parseTlv().getOrThrow()
        assertTrue(nodes.firstWithTag(0x82u.toUByte()).isSuccess)
    }

    @Test
    fun firstWithTagUByteNotFound() {
        val bytes = ubyteArrayOf(0x80u, 0x01u, 0xAAu)
        val nodes = bytes.parseTlv().getOrThrow()
        assertTrue(nodes.firstWithTag(0x81u.toUByte()).isFailure)
    }

    @Test
    fun trailingZeroPaddingSkipped() {
        val bytes = ubyteArrayOf(0x80u, 0x01u, 0xAAu, 0x00u, 0x00u, 0x00u)
        val nodes = bytes.parseTlv().getOrThrow()
        assertEquals(1, nodes.size)
    }

    @Test
    fun allZeroBufferProducesEmptyList() {
        val bytes = ubyteArrayOf(0x00u, 0x00u, 0x00u)
        val nodes = bytes.parseTlv().getOrThrow()
        assertEquals(0, nodes.size)
    }

    @Test
    fun truncatedValueReturnsFailure() {
        val bytes = ubyteArrayOf(0x80u, 0x0Au, 0x01u, 0x02u)
        assertTrue(bytes.parseTlv().isFailure)
    }

    @Test
    fun tagAtEndWithNoLengthReturnsFailure() {
        val bytes = ubyteArrayOf(0x80u)
        assertTrue(bytes.parseTlv().isFailure)
    }

    @Test
    fun childrenParsesNestedTlvs() {
        val inner = ubyteArrayOf(0x80u, 0x01u, 0xAAu, 0x81u, 0x01u, 0xBBu)
        val outer = ubyteArrayOf(0x7Cu) + ubyteArrayOf(inner.size.toUByte()) + inner
        val nodes = outer.parseTlv().getOrThrow()
        assertEquals(1, nodes.size)
        val children = nodes[0].children().getOrThrow()
        assertEquals(2, children.size)
        assertEquals(0x80u, children[0].tag)
        assertEquals(0x81u, children[1].tag)
    }

    @Test
    fun emptyBuffer() {
        val nodes = ubyteArrayOf().parseTlv().getOrThrow()
        assertEquals(0, nodes.size)
    }

    @Test
    fun zeroLengthValue() {
        val bytes = ubyteArrayOf(0x80u, 0x00u)
        val nodes = bytes.parseTlv().getOrThrow()
        assertEquals(1, nodes.size)
        assertEquals(0, nodes[0].value.size)
    }
}
