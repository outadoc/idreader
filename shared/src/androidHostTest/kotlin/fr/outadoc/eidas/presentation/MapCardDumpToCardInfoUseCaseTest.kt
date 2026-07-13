package fr.outadoc.eidas.presentation

import fr.outadoc.eidas.lds.model.AdditionalPersonalDetails
import fr.outadoc.eidas.lds.model.CardDump
import fr.outadoc.eidas.lds.model.CardHolderName
import fr.outadoc.eidas.lds.model.Centimeters
import fr.outadoc.eidas.lds.model.Date
import fr.outadoc.eidas.lds.model.MrzInfo
import fr.outadoc.eidas.lds.model.OptionalDetails
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MapCardDumpToCardInfoUseCaseTest {
    private val mrzInfo =
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
                        ),
                ),
            birthDate =
                Date.from(
                    year = 1990,
                    month = 7,
                    day = 13,
                ),
            sex = "F",
            expiryDate =
                Date.from(
                    year = 2030,
                    month = 2,
                    day = 11,
                ),
            optionalData1 = null,
            optionalData2 = null,
        )

    private val additionalPersonalDetails =
        AdditionalPersonalDetails(
            title = "DR",
            fullNameNationalCharacters =
                CardHolderName(
                    surname = "Martin",
                    givenNames =
                        persistentListOf(
                            "Maëlys",
                            "Gaëlle",
                        ),
                ),
            personalNumber = "1234567890",
            fullDateOfBirth =
                Date.from(
                    year = 1990,
                    month = 7,
                    day = 14,
                ),
            placeOfBirth =
                listOf(
                    "Paris",
                    "France",
                ),
            permanentAddress =
                listOf(
                    "1 rue de la Paix",
                    "75001 Paris",
                ),
            telephone = "+33612345678",
            profession = "Engineer",
            personalSummary = null,
            otherValidTdNumbers = null,
            custodyInformation = null,
        )

    @Test
    fun `Name and birth date prefer DG11 over MRZ`() {
        val cardDump =
            CardDump(
                mrzInfo = mrzInfo,
                additionalPersonalDetails = additionalPersonalDetails,
                optionalDetails = null,
                picture = null,
            )

        val uiModel = cardDump.toCardInfoUiModel()

        assertEquals(expected = "Martin", actual = uiModel.surname)
        assertEquals(expected = "Maëlys, Gaëlle", actual = uiModel.givenNames)
        assertEquals(expected = "1990-07-14", actual = uiModel.birthDate)
    }

    @Test
    fun `Name and birth date fall back to MRZ when DG11 is absent`() {
        val cardDump =
            CardDump(
                mrzInfo = mrzInfo,
                additionalPersonalDetails = null,
                optionalDetails = null,
                picture = null,
            )

        val uiModel = cardDump.toCardInfoUiModel()

        assertEquals(expected = "MARTIN", actual = uiModel.surname)
        assertEquals(expected = "MAELYS, GAELLE", actual = uiModel.givenNames)
        assertEquals(expected = "1990-07-13", actual = uiModel.birthDate)
    }

    @Test
    fun `Name and birth date fall back to MRZ when DG11 fields are null`() {
        val cardDump =
            CardDump(
                mrzInfo = mrzInfo,
                additionalPersonalDetails =
                    additionalPersonalDetails.copy(
                        fullNameNationalCharacters = null,
                        fullDateOfBirth = null,
                    ),
                optionalDetails = null,
                picture = null,
            )

        val uiModel = cardDump.toCardInfoUiModel()

        assertEquals(expected = "MARTIN", actual = uiModel.surname)
        assertEquals(expected = "MAELYS, GAELLE", actual = uiModel.givenNames)
        assertEquals(expected = "1990-07-13", actual = uiModel.birthDate)
    }

    @Test
    fun `Fields are formatted for display`() {
        val cardDump =
            CardDump(
                mrzInfo = mrzInfo,
                additionalPersonalDetails = additionalPersonalDetails,
                optionalDetails =
                    OptionalDetails(
                        height = Centimeters(cm = 170),
                    ),
                picture = null,
            )

        val uiModel = cardDump.toCardInfoUiModel()

        assertEquals(expected = "Paris, France", actual = uiModel.placeOfBirth)
        assertEquals(expected = "1 rue de la Paix\n75001 Paris", actual = uiModel.permanentAddress)
        assertEquals(expected = "170 cm", actual = uiModel.height)
        assertEquals(expected = "2030-02-11", actual = uiModel.expiryDate)
    }

    @Test
    fun `Approximate dates keep their placeholders`() {
        val cardDump =
            CardDump(
                mrzInfo = mrzInfo,
                additionalPersonalDetails =
                    additionalPersonalDetails.copy(
                        fullDateOfBirth =
                            Date.from(
                                year = 1999,
                                month = null,
                                day = null,
                            ),
                    ),
                optionalDetails = null,
                picture = null,
            )

        val uiModel = cardDump.toCardInfoUiModel()

        assertEquals(expected = "1999-??-??", actual = uiModel.birthDate)
    }

    @Test
    fun `Empty dump maps to empty UI model`() {
        val cardDump =
            CardDump(
                mrzInfo = null,
                additionalPersonalDetails = null,
                optionalDetails = null,
                picture = null,
            )

        val uiModel = cardDump.toCardInfoUiModel()

        assertNull(uiModel.picture)
        assertNull(uiModel.documentFormat)
        assertNull(uiModel.documentCode)
        assertNull(uiModel.documentNumber)
        assertNull(uiModel.issuingState)
        assertNull(uiModel.expiryDate)
        assertNull(uiModel.optionalData1)
        assertNull(uiModel.optionalData2)
        assertNull(uiModel.title)
        assertNull(uiModel.surname)
        assertNull(uiModel.givenNames)
        assertNull(uiModel.nationality)
        assertNull(uiModel.birthDate)
        assertNull(uiModel.placeOfBirth)
        assertNull(uiModel.sex)
        assertNull(uiModel.height)
        assertNull(uiModel.personalNumber)
        assertNull(uiModel.permanentAddress)
        assertNull(uiModel.telephone)
        assertNull(uiModel.profession)
        assertNull(uiModel.personalSummary)
        assertNull(uiModel.otherValidTdNumbers)
        assertNull(uiModel.custodyInformation)
    }
}
