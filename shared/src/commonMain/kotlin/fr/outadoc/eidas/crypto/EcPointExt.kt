@file:OptIn(ExperimentalUnsignedTypes::class)

package fr.outadoc.eidas.crypto

fun EcPoint.serializeUncompressed(): UByteArray = ubyteArrayOf(0x04u, *x, *y)

fun deserializedUncompressedEcPoint(data: UByteArray): EcPoint {
    check(data.first() == 0x04u.toUByte()) {
        "EC point from chip does not start with expected prefix"
    }

    val coordSize: Int = (data.size - 1) / 2

    return EcPoint(
        x = data.copyOfRange(1, coordSize),
        y = data.copyOfRange(coordSize + 1, coordSize * 2 + 1),
    )
}
