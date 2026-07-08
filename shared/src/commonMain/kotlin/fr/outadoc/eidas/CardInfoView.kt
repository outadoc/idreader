package fr.outadoc.eidas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.outadoc.eidas.lds.model.AdditionalPersonalDetails
import fr.outadoc.eidas.lds.model.CardDump
import fr.outadoc.eidas.lds.model.CardHolderName
import fr.outadoc.eidas.lds.model.MrzInfo

@Composable
fun CardInfo(
    modifier: Modifier = Modifier,
    cardDump: CardDump,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        cardDump.mrzInfo?.let { mrz ->
            InfoSection(title = "Document") {
                InfoField(
                    label = "Format",
                    value = mrz.format,
                )
                InfoField(
                    label = "Document code",
                    value = mrz.documentCode,
                )
                InfoField(
                    label = "Document number",
                    value = mrz.documentNumber,
                )
                InfoField(
                    label = "Issuing state",
                    value = mrz.issuingState,
                )
                InfoField(
                    label = "Expiry date",
                    value = mrz.expiryDate,
                )
                mrz.optionalData1?.let {
                    InfoField(
                        label = "Optional data 1",
                        value = it,
                    )
                }
                mrz.optionalData2?.let {
                    InfoField(
                        label = "Optional data 2",
                        value = it,
                    )
                }
            }

            InfoSection(title = "Holder") {
                InfoField(
                    label = "Name",
                    value = mrz.cardHolderName.formatted(),
                )
                InfoField(
                    label = "Nationality",
                    value = mrz.nationality,
                )
                InfoField(
                    label = "Date of birth",
                    value = mrz.birthDate,
                )
                InfoField(
                    label = "Sex",
                    value = mrz.sex,
                )
            }
        }

        cardDump.additionalPersonalDetails?.let { details ->
            InfoSection(title = "Additional details") {
                details.title?.let {
                    InfoField(
                        label = "Title",
                        value = it,
                    )
                }
                details.fullNameNationalCharacters?.let {
                    InfoField(
                        label = "Name (national characters)",
                        value = it.formatted(),
                    )
                }
                details.personalNumber?.let {
                    InfoField(
                        label = "Personal number",
                        value = it,
                    )
                }
                details.fullDateOfBirth?.let {
                    InfoField(
                        label = "Date of birth",
                        value = it,
                    )
                }
                details.placeOfBirth?.let {
                    InfoField(
                        label = "Place of birth",
                        value = it,
                    )
                }
                details.permanentAddress?.let {
                    InfoField(
                        label = "Permanent address",
                        value = it,
                    )
                }
                details.telephone?.let {
                    InfoField(
                        label = "Telephone",
                        value = it,
                    )
                }
                details.profession?.let {
                    InfoField(
                        label = "Profession",
                        value = it,
                    )
                }
                details.personalSummary?.let {
                    InfoField(
                        label = "Personal summary",
                        value = it,
                    )
                }
                details.otherValidTdNumbers?.let {
                    InfoField(
                        label = "Other valid TD numbers",
                        value = it,
                    )
                }
                details.custodyInformation?.let {
                    InfoField(
                        label = "Custody information",
                        value = it,
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        HorizontalDivider()
        content()
    }
}

@Composable
private fun InfoField(
    label: String,
    value: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

private fun CardHolderName.formatted(): String =
    buildString {
        append(surname)
        givenNames.forEach { givenName ->
            append(" ")
            append(givenName)
        }
    }

@Preview
@Composable
private fun CardInfoPreview() {
    MaterialTheme {
        CardInfo(
            modifier = Modifier.padding(16.dp),
            cardDump =
                CardDump(
                    mrzInfo =
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
                    additionalPersonalDetails =
                        AdditionalPersonalDetails(
                            title = null,
                            fullNameNationalCharacters =
                                CardHolderName(
                                    surname = "Martin",
                                    givenNames = listOf("Maëlys", "Gaëlle", "Marie"),
                                ),
                            personalNumber = "1234567890",
                            fullDateOfBirth = "19900713",
                            placeOfBirth = "Paris",
                            permanentAddress = "1 rue de la Paix<75001 Paris",
                            telephone = "+33612345678",
                            profession = "Engineer",
                            personalSummary = null,
                            otherValidTdNumbers = null,
                            custodyInformation = null,
                        ),
                    picture = null,
                ),
        )
    }
}
