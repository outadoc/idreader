package fr.outadoc.eidas.crypto

@OptIn(ExperimentalUnsignedTypes::class)
interface PublicKey {
    val encoded: UByteArray
    val uncompressedPublicPoint: UByteArray
}
