package fr.outadoc.eidas.media

import coil3.decode.Decoder
import coil3.decode.ImageSource

expect class Jpeg2000Decoder(
    source: ImageSource,
) : Decoder
