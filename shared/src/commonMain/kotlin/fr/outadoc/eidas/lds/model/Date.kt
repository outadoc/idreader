package fr.outadoc.eidas.lds.model

import kotlinx.datetime.LocalDate

sealed interface Date {
    data class Exact(
        val date: LocalDate,
    ) : Date {
        override fun toString(): String = date.toString()
    }

    data class Approx(
        val year: Int?,
        val month: Int?,
        val day: Int?,
    ) : Date {
        override fun toString(): String =
            buildString {
                append(year ?: "????")
                append("-")
                append(month?.toString()?.padStart(2, '0') ?: "??")
                append("-")
                append(day?.toString()?.padStart(2, '0') ?: "??")
            }
    }

    companion object {
        fun from(
            year: Int?,
            month: Int?,
            day: Int?,
        ): Date =
            if (year == null || month == null || day == null) {
                Approx(
                    year = year,
                    month = month,
                    day = day,
                )
            } else {
                Exact(
                    LocalDate(
                        year = year,
                        month = month,
                        day = day,
                    ),
                )
            }
    }
}
