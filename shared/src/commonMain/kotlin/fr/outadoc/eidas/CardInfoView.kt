package fr.outadoc.eidas

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.outadoc.eidas.lds.model.CardDump

@Composable
fun CardInfo(
    modifier: Modifier = Modifier,
    cardDump: CardDump,
) {
    Column(modifier = modifier) {
        cardDump.additionalPersonalDetails?.let { details ->
            details.fullNameNationalCharacters?.let { name ->
                Text(
                    buildString {
                        append(name.surname)
                        append(" ")
                        name.givenNames.forEach { givenName ->
                            append(givenName)
                            append(" ")
                        }
                    },
                )
            }
        }
    }
}
