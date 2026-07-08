package fr.outadoc.eidas.lds

class ParseMrzUseCase {
    operator fun invoke(mrz: String): Result<MrzInfo> {
        // TD1: 3 lines × 30 = 90 chars
        val format =
            when (mrz.length) {
                90 -> "TD1"

                72 -> "TD2"

                88 -> "TD3"

                else -> return Result.failure(
                    IllegalStateException("Unknown MRZ format: length ${mrz.length}"),
                )
            }

        // Line 1 (0–29)
        val documentCode = mrz.substring(0, 2)
        val issuingState = mrz.substring(2, 5)
        val documentNumber = mrz.substring(5, 14)
        val documentNumberCheckDigit = mrz.substring(14, 15)
        val optionalData1 = mrz.substring(15, 30).trim('<')

        // Line 2 (30–59)
        val birthDateRaw: String = mrz.substring(30, 36)
        val birthDateCheckDigit: String = mrz.substring(36, 37)
        val sex: String = mrz.substring(37, 38)
        val expiryDateRaw: String = mrz.substring(38, 44)
        val expiryDateCheckDigit: String = mrz.substring(44, 45)
        val nationality: String = mrz.substring(45, 48)
        val optionalData2: String = mrz.substring(48, 59).trim('<')
        val compositeCheckDigit: String = mrz.substring(59, 60)

        // Line 3 (60–89): SURNAME<<GIVEN1<GIVEN2<...
        val nameField: String = mrz.substring(60, 90)

        val nameParts: List<String> =
            nameField.split("<<", limit = 2)

        val surname: String =
            nameParts[0].replace('<', ' ').trim()

        val givenNames: List<String> =
            nameParts
                .getOrElse(1) { "" }
                .trim('<')
                .split('<')

        return Result.success(
            MrzInfo(
                format = format,
                documentCode = documentCode,
                issuingState = issuingState,
                documentNumber = documentNumber,
                documentNumberCheckDigit = documentNumberCheckDigit,
                optionalData1 = optionalData1,
                optionalData2 = optionalData2,
                birthDateRaw = birthDateRaw,
                birthDate = birthDateRaw,
                birthDateCheckDigit = birthDateCheckDigit,
                sex = sex,
                expiryDate = expiryDateRaw,
                expiryDateCheckDigit = expiryDateCheckDigit,
                nationality = nationality,
                compositeCheckDigit = compositeCheckDigit,
                surname = surname,
                givenNames = givenNames,
            ),
        )
    }
}
