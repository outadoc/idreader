package fr.outadoc.eidas.crypto

@OptIn(ExperimentalUnsignedTypes::class)
data class EcPoint(
    val x: UByteArray,
    val y: UByteArray,
)
