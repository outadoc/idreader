package fr.outadoc.eidas.crypto

import fr.outadoc.eidas.utils.KmpBytes

interface PrivateKey {
    val encoded: KmpBytes
}
