package fr.outadoc.eidas.lds

import fr.outadoc.eidas.lds.model.CardHolderName
import fr.outadoc.eidas.lds.model.MrzInfo
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

class ParseMrzUseCaseTest {
    @Test
    fun `Parse TD1 MRZ from FR specimen`() {
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
                            givenNames =
                                persistentListOf(
                                    "MAELYS",
                                    "GAELLE",
                                    "MARIE",
                                ),
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

    @Test
    fun `Parse TD1 MRZ from ISAO example`() {
        val mrz =
            "I<NLDXI85935F86999999990<<<<<<7208148F1108268NLD<<<<<<<<<<<4VAN<DER<STEEN<<MARIANNE<LOUISE"

        val parseMrz =
            ParseMrzUseCase(
                parseMrzName = ParseMrzNameUseCase(),
            )

        val parsed = parseMrz(mrz).getOrThrow()

        assertEquals(
            expected =
                MrzInfo(
                    format = "TD1",
                    documentCode = "I<",
                    documentNumber = "XI85935F8",
                    issuingState = "NLD",
                    nationality = "NLD",
                    cardHolderName =
                        CardHolderName(
                            surname = "VAN DER STEEN",
                            givenNames =
                                persistentListOf(
                                    "MARIANNE",
                                    "LOUISE",
                                ),
                        ),
                    birthDate = "720814",
                    sex = "F",
                    expiryDate = "110826",
                    optionalData1 = "999999990",
                    optionalData2 = null,
                ),
            actual = parsed,
        )
    }
}
