package fr.outadoc.eidas.crypto

import kotlin.uuid.Uuid

data class KeyPair(
    val alias: Uuid,
    val publicKey: PublicKey,
)
