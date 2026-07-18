@file:OptIn(ExperimentalUnsignedTypes::class)

package fr.outadoc.eidas.crypto

import fr.outadoc.eidas.utils.toKmpBytes

fun EcPoint.serializeUncompressed(): UByteArray = ubyteArrayOf(0x04u, *x.toUByteArray(), *y.toUByteArray())

fun deserializedUncompressedEcPoint(data: UByteArray): EcPoint {
    check(data.first() == 0x04u.toUByte()) {
        "EC point from chip does not start with expected prefix"
    }

    val coordSize: Int = (data.size - 1) / 2

    return EcPoint(
        x = data.copyOfRange(1, 1 + coordSize).toKmpBytes(),
        y = data.copyOfRange(1 + coordSize, 1 + coordSize * 2).toKmpBytes(),
    )
}
