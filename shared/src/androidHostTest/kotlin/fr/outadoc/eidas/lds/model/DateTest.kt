package fr.outadoc.eidas.lds.model

import kotlin.test.Test
import kotlin.test.assertEquals

class DateTest {
    @Test
    fun `Complete date is Exact`() {
        val date =
            Date.from(
                year = 1995,
                month = 7,
                day = 13,
            )

        assertEquals(
            expected = "1995-07-13",
            actual = date.toString(),
        )
    }

    @Test
    fun `Date with unknown year is Approx`() {
        val date =
            Date.from(
                year = null,
                month = 7,
                day = 13,
            )

        assertEquals(
            expected = "????-07-13",
            actual = date.toString(),
        )
    }

    @Test
    fun `Date with unknown month is Approx`() {
        val date =
            Date.from(
                year = 1995,
                month = null,
                day = 13,
            )

        assertEquals(
            expected = "1995-??-13",
            actual = date.toString(),
        )
    }

    @Test
    fun `Date with unknown day is Approx`() {
        val date =
            Date.from(
                year = 1995,
                month = 7,
                day = null,
            )

        assertEquals(
            expected = "1995-07-??",
            actual = date.toString(),
        )
    }

    @Test
    fun `Fully unknown date is Approx`() {
        val date =
            Date.from(
                year = null,
                month = null,
                day = null,
            )

        assertEquals(
            expected = "????-??-??",
            actual = date.toString(),
        )
    }
}
