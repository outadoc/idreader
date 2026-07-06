package fr.outadoc.eidas.pace

@OptIn(ExperimentalUnsignedTypes::class)
data class PaceSessionKeys(
    val kEnc: UByteArray,
    val kMac: UByteArray,
)
