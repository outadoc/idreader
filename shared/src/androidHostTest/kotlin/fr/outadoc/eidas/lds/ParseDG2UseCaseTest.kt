package fr.outadoc.eidas.lds

import fr.outadoc.eidas.lds.model.DocumentPicture
import fr.outadoc.eidas.nfc.Icao9303
import fr.outadoc.eidas.tlv.buildTlv
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalUnsignedTypes::class)
class ParseDG2UseCaseTest {
    @Test
    fun `Extracts JPEG2000 image data from the facial record`() {
        val imageData = ubyteArrayOf(0xFFu, 0x4Fu, 0xFFu, 0x51u, 0x12u, 0x34u)

        val facialRecord =
            buildList {
                // Facial record header
                addAll(listOf(0x46u, 0x41u, 0x43u, 0x00u)) // "FAC\0"
                addAll(listOf(0x30u, 0x31u, 0x30u, 0x00u)) // version "010\0"
                addAll(listOf(0x00u, 0x00u, 0x00u, 0x00u)) // record length
                addAll(listOf(0x00u, 0x01u)) // number of facial images

                // Facial information
                addAll(listOf(0x00u, 0x00u, 0x00u, 0x00u)) // record data length
                addAll(listOf(0x00u, 0x01u)) // number of feature points
                addAll(List(14) { 0x00u }) // gender, colours, mask, expression, pose

                // One feature point
                addAll(List(8) { 0x00u })

                // Image information
                add(0x01u) // face image type
                add(0x01u) // image data type: JPEG2000
                addAll(List(10) { 0x00u }) // dimensions, colour space, source, quality

                addAll(imageData.toList())
            }.map { it.toUByte() }.toUByteArray()

        val dg2 =
            buildTlv {
                constructed(tag = Icao9303.Tags.DG2) {
                    constructed(tag = 0x7F61u) {
                        constructed(tag = 0x7F60u) {
                            tlv(tag = 0x5F2Eu, value = facialRecord)
                        }
                    }
                }
            }

        val parsed = ParseDG2UseCase().invoke(rawData = dg2).getOrThrow()

        assertEquals(
            expected = DocumentPicture.Format.Jpeg2000,
            actual = parsed.format,
        )
        assertContentEquals(
            expected = imageData,
            actual = parsed.bytes,
        )
    }

    @Test
    fun `Fails on a biometric data block without a facial record signature`() {
        val dg2 =
            buildTlv {
                constructed(tag = Icao9303.Tags.DG2) {
                    constructed(tag = 0x7F61u) {
                        constructed(tag = 0x7F60u) {
                            tlv(tag = 0x5F2Eu, value = ubyteArrayOf(0xFFu, 0x4Fu, 0xFFu, 0x51u))
                        }
                    }
                }
            }

        val result = ParseDG2UseCase().invoke(rawData = dg2)

        assertTrue(result.isFailure)
    }
}
