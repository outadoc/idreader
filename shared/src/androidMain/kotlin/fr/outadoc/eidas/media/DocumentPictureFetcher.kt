package fr.outadoc.eidas.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.request.Options
import com.gemalto.jp2.JP2Decoder
import fr.outadoc.eidas.lds.model.DocumentPicture

@OptIn(ExperimentalUnsignedTypes::class)
class DocumentPictureFetcher(
    private val picture: DocumentPicture,
) : Fetcher {
    override suspend fun fetch(): FetchResult {
        val bytes = picture.bytes.toByteArray()

        val bitmap: Bitmap =
            when (picture.format) {
                DocumentPicture.Format.Jpeg -> {
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }

                DocumentPicture.Format.Jpeg2000 -> {
                    JP2Decoder(bytes).decode()
                }
            } ?: error("Failed to decode document picture as ${picture.format}")

        return ImageFetchResult(
            image = bitmap.asImage(),
            isSampled = false,
            dataSource = DataSource.MEMORY,
        )
    }

    class Factory : Fetcher.Factory<DocumentPicture> {
        override fun create(
            data: DocumentPicture,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher =
            DocumentPictureFetcher(
                picture = data,
            )
    }
}
