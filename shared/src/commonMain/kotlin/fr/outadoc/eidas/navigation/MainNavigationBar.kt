package fr.outadoc.eidas.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.outadoc.eidas.icons.AppIcons
import fr.outadoc.eidas.icons.contactless
import fr.outadoc.eidas.icons.terminal

@Composable
fun MainNavigationBar(
    selected: Screen,
    onSelect: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            selected = selected == Screen.Reader,
            onClick = { onSelect(Screen.Reader) },
            icon = {
                Icon(
                    imageVector = AppIcons.contactless,
                    contentDescription = null,
                )
            },
            label = { Text("Reader") },
        )

        NavigationBarItem(
            selected = selected == Screen.Logs,
            onClick = { onSelect(Screen.Logs) },
            icon = {
                Icon(
                    imageVector = AppIcons.terminal,
                    contentDescription = null,
                )
            },
            label = { Text("Logs") },
        )
    }
}
