@file:OptIn(ExperimentalUnsignedTypes::class)

package fr.outadoc.eidas.pace

import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.tlv.TlvNode
import fr.outadoc.eidas.tlv.firstWithTag
import fr.outadoc.eidas.tlv.parseTlv

internal fun UByteArray.parseDynamicAuthData(): Result<List<TlvNode>> {
    val outer: List<TlvNode> =
        parseTlv().getOrElse {
            return Result.failure(it)
        }

    val node: TlvNode =
        outer.firstWithTag(Iso7816.Tags.DynamicAuthenticationData)
            ?: return Result.failure(
                IllegalStateException("Could not find dynamic auth data in response"),
            )

    return node.children()
}
