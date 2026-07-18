package fr.outadoc.eidas.crypto

import fr.outadoc.eidas.utils.KmpBytes

data class EcPoint(
    val x: KmpBytes,
    val y: KmpBytes,
)
