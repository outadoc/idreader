package fr.outadoc.eidas.pace.model

import fr.outadoc.eidas.crypto.Algorithm

@OptIn(ExperimentalUnsignedTypes::class)
data class PaceCredentials(
    val algorithm: Algorithm,
    val kEnc: UByteArray,
    val kMac: UByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PaceCredentials

        if (algorithm != other.algorithm) return false
        if (!kEnc.contentEquals(other.kEnc)) return false
        if (!kMac.contentEquals(other.kMac)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = algorithm.hashCode()
        result = 31 * result + kEnc.contentHashCode()
        result = 31 * result + kMac.contentHashCode()
        return result
    }
}
