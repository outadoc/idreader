package fr.outadoc.eidas.utils

fun String.parseHex(): ByteArray =
    chunked(2).map { it.toInt(16).toByte() }.toByteArray()

fun ByteArray.toPrettyHex(): String {
    return toHexString(
        HexFormat {
            upperCase = true
            bytes {
                byteSeparator = " "
            }
        }
    )
}
