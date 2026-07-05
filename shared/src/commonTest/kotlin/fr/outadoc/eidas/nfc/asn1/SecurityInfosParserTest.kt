package fr.outadoc.eidas.nfc.asn1

import at.asitplus.signum.indispensable.asn1.ObjectIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SecurityInfosParserTest {

    private val parser = SecurityInfosParser()

    // 31 28  SET (40 bytes)
    //   30 12  SEQUENCE (18 bytes)
    //     06 0A 04 00 7F 00 07 02 02 04 02 04  OID  0.4.0.127.0.7.2.2.4.2.4 (id-PACE-ECDH-GM-AES-CBC-CMAC-256)
    //     02 01 02  INTEGER 2  (version)
    //     02 01 0D  INTEGER 13 (parameterId)
    //   30 12  SEQUENCE (18 bytes)
    //     06 0A 04 00 7F 00 07 02 02 04 04 04  OID  0.4.0.127.0.7.2.2.4.4.4 (id-PACE-ECDH-IM-AES-CBC-CMAC-256)
    //     02 01 02  INTEGER 2  (version)
    //     02 01 0D  INTEGER 13 (parameterId)
    // 00 00 00 00 00 00 00 00  trailing file padding
    private val derBytes = byteArrayOf(
        0x31, 0x28,
            0x30, 0x12,
                0x06, 0x0A, 0x04, 0x00, 0x7F.toByte(), 0x00, 0x07, 0x02, 0x02, 0x04, 0x02, 0x04,
                0x02, 0x01, 0x02,
                0x02, 0x01, 0x0D,
            0x30, 0x12,
                0x06, 0x0A, 0x04, 0x00, 0x7F.toByte(), 0x00, 0x07, 0x02, 0x02, 0x04, 0x04, 0x04,
                0x02, 0x01, 0x02,
                0x02, 0x01, 0x0D,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    )

    @Test
    fun parsesExactlyTwoEntries() {
        val result = parser.parse(derBytes)
        assertEquals(2, result.size)
    }

    @Test
    fun parsesFirstPaceInfo() {
        val pace = parser.parse(derBytes)[0] as SecurityInfo.Pace
        assertEquals(ObjectIdentifier("0.4.0.127.0.7.2.2.4.2.4"), pace.protocol)
        assertEquals(2, pace.version)
        assertEquals(13, pace.parameterId)
    }

    @Test
    fun parsesSecondPaceInfo() {
        val pace = parser.parse(derBytes)[1] as SecurityInfo.Pace
        assertEquals(ObjectIdentifier("0.4.0.127.0.7.2.2.4.4.4"), pace.protocol)
        assertEquals(2, pace.version)
        assertEquals(13, pace.parameterId)
    }

    @Test
    fun ignoresTrailingPaddingBytes() {
        // Padding must not produce extra entries
        val result = parser.parse(derBytes)
        assertEquals(2, result.size)
    }

    @Test
    fun parsesEntryWithNoParameterId() {
        // First entry has no parameterId; second still does.
        // SET (37 = 0x25 bytes)
        //   SEQUENCE (15 = 0x0F bytes): OID + version only
        //   SEQUENCE (18 = 0x12 bytes): OID + version + parameterId
        val derWithoutParamId = byteArrayOf(
            0x31, 0x25,
                0x30, 0x0F,
                    0x06, 0x0A, 0x04, 0x00, 0x7F.toByte(), 0x00, 0x07, 0x02, 0x02, 0x04, 0x02, 0x04,
                    0x02, 0x01, 0x02,
                0x30, 0x12,
                    0x06, 0x0A, 0x04, 0x00, 0x7F.toByte(), 0x00, 0x07, 0x02, 0x02, 0x04, 0x04, 0x04,
                    0x02, 0x01, 0x02,
                    0x02, 0x01, 0x0D,
        )
        val result = parser.parse(derWithoutParamId)
        val first = result[0] as SecurityInfo.Pace
        assertNull(first.parameterId)
        val second = result[1] as SecurityInfo.Pace
        assertEquals(13, second.parameterId)
    }
}
