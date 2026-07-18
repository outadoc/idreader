package fr.outadoc.eidas.screen.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import fr.outadoc.eidas.AppTheme
import fr.outadoc.eidas.icons.AppIcons
import fr.outadoc.eidas.icons.contactless

@Composable
fun ReaderWaitingContent(
    modifier: Modifier = Modifier,
    onStopListeningClicked: () -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(128.dp),
            imageVector = AppIcons.contactless,
            contentDescription = "Tap your document",
        )

        Text("Waiting for document…")

        Button(onClick = onStopListeningClicked) {
            Text("Cancel")
        }
    }
}

@PreviewLightDark
@Composable
private fun ReaderWaitingPreview() {
    AppTheme {
        Surface {
            ReaderWaitingContent()
        }
    }
}
