package fr.outadoc.eidas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import fr.outadoc.eidas.icons.AppIcons
import fr.outadoc.eidas.icons.settings
import fr.outadoc.eidas.navigation.MainNavigationBar
import fr.outadoc.eidas.navigation.Screen
import fr.outadoc.eidas.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    onSelectTab: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSettings by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("eIDAS Reader") },
                actions = {
                    IconButton(
                        onClick = { showSettings = true },
                    ) {
                        Icon(
                            imageVector = AppIcons.settings,
                            contentDescription = "Open settings",
                        )
                    }
                },
            )
        },
        bottomBar = {
            MainNavigationBar(
                selected = Screen.Reader,
                onSelect = onSelectTab,
            )
        },
    ) { insets ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(insets),
        )
    }

    if (showSettings) {
        ModalBottomSheet(
            onDismissRequest = { showSettings = false },
            sheetState = sheetState,
        ) {
            SettingsScreen()
        }
    }
}
