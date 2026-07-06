package fr.outadoc.eidas.pace

import fr.outadoc.eidas.nfc.Iso7816
import io.github.rafaelrabeloit.bertlv.TLVList

@OptIn(ExperimentalUnsignedTypes::class)
internal fun UByteArray.parseDynamicAuthData(): Result<TLVList> {
    val outer =
        TLVList
            .fromTlvListBuffer(this.toByteArray())
            .find(Iso7816.Tags.DynamicAuthenticationData.toInt())
            ?.value as? TLVList

    return if (outer == null) {
        Result.failure(
            IllegalStateException("Could not find dynamic auth data in response"),
        )
    } else {
        Result.success(outer)
    }
}
