package fr.outadoc.eidas.media

import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFRelease
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGColorSpaceRelease
import platform.CoreGraphics.CGContextDrawImage
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGImageRef
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGRectMake
import platform.ImageIO.CGImageSourceCreateImageAtIndex
import platform.ImageIO.CGImageSourceCreateWithData

@OptIn(ExperimentalForeignApi::class)
actual class Jpeg2000Decoder actual constructor(
    private val source: ImageSource,
) : Decoder {
    override suspend fun decode(): DecodeResult? {
        val bytes = source.source().readByteArray()
        val bitmap =
            decodeJpeg2000(bytes)
                ?: error("Failed to decode JPEG2000 image")
        return DecodeResult(
            image = bitmap.asImage(),
            isSampled = false,
        )
    }

    // ImageIO natively decodes JPEG2000 (public.jpeg-2000), unlike Skia's
    // built-in decoder used elsewhere by Coil on non-Android targets.
    private fun decodeJpeg2000(bytes: ByteArray): Bitmap? {
        val ubytes = bytes.toUByteArray()
        val data =
            CFDataCreate(null, ubytes.refTo(0), ubytes.size.toLong())
                ?: return null
        try {
            val imageSource = CGImageSourceCreateWithData(data, null) ?: return null
            try {
                val cgImage = CGImageSourceCreateImageAtIndex(imageSource, 0uL, null) ?: return null
                try {
                    return cgImageToBitmap(cgImage)
                } finally {
                    CGImageRelease(cgImage)
                }
            } finally {
                CFRelease(imageSource)
            }
        } finally {
            CFRelease(data)
        }
    }

    private fun cgImageToBitmap(image: CGImageRef): Bitmap {
        val width = CGImageGetWidth(image)
        val height = CGImageGetHeight(image)
        val bytesPerRow = width * 4uL
        val pixels = ByteArray((bytesPerRow * height).toInt())

        val colorSpace = CGColorSpaceCreateDeviceRGB()
        try {
            pixels.usePinned { pinned ->
                val context =
                    CGBitmapContextCreate(
                        data = pinned.addressOf(0),
                        width = width,
                        height = height,
                        bitsPerComponent = 8uL,
                        bytesPerRow = bytesPerRow,
                        space = colorSpace,
                        bitmapInfo = CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value,
                    ) ?: error("Failed to create bitmap context")
                try {
                    CGContextDrawImage(
                        context,
                        CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()),
                        image,
                    )
                } finally {
                    CGContextRelease(context)
                }
            }
        } finally {
            CGColorSpaceRelease(colorSpace)
        }

        return Bitmap().apply {
            installPixels(
                info =
                    ImageInfo(
                        ColorInfo(ColorType.RGBA_8888, ColorAlphaType.PREMUL, null),
                        width.toInt(),
                        height.toInt(),
                    ),
                pixels = pixels,
                rowBytes = bytesPerRow.toInt(),
            )
            setImmutable()
        }
    }
}
