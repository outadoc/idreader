package fr.outadoc.eidas.lds.model

@OptIn(ExperimentalUnsignedTypes::class)
data class DocumentPicture(
    val format: Format,
    val bytes: UByteArray,
) {
    enum class Format {
        Jpeg,
        Jpeg2000,
    }

    override fun toString(): String = "$format, ${bytes.size} bytes"
}
