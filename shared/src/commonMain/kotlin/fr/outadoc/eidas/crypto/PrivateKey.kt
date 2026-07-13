package fr.outadoc.eidas.crypto

@OptIn(ExperimentalUnsignedTypes::class)
interface PrivateKey {
    val encoded: UByteArray
}
