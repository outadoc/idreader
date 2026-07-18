package fr.outadoc.eidas.media

import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import fr.outadoc.eidas.lds.model.DocumentPicture
import okio.Buffer

@OptIn(ExperimentalUnsignedTypes::class)
class DocumentPictureFetcher(
    private val picture: DocumentPicture,
    private val options: Options,
) : Fetcher {
    override suspend fun fetch(): FetchResult =
        SourceFetchResult(
            source =
                ImageSource(
                    source = Buffer().apply { write(picture.bytes.toByteArray()) },
                    fileSystem = options.fileSystem,
                ),
            mimeType =
                when (picture.format) {
                    DocumentPicture.Format.Jpeg -> "image/jpeg"
                    DocumentPicture.Format.Jpeg2000 -> "image/jp2"
                },
            dataSource = DataSource.MEMORY,
        )

    class Factory : Fetcher.Factory<DocumentPicture> {
        override fun create(
            data: DocumentPicture,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher =
            DocumentPictureFetcher(
                picture = data,
                options = options,
            )
    }
}
