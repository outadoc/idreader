package fr.outadoc.eidas.lds

import fr.outadoc.eidas.lds.model.Date

class Parse8DigitDateUseCase {
    operator fun invoke(date: String): Result<Date> {
        if (date.length != 8) {
            return Result.failure(
                IllegalArgumentException("Expected 8-digit date but got: $date"),
            )
        }

        if (date.any { char -> !char.isDigit() }) {
            return Result.failure(
                IllegalArgumentException("Expected numeric date but got: $date"),
            )
        }

        val year: Int? =
            date
                .substring(0..3)
                .toInt()
                .takeIf { it > 0 }

        val month: Int? =
            date
                .substring(4..5)
                .toInt()
                .takeIf { it > 0 }

        val day: Int? =
            date
                .substring(6..7)
                .toInt()
                .takeIf { it > 0 }

        return Result.success(
            Date.from(
                year = year,
                month = month,
                day = day,
            ),
        )
    }
}
