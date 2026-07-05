package fr.outadoc.eidas.nfc

import io.github.rafaelrabeloit.bertlv.TLV
import io.github.rafaelrabeloit.bertlv.TLVList

fun tlvList(block: TlvListBuilder.() -> Unit): UByteArray = TlvListBuilder().apply(block).build()

class TlvListBuilder {
    private val tlvs = mutableListOf<TLV<*>>()

    fun tlv(
        tag: UByte,
        value: UByteArray,
    ) {
        tlvs += TLV.fromTagAndBinaryValue(tag.toInt(), value.toByteArray())
    }

    fun tlv(
        tag: UByte,
        value: UByte,
    ) {
        tlvs += TLV.fromTagAndBinaryValue(tag.toInt(), byteArrayOf(value.toByte()))
    }

    fun build(): UByteArray = TLVList.fromTlvs(tlvs).bytes.toUByteArray()
}
