@file:OptIn(ExperimentalUnsignedTypes::class)

package fr.outadoc.eidas.tlv

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class TlvBuilderTest {
    @Test
    fun buildSingleTlvWithUIntTag() {
        val wire = buildTlv { tlv(0x80u, ubyteArrayOf(0x01u, 0x02u)) }
        assertContentEquals(ubyteArrayOf(0x80u, 0x02u, 0x01u, 0x02u), wire)
    }

    @Test
    fun buildSingleTlvWithUByteTag() {
        val wire = buildTlv { tlv(0x80u.toUByte(), ubyteArrayOf(0xAAu)) }
        assertContentEquals(ubyteArrayOf(0x80u, 0x01u, 0xAAu), wire)
    }

    @Test
    fun buildSingleByteValue() {
        val wire = buildTlv { tlv(0x83u, 0x02u.toUByte()) }
        assertContentEquals(ubyteArrayOf(0x83u, 0x01u, 0x02u), wire)
    }

    @Test
    fun buildMultipleTlvs() {
        val wire = buildTlv {
            tlv(0x80u, ubyteArrayOf(0xAAu))
            tlv(0x81u, ubyteArrayOf(0xBBu))
        }
        assertContentEquals(ubyteArrayOf(0x80u, 0x01u, 0xAAu, 0x81u, 0x01u, 0xBBu), wire)
    }

    @Test
    fun buildConstructedTlv() {
        val wire = buildTlv {
            constructed(0x7Cu) {
                tlv(0x80u, ubyteArrayOf(0x01u))
                tlv(0x81u, ubyteArrayOf(0x02u))
            }
        }
        val expected = ubyteArrayOf(
            0x7Cu, 0x06u,
            0x80u, 0x01u, 0x01u,
            0x81u, 0x01u, 0x02u,
        )
        assertContentEquals(expected, wire)
    }

    @Test
    fun buildTwoByteTag() {
        // 0x7F49 encodes as two bytes: 7F 49
        val wire = buildTlv { tlv(0x7F49u, ubyteArrayOf(0xAAu)) }
        assertContentEquals(ubyteArrayOf(0x7Fu, 0x49u, 0x01u, 0xAAu), wire)
    }

    @Test
    fun buildLongFormLengthAutoSelected() {
        val payload = UByteArray(200) { it.toUByte() }
        val wire = buildTlv { tlv(0x80u, payload) }
        // Length 200 = 0xC8 → encoded as 0x81 0xC8
        assertEquals(0x80u.toUByte(), wire[0])
        assertEquals(0x81u.toUByte(), wire[1])
        assertEquals(0xC8u.toUByte(), wire[2])
        assertEquals(200, wire.size - 3)
    }

    @Test
    fun buildLongFormTwoByteLength() {
        val payload = UByteArray(300) { it.toUByte() }
        val wire = buildTlv { tlv(0x80u, payload) }
        // Length 300 = 0x012C → encoded as 0x82 0x01 0x2C
        assertEquals(0x80u.toUByte(), wire[0])
        assertEquals(0x82u.toUByte(), wire[1])
        assertEquals(0x01u.toUByte(), wire[2])
        assertEquals(0x2Cu.toUByte(), wire[3])
    }

    @Test
    fun roundTripSingleTlv() {
        val payload = ubyteArrayOf(0xDEu, 0xADu, 0xBEu, 0xEFu)
        val wire = buildTlv { tlv(0x80u, payload) }
        val nodes = wire.parseTlv().getOrThrow()
        assertEquals(1, nodes.size)
        assertEquals(0x80u, nodes[0].tag)
        assertContentEquals(payload, nodes[0].value)
    }

    @Test
    fun roundTripMultiByteTag() {
        val payload = ubyteArrayOf(0x01u, 0x02u)
        val wire = buildTlv { tlv(0x7F49u, payload) }
        val nodes = wire.parseTlv().getOrThrow()
        assertEquals(1, nodes.size)
        assertEquals(0x7F49u, nodes[0].tag)
        assertContentEquals(payload, nodes[0].value)
    }

    @Test
    fun roundTripConstructed() {
        val wire = buildTlv {
            constructed(0x7Cu) {
                tlv(0x80u, ubyteArrayOf(0x01u))
                tlv(0x81u, ubyteArrayOf(0x02u, 0x03u))
            }
        }
        val outer = wire.parseTlv().getOrThrow()
        assertEquals(1, outer.size)
        assertEquals(0x7Cu, outer[0].tag)
        val inner = outer[0].children().getOrThrow()
        assertEquals(2, inner.size)
        assertEquals(0x80u, inner[0].tag)
        assertContentEquals(ubyteArrayOf(0x01u), inner[0].value)
        assertEquals(0x81u, inner[1].tag)
        assertContentEquals(ubyteArrayOf(0x02u, 0x03u), inner[1].value)
    }

    @Test
    fun roundTripLongFormLength() {
        val payload = UByteArray(200) { it.toUByte() }
        val wire = buildTlv { tlv(0x7Cu, payload) }
        val nodes = wire.parseTlv().getOrThrow()
        assertEquals(1, nodes.size)
        assertContentEquals(payload, nodes[0].value)
    }

    @Test
    fun emptyBuilder() {
        val wire = buildTlv { }
        assertEquals(0, wire.size)
    }

    @Test
    fun zeroLengthValue() {
        val wire = buildTlv { tlv(0x80u, ubyteArrayOf()) }
        assertContentEquals(ubyteArrayOf(0x80u, 0x00u), wire)
    }
}
