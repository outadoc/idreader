package fr.outadoc.eidas.nfc.commands

import fr.outadoc.eidas.nfc.CApdu

/**
 * The command MSE:Set AT is used to select and initialize the following protocols: PACE, Chip
 * Authentication, Terminal Authentication, and Restricted Identification.
 */

class MseSetAtUseCase {
    companion object {
        val INS: UByte = 0x22u
        val PACE_P1: UByte = 0xC1u
        val PACE_P2: UByte = 0xA4u
    }

    fun paceSetAt(): CApdu {
        TODO()
    }
}
