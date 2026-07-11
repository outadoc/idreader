package fr.outadoc.eidas.screen.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.outadoc.eidas.icons.AppIcons
import fr.outadoc.eidas.icons.mobile
import fr.outadoc.eidas.icons.passport

@Composable
fun ReadingReaderContent(
    commandCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        Icon(
            modifier = Modifier.size(64.dp),
            imageVector = AppIcons.mobile,
            contentDescription = "Mobile device",
        )

        Box(modifier = Modifier.padding(32.dp)) {
            Text(
                text = "·".repeat(commandCount),
                textAlign = TextAlign.Center,
            )
        }

        Icon(
            modifier = Modifier.size(64.dp),
            imageVector = AppIcons.passport,
            contentDescription = "Identity document",
        )
    }
}
