package fr.outadoc.eidas.pace

import fr.outadoc.eidas.crypto.Algorithm

@OptIn(ExperimentalUnsignedTypes::class)
data class PaceSession(
    val algorithm: Algorithm,
    val kEnc: UByteArray,
    val kMac: UByteArray,
)
