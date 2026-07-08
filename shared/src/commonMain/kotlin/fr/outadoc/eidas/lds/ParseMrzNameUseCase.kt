package fr.outadoc.eidas.lds

class ParseMrzNameUseCase {
    operator fun invoke(rawName: String): CardHolderName {
        // Format: SURNAME<<GIVEN1<GIVEN2<...
        val nameParts: List<String> =
            rawName.split("<<", limit = 2)

        val surname: String =
            nameParts[0].replace('<', ' ').trim()

        val givenNames: List<String> =
            nameParts
                .getOrElse(1) { "" }
                .trim('<')
                .split('<')

        return CardHolderName(
            surname = surname,
            givenNames = givenNames,
        )
    }
}
