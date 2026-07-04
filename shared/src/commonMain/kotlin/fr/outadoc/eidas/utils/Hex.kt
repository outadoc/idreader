package fr.outadoc.eidas.utils

fun String.parseHex(): ByteArray =
    chunked(2).map { it.toInt(16).toByte() }.toByteArray()
