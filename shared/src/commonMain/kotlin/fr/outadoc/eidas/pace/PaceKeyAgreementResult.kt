package fr.outadoc.eidas.pace

@OptIn(ExperimentalUnsignedTypes::class)
data class PaceKeyAgreementResult(
    val kEnc: UByteArray,
    val kMac: UByteArray,
    val terminalFinalPub: UByteArray,
    val chipFinalPub: UByteArray,
)
