package fr.outadoc.eidas.pace.model

@OptIn(ExperimentalUnsignedTypes::class)
data class PaceKeyAgreementResult(
    val kEnc: UByteArray,
    val kMac: UByteArray,
    val terminalFinalPub: UByteArray,
    val chipFinalPub: UByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PaceKeyAgreementResult

        if (!kEnc.contentEquals(other.kEnc)) return false
        if (!kMac.contentEquals(other.kMac)) return false
        if (!terminalFinalPub.contentEquals(other.terminalFinalPub)) return false
        if (!chipFinalPub.contentEquals(other.chipFinalPub)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = kEnc.contentHashCode()
        result = 31 * result + kMac.contentHashCode()
        result = 31 * result + terminalFinalPub.contentHashCode()
        result = 31 * result + chipFinalPub.contentHashCode()
        return result
    }
}
