package fr.outadoc.eidas.lds

import fr.outadoc.eidas.lds.model.CardHolderName
import fr.outadoc.eidas.lds.model.MrzInfo
import kotlin.test.Test
import kotlin.test.assertEquals

class ParseMrzUseCaseTest {
    @Test
    fun `Parse specimen MRZ`() {
        val mrz =
            "IDFRAX4RTBPFW46<<<<<<<<<<<<<<<9007138F3002119FRA<<<<<<<<<<<6MARTIN<<MAELYS<GAELLE<MARIE<<<"

        val parseMrz =
            ParseMrzUseCase(
                parseMrzName = ParseMrzNameUseCase(),
            )

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
                            givenNames = listOf("MAELYS", "GAELLE", "MARIE"),
                        ),
                    birthDate = "900713",
                    sex = "F",
                    expiryDate = "300211",
                    optionalData1 = null,
                    optionalData2 = null,
                ),
            actual = parsed,
        )
    }
}
