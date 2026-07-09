package fr.outadoc.eidas.media

import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import com.gemalto.jp2.JP2Decoder
import okio.BufferedSource
import okio.ByteString
import okio.ByteString.Companion.toByteString

class Jpeg2000Decoder(
    private val source: ImageSource,
) : Decoder {
    override suspend fun decode(): DecodeResult {
        val bytes = source.source().readByteArray()
        val bitmap =
            JP2Decoder(bytes).decode()
                ?: error("Failed to decode JPEG2000 image")
        return DecodeResult(
            image = bitmap.asImage(),
            isSampled = false,
        )
    }

    class Factory : Decoder.Factory {
        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader,
        ): Decoder? {
            if (!isJpeg2000(result.source.source())) {
                return null
            }

            return Jpeg2000Decoder(
                source = result.source,
            )
        }

        fun isJpeg2000(source: BufferedSource): Boolean =
            source.rangeEquals(0, JP2_RFC3745_MAGIC) ||
                source.rangeEquals(0, JP2_MAGIC) ||
                source.rangeEquals(0, J2K_CODESTREAM_MAGIC)

        private companion object {
            private val JP2_RFC3745_MAGIC: ByteString =
                "0000000c6a5020200d0a870a".hexToByteArray().toByteString()

            private val JP2_MAGIC: ByteString =
                "0d0a870a".hexToByteArray().toByteString()

            private val J2K_CODESTREAM_MAGIC =
                "ff4fff51".hexToByteArray().toByteString()
        }
    }
}
