package fr.outadoc.eidas.nfc

class CApdu(
    val cla: UByte,
    val ins: UByte,
    val p1: UByte,
    val p2: UByte,
    val data: UByteArray,
    val le: UByte?,
) {
    fun serialize(): UByteArray =
        ubyteArrayOf(
            cla,
            ins,
            p1,
            p2,
            data.size.toUByte(),
            *data,
            *if (le != null) {
                ubyteArrayOf(le)
            } else {
                ubyteArrayOf()
            },
        )
}
