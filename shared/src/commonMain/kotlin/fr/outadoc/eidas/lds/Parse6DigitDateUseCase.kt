package fr.outadoc.eidas.lds

import fr.outadoc.eidas.lds.model.Date
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class Parse6DigitDateUseCase(
    private val clock: Clock,
    private val timeZone: TimeZone,
) {
    enum class DateIsIn { PAST, FUTURE }

    /**
     * @param dateIsIn Indicates whether the date to decode is in the past or the future.
     */
    operator fun invoke(
        date: String,
        dateIsIn: DateIsIn,
    ): Result<Date> {
        if (date.length != 6) {
            return Result.failure(
                IllegalArgumentException("Expected 6-digit date but got: $date"),
            )
        }

        if (date.any { char -> !char.isDigit() }) {
            return Result.failure(
                IllegalArgumentException("Expected numeric date but got: $date"),
            )
        }

        val yearWithoutCentury: Int? =
            date
                .substring(0..1)
                .toInt()
                .takeIf { it > 0 }

        val month: Int? =
            date
                .substring(2..3)
                .toInt()
                .takeIf { it > 0 }

        val day: Int? =
            date
                .substring(4..5)
                .toInt()
                .takeIf { it > 0 }

        val today: LocalDate = clock.now().toLocalDateTime(timeZone).date

        val year: Int? =
            yearWithoutCentury?.let { twoDigitYear ->
                val currentYear = today.year
                val candidate = (currentYear / 100) * 100 + twoDigitYear
                when (dateIsIn) {
                    DateIsIn.PAST -> if (candidate > currentYear) candidate - 100 else candidate
                    DateIsIn.FUTURE -> if (candidate < currentYear) candidate + 100 else candidate
                }
            }

        return Result.success(
            Date.from(
                year = year,
                month = month,
                day = day,
            ),
        )
    }
}
