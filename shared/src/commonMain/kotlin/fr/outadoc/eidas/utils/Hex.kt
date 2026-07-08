@file:OptIn(ExperimentalUnsignedTypes::class)

package fr.outadoc.eidas.utils

private val hexFormat =
    HexFormat {
        upperCase = true
        bytes {
            byteSeparator = " "
        }
    }

fun UByteArray.toPrettyHex(): String = toHexString(hexFormat)

fun UByte.toPrettyHex(): String = toHexString(hexFormat)
