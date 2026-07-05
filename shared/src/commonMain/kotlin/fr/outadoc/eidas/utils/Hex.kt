package fr.outadoc.eidas.utils

fun String.parseHex(): UByteArray =
    chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
        .toUByteArray()

fun UByteArray.toPrettyHex(): String =
    toHexString(
        HexFormat {
            upperCase = true
            bytes {
                byteSeparator = " "
            }
        },
    )
