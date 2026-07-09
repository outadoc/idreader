package fr.outadoc.eidas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import fr.outadoc.eidas.presentation.CardInfoUiModel

@Composable
fun CardInfo(
    modifier: Modifier = Modifier,
    cardInfo: CardInfoUiModel,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AsyncImage(
            modifier = Modifier.height(160.dp),
            model = cardInfo.picture,
            contentDescription = "Photo of document holder",
        )

        InfoSection(
            title = "Document",
            fields =
                listOf(
                    "Format" to cardInfo.format,
                    "Document code" to cardInfo.documentCode,
                    "Document number" to cardInfo.documentNumber,
                    "Issuing state" to cardInfo.issuingState,
                    "Expiry date" to cardInfo.expiryDate,
                    "Optional data 1" to cardInfo.optionalData1,
                    "Optional data 2" to cardInfo.optionalData2,
                ),
        )

        InfoSection(
            title = "Holder",
            fields =
                listOf(
                    "Title" to cardInfo.title,
                    "Surname" to cardInfo.surname,
                    "Given Names" to cardInfo.givenNames,
                    "Nationality" to cardInfo.nationality,
                    "Date of birth" to cardInfo.birthDate,
                    "Place of birth" to cardInfo.placeOfBirth,
                    "Sex" to cardInfo.sex,
                    "Height" to cardInfo.height,
                    "Personal number" to cardInfo.personalNumber,
                    "Permanent address" to cardInfo.permanentAddress,
                    "Telephone" to cardInfo.telephone,
                    "Profession" to cardInfo.profession,
                    "Personal summary" to cardInfo.personalSummary,
                    "Other valid TD numbers" to cardInfo.otherValidTdNumbers,
                    "Custody information" to cardInfo.custodyInformation,
                ),
        )
    }
}

@Composable
private fun InfoSection(
    title: String,
    fields: List<Pair<String, String?>>,
) {
    val filledFields =
        fields.mapNotNull { (label, value) ->
            value?.let { label to it }
        }

    if (filledFields.isEmpty()) {
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        HorizontalDivider()
        filledFields.forEach { (label, value) ->
            InfoField(
                label = label,
                value = value,
            )
        }
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

@Preview(showBackground = true)
@Composable
private fun CardInfoPreview() {
    MaterialTheme {
        CardInfo(
            modifier = Modifier.padding(16.dp),
            cardInfo =
                CardInfoUiModel(
                    picture = null,
                    format = "TD1",
                    documentCode = "ID",
                    documentNumber = "X4RTBPFW4",
                    issuingState = "FRA",
                    expiryDate = "2030-02-11",
                    optionalData1 = null,
                    optionalData2 = null,
                    title = null,
                    surname = "Martin",
                    givenNames = "Maëlys, Gaëlle, Marie",
                    nationality = "FRA",
                    birthDate = "1990-07-13",
                    placeOfBirth = "Paris",
                    sex = "F",
                    height = "182 cm",
                    personalNumber = "1234567890",
                    permanentAddress = "1 rue de la Paix\n75001 Paris",
                    telephone = "+33612345678",
                    profession = "Engineer",
                    personalSummary = null,
                    otherValidTdNumbers = null,
                    custodyInformation = null,
                ),
        )
    }
}
