package fr.outadoc.eidas.pace

@OptIn(ExperimentalUnsignedTypes::class)
data class PaceSession(
    val kEnc: UByteArray,
    val kMac: UByteArray,
)
