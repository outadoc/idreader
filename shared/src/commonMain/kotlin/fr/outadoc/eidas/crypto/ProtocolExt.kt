package fr.outadoc.eidas.crypto

import at.asitplus.signum.indispensable.asn1.ObjectIdentifier

@OptIn(ExperimentalUnsignedTypes::class)
val Protocol.oidBytes: UByteArray
    get() = ObjectIdentifier(oid).bytes.toUByteArray()
