package fr.outadoc.eidas.nfc

class CApdu(
    val cla: UByte,
    val ins: UByte,
    val p1: UByte,
    val p2: UByte,
    val data: UByteArray? = null,
    val le: UByte?,
) {
    fun serialize(): UByteArray =
        ubyteArrayOf(
            cla,
            ins,
            p1,
            p2,
            *if (data != null) {
                ubyteArrayOf(data.size.toUByte())
            } else {
                ubyteArrayOf()
            },
            *(data ?: ubyteArrayOf()),
            *if (le != null) {
                ubyteArrayOf(le)
            } else {
                ubyteArrayOf()
            },
        )
}
