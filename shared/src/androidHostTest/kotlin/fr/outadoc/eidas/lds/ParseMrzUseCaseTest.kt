package fr.outadoc.eidas.lds

import fr.outadoc.eidas.lds.model.CardHolderName
import fr.outadoc.eidas.lds.model.Date
import fr.outadoc.eidas.lds.model.MrzInfo
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

class ParseMrzUseCaseTest {
    private val fixedClock =
        object : Clock {
            override fun now(): Instant = Instant.parse("2010-01-01T00:00:00Z")
        }

    private val parseMrz =
        ParseMrzUseCase()

    @Test
    fun `Parse TD1 MRZ from FR specimen`() {
        val mrz =
            "IDFRAX4RTBPFW46<<<<<<<<<<<<<<<9007138F3002119FRA<<<<<<<<<<<6MARTIN<<MAELYS<GAELLE<MARIE<<<"

        val parsed = parseMrz(mrz).getOrThrow()

        assertEquals(
            expected =
                MrzInfo(
                    format = "TD1",
                    documentCode = "ID",
                    documentNumber = "X4RTBPFW4",
                    issuingState = "FRA",
                    nationality = "FRA",
                    cardHolderName =
                        CardHolderName(
                            surname = "MARTIN",
                            givenNames =
                                persistentListOf(
                                    "MAELYS",
                                    "GAELLE",
                                    "MARIE",
                                ),
                        ),
                    birthDate =
                        Date.from(
                            year = 1990,
                            month = 7,
                            day = 13,
                        ),
                    sex = "F",
                    expiryDate =
                        Date.from(
                            year = 2030,
                            month = 2,
                            day = 11,
                        ),
                    optionalData1 = null,
                    optionalData2 = null,
                ),
            actual = parsed,
        )
    }

    @Test
    fun `Parse TD1 MRZ from ISAO example`() {
        val mrz =
            "I<NLDXI85935F86999999990<<<<<<7208148F1108268NLD<<<<<<<<<<<4VAN<DER<STEEN<<MARIANNE<LOUISE"

        val parsed = parseMrz(mrz).getOrThrow()

        assertEquals(
            expected =
                MrzInfo(
                    format = "TD1",
                    documentCode = "I<",
                    documentNumber = "XI85935F8",
                    issuingState = "NLD",
                    nationality = "NLD",
                    cardHolderName =
                        CardHolderName(
                            surname = "VAN DER STEEN",
                            givenNames =
                                persistentListOf(
                                    "MARIANNE",
                                    "LOUISE",
                                ),
                        ),
                    birthDate =
                        Date.from(
                            year = 1972,
                            month = 8,
                            day = 14,
                        ),
                    sex = "F",
                    expiryDate =
                        Date.from(
                            year = 2011,
                            month = 8,
                            day = 26,
                        ),
                    optionalData1 = "999999990",
                    optionalData2 = null,
                ),
            actual = parsed,
        )
    }
}
