package fr.outadoc.eidas.crypto

@OptIn(ExperimentalUnsignedTypes::class)
class AndroidPublicKey(private val point: EcPoint) : PublicKey {
    override val encoded: UByteArray
        get() = point.serializeUncompressed()

    override val uncompressedPublicPoint: UByteArray
        get() = point.serializeUncompressed()
}
