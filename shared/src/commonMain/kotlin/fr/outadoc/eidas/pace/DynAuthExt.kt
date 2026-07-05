package fr.outadoc.eidas.pace

import fr.outadoc.eidas.nfc.Iso7816
import io.github.rafaelrabeloit.bertlv.TLVList

@OptIn(ExperimentalUnsignedTypes::class)
internal fun UByteArray.parseDynamicAuthData(): TLVList {
    val outer =
        TLVList
            .fromTlvListBuffer(this.toByteArray())
            .find(Iso7816.Tags.DynamicAuthenticationData.toInt())
            ?.value as? TLVList
    return checkNotNull(outer) { "Could not find dynamic auth data in response" }
}
