package fr.outadoc.eidas.media

import coil3.key.Keyer
import coil3.request.Options
import fr.outadoc.eidas.lds.model.DocumentPicture

@OptIn(ExperimentalUnsignedTypes::class)
class DocumentPictureKeyer : Keyer<DocumentPicture> {
    override fun key(
        data: DocumentPicture,
        options: Options,
    ): String = data.bytes.contentHashCode().toString()
}
