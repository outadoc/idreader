package fr.outadoc.eidas.lds

import fr.outadoc.eidas.lds.model.DocumentPicture
import fr.outadoc.eidas.nfc.Icao9303
import fr.outadoc.eidas.tlv.firstWithTag
import fr.outadoc.eidas.tlv.parseTlv
import fr.outadoc.eidas.utils.flatMap

@OptIn(ExperimentalUnsignedTypes::class)
class ParseDG2UseCase {
    operator fun invoke(rawData: UByteArray): Result<DocumentPicture> =
        rawData
            .parseTlv()
            .flatMap { tagList -> tagList.firstWithTag(Icao9303.DataGroup.DG2.tag) }
            .flatMap { rootNode -> rootNode.value.parseTlv() }
            .flatMap { nodes -> nodes.firstWithTag(Icao9303.Tags.BiometricInformationTemplateGroupTemplate) }
            .flatMap { templateNode -> templateNode.value.parseTlv() }
            .flatMap { nodes -> nodes.firstWithTag(Icao9303.Tags.BiometricInformationTemplate) }
            .flatMap { biometricInformation -> biometricInformation.value.parseTlv() }
            .flatMap { nodes -> nodes.firstWithTag(Icao9303.Tags.BiometricData) }
            .flatMap { biometricData -> parseFacialRecord(record = biometricData.value) }

    /**
     * Extracts the encoded image from an ISO/IEC 19794-5 facial record.
     *
     * The biometric data block does not contain the image directly; it is
     * preceded by a facial record header, a facial information block, optional
     * feature points and an image information block.
     */
    private fun parseFacialRecord(record: UByteArray): Result<DocumentPicture> =
        runCatching {
            check(
                record.size >= FACIAL_RECORD_HEADER_LENGTH &&
                    record[0] == 0x46u.toUByte() &&
                    record[1] == 0x41u.toUByte() &&
                    record[2] == 0x43u.toUByte() &&
                    record[3] == 0x00u.toUByte(),
            ) {
                "Missing ISO/IEC 19794-5 facial record signature"
            }

            val featurePointCount: Int =
                (record[FACIAL_RECORD_HEADER_LENGTH + 4].toInt() shl 8) or
                    record[FACIAL_RECORD_HEADER_LENGTH + 5].toInt()

            val imageInformationOffset: Int =
                FACIAL_RECORD_HEADER_LENGTH +
                    FACIAL_INFORMATION_LENGTH +
                    featurePointCount * FEATURE_POINT_LENGTH

            val imageDataOffset: Int = imageInformationOffset + IMAGE_INFORMATION_LENGTH

            check(imageDataOffset < record.size) {
                "Facial record too short: expected image data at offset $imageDataOffset, but record is ${record.size} bytes"
            }

            val format: DocumentPicture.Format =
                when (val imageDataType = record[imageInformationOffset + 1].toInt()) {
                    0 -> DocumentPicture.Format.Jpeg
                    1 -> DocumentPicture.Format.Jpeg2000
                    else -> error("Unsupported image data type: $imageDataType")
                }

            DocumentPicture(
                format = format,
                bytes =
                    record.copyOfRange(
                        fromIndex = imageDataOffset,
                        toIndex = record.size,
                    ),
            )
        }

    private companion object {
        // Format identifier "FAC\0", version, record length, image count
        const val FACIAL_RECORD_HEADER_LENGTH = 14

        // Record data length, feature point count, gender, eye/hair colour,
        // feature mask, expression, pose angle, pose angle uncertainty
        const val FACIAL_INFORMATION_LENGTH = 20

        const val FEATURE_POINT_LENGTH = 8

        // Face image type, image data type, width, height, colour space,
        // source type, device type, quality
        const val IMAGE_INFORMATION_LENGTH = 12
    }
}
