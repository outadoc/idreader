package fr.outadoc.eidas.crypto

import fr.outadoc.eidas.utils.KmpBytes
import fr.outadoc.eidas.utils.toKmpBytes

@OptIn(ExperimentalUnsignedTypes::class)
class AndroidPublicKey(
    private val point: EcPoint,
) : PublicKey {
    override val encoded: KmpBytes
        get() = point.serializeUncompressed().toKmpBytes()

    override val uncompressedPublicPoint: KmpBytes
        get() = point.serializeUncompressed().toKmpBytes()
}
