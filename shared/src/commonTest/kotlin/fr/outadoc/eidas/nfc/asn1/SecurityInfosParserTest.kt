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
    private val derBytes =
        "31283012060A04007F0007020204020402010202010D3012060A04007F0007020204040402010202010D0000000000000000"
            .hexToUByteArray()

    @Test
    fun parsesExactlyTwoEntries() {
        val result = parser.parse(derBytes.toUByteArray()).getOrThrow()
        assertEquals(2, result.size)
    }

    @Test
    fun parsesFirstPaceInfo() {
        val pace = parser.parse(derBytes.toUByteArray()).getOrThrow()[0] as SecurityInfo.Pace
        assertEquals(ObjectIdentifier("0.4.0.127.0.7.2.2.4.2.4"), pace.protocol)
        assertEquals(2, pace.version)
        assertEquals(13, pace.parameterId)
    }

    @Test
    fun parsesSecondPaceInfo() {
        val pace = parser.parse(derBytes.toUByteArray()).getOrThrow()[1] as SecurityInfo.Pace
        assertEquals(ObjectIdentifier("0.4.0.127.0.7.2.2.4.4.4"), pace.protocol)
        assertEquals(2, pace.version)
        assertEquals(13, pace.parameterId)
    }

    @Test
    fun ignoresTrailingPaddingBytes() {
        // Padding must not produce extra entries
        val result = parser.parse(derBytes.toUByteArray()).getOrThrow()
        assertEquals(2, result.size)
    }

    @Test
    fun parsesEntryWithNoParameterId() {
        // First entry has no parameterId; second still does.
        // SET (37 = 0x25 bytes)
        //   SEQUENCE (15 = 0x0F bytes): OID + version only
        //   SEQUENCE (18 = 0x12 bytes): OID + version + parameterId
        val derWithoutParamId =
            "3125300F060A04007F000702020402040201023012060A04007F0007020204040402010202010D"
                .hexToUByteArray()

        val result = parser.parse(derWithoutParamId).getOrThrow()
        val first = result[0] as SecurityInfo.Pace
        assertNull(first.parameterId)
        val second = result[1] as SecurityInfo.Pace
        assertEquals(13, second.parameterId)
    }
}
