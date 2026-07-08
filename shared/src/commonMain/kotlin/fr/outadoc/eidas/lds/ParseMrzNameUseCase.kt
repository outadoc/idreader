package fr.outadoc.eidas.lds

import fr.outadoc.eidas.lds.model.CardHolderName
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

class ParseMrzNameUseCase {
    operator fun invoke(rawName: String): CardHolderName {
        // Format: SURNAME<<GIVEN1<GIVEN2<...
        val nameParts: List<String> =
            rawName.split("<<", limit = 2)

        val surname: String =
            nameParts[0].replace('<', ' ').trim()

        val givenNames: ImmutableList<String> =
            nameParts
                .getOrElse(1) { "" }
                .trim('<')
                .split('<')
                .toImmutableList()

        return CardHolderName(
            surname = surname,
            givenNames = givenNames,
        )
    }
}
