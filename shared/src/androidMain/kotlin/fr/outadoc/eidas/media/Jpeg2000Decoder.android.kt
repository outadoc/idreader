package fr.outadoc.eidas.media

import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import com.gemalto.jp2.JP2Decoder

actual class Jpeg2000Decoder actual constructor(
    private val source: ImageSource,
) : Decoder {
    override suspend fun decode(): DecodeResult? {
        val bytes = source.source().readByteArray()
        val bitmap =
            JP2Decoder(bytes).decode()
                ?: error("Failed to decode JPEG2000 image")
        return DecodeResult(
            image = bitmap.asImage(),
            isSampled = false,
        )
    }
}
