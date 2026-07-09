package fr.outadoc.eidas.lds

import fr.outadoc.eidas.lds.model.Date
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Clock
import kotlin.time.Instant

class Parse6DigitDateUseCaseTest {
    private val fixedClock =
        object : Clock {
            override fun now(): Instant = Instant.parse("2030-06-15T12:00:00Z")
        }

    private val parse6DigitDate =
        Parse6DigitDateUseCase(
            clock = fixedClock,
            timeZone = TimeZone.UTC,
        )

    @Test
    fun `Past date with year after current year is in previous century`() {
        val parsed =
            parse6DigitDate(
                date = "950713",
                dateIsIn = Parse6DigitDateUseCase.DateIsIn.PAST,
            ).getOrThrow()

        assertEquals(
            expected =
                Date.Exact(
                    date =
                        LocalDate(
                            year = 1995,
                            month = 7,
                            day = 13,
                        ),
                ),
            actual = parsed,
        )
    }

    @Test
    fun `Past date with year before current year is in current century`() {
        val parsed =
            parse6DigitDate(
                date = "030713",
                dateIsIn = Parse6DigitDateUseCase.DateIsIn.PAST,
            ).getOrThrow()

        assertEquals(
            expected =
                Date.Exact(
                    date =
                        LocalDate(
                            year = 2003,
                            month = 7,
                            day = 13,
                        ),
                ),
            actual = parsed,
        )
    }

    @Test
    fun `Past date with year equal to current year is in current century`() {
        val parsed =
            parse6DigitDate(
                date = "300713",
                dateIsIn = Parse6DigitDateUseCase.DateIsIn.PAST,
            ).getOrThrow()

        assertEquals(
            expected =
                Date.Exact(
                    date =
                        LocalDate(
                            year = 2030,
                            month = 7,
                            day = 13,
                        ),
                ),
            actual = parsed,
        )
    }

    @Test
    fun `Future date with year after current year is in current century`() {
        val parsed =
            parse6DigitDate(
                date = "310211",
                dateIsIn = Parse6DigitDateUseCase.DateIsIn.FUTURE,
            ).getOrThrow()

        assertEquals(
            expected =
                Date.Exact(
                    date =
                        LocalDate(
                            year = 2031,
                            month = 2,
                            day = 11,
                        ),
                ),
            actual = parsed,
        )
    }

    @Test
    fun `Future date with year before current year is in next century`() {
        val parsed =
            parse6DigitDate(
                date = "290211",
                dateIsIn = Parse6DigitDateUseCase.DateIsIn.FUTURE,
            ).getOrThrow()

        assertEquals(
            expected =
                Date.Exact(
                    date =
                        LocalDate(
                            year = 2129,
                            month = 2,
                            day = 11,
                        ),
                ),
            actual = parsed,
        )
    }

    @Test
    fun `Future date with year equal to current year is in current century`() {
        val parsed =
            parse6DigitDate(
                date = "300211",
                dateIsIn = Parse6DigitDateUseCase.DateIsIn.FUTURE,
            ).getOrThrow()

        assertEquals(
            expected =
                Date.Exact(
                    date =
                        LocalDate(
                            year = 2030,
                            month = 2,
                            day = 11,
                        ),
                ),
            actual = parsed,
        )
    }

    @Test
    fun `All-zero date is fully approximate`() {
        val parsed =
            parse6DigitDate(
                date = "000000",
                dateIsIn = Parse6DigitDateUseCase.DateIsIn.PAST,
            ).getOrThrow()

        assertEquals(
            expected =
                Date.Approx(
                    year = null,
                    month = null,
                    day = null,
                ),
            actual = parsed,
        )
    }

    @Test
    fun `Century is guessed even when month and day are unknown`() {
        val parsed =
            parse6DigitDate(
                date = "990000",
                dateIsIn = Parse6DigitDateUseCase.DateIsIn.PAST,
            ).getOrThrow()

        assertEquals(
            expected =
                Date.Approx(
                    year = 1999,
                    month = null,
                    day = null,
                ),
            actual = parsed,
        )
    }

    @Test
    fun `Date with wrong length fails`() {
        val result =
            parse6DigitDate(
                date = "12345",
                dateIsIn = Parse6DigitDateUseCase.DateIsIn.PAST,
            )

        assertIs<IllegalArgumentException>(result.exceptionOrNull())
    }

    @Test
    fun `Non-numeric date fails`() {
        val result =
            parse6DigitDate(
                date = "9AB713",
                dateIsIn = Parse6DigitDateUseCase.DateIsIn.PAST,
            )

        assertIs<IllegalArgumentException>(result.exceptionOrNull())
    }
}
