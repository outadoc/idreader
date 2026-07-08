@file:OptIn(ExperimentalUnsignedTypes::class)

package fr.outadoc.eidas.nfc

import fr.outadoc.eidas.tlv.TlvBuilder

fun tlvList(block: TlvListBuilder.() -> Unit): UByteArray = TlvListBuilder().apply(block).build()

class TlvListBuilder {
    private val inner = TlvBuilder()

    fun tlv(
        tag: UByte,
        value: UByteArray,
    ) = inner.tlv(tag, value)

    fun tlv(
        tag: UByte,
        value: ByteArray,
    ) = inner.tlv(tag, value.toUByteArray())

    fun tlv(
        tag: UByte,
        value: UByte,
    ) = inner.tlv(tag, value)

    fun build(): UByteArray = inner.build()
}
