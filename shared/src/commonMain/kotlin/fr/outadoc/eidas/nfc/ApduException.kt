package fr.outadoc.eidas.nfc

import fr.outadoc.eidas.utils.toPrettyHex

class ApduException(
    val sw1: UByte,
    val sw2: UByte,
) : Exception("Chip returned SW ${sw1.toPrettyHex()} ${sw2.toPrettyHex()}")
