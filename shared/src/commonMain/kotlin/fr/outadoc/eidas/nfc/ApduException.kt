package fr.outadoc.eidas.nfc

class ApduException(
    val sw1: UByte,
    val sw2: UByte,
) : Exception()
