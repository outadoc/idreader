package fr.outadoc.eidas.lds

class ParseMrzUseCase {
    operator fun invoke(mrz: String): Result<MrzInfo> {
        val format: String =
            when (mrz.length) {
                90 -> "TD1"

                72 -> "TD2"

                88 -> "TD3"

                else -> return Result.failure(
                    IllegalStateException("Unknown MRZ format: length ${mrz.length}"),
                )
            }

        // Line 1 (0–29)
        val documentCode: String = mrz.substring(0, 2)
        val issuingState: String = mrz.substring(2, 5)
        val documentNumber: String = mrz.substring(5, 14)
        val optionalData1: String? =
            mrz
                .substring(15, 30)
                .trim('<')
                .takeIf { it.isNotEmpty() }

        // Line 2 (30–59)
        val birthDateRaw: String = mrz.substring(30, 36)
        val sex: String = mrz.substring(37, 38)
        val expiryDateRaw: String = mrz.substring(38, 44)
        val nationality: String = mrz.substring(45, 48)
        val optionalData2: String? =
            mrz
                .substring(48, 59)
                .trim('<')
                .takeIf { it.isNotEmpty() }

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
                documentNumber = documentNumber,
                issuingState = issuingState,
                nationality = nationality,
                surname = surname,
                givenNames = givenNames,
                birthDate = birthDateRaw,
                sex = sex,
                expiryDate = expiryDateRaw,
                optionalData1 = optionalData1,
                optionalData2 = optionalData2,
            ),
        )
    }
}
