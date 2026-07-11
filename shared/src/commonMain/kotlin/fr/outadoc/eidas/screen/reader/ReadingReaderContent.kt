package fr.outadoc.eidas.screen.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.eidas.icons.AppIcons
import fr.outadoc.eidas.icons.mobile
import fr.outadoc.eidas.icons.passport

@Composable
fun ReadingReaderContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        // TODO Display graphics card -> phone with moving data on read

        Icon(
            modifier = Modifier.size(64.dp),
            imageVector = AppIcons.mobile,
            contentDescription = "Mobile device",
        )

        CircularProgressIndicator()

        Icon(
            modifier = Modifier.size(64.dp),
            imageVector = AppIcons.passport,
            contentDescription = "Identity document",
        )
    }
}
