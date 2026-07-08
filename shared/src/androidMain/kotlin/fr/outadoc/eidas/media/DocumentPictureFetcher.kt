package fr.outadoc.eidas.media

import android.util.Log
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.request.Options
import com.gemalto.jp2.JP2Decoder
import fr.outadoc.eidas.lds.model.DocumentPicture
import kotlin.io.encoding.Base64

@OptIn(ExperimentalUnsignedTypes::class)
class DocumentPictureFetcher(
    private val picture: DocumentPicture,
) : Fetcher {
    override suspend fun fetch(): FetchResult? {
        val bytes = picture.bytes.toByteArray()
        val bitmap = JP2Decoder(bytes).decode()

        Log.d("trx", Base64.encode(bytes))

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
