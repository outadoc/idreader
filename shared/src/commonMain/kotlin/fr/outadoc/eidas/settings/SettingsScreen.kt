package fr.outadoc.eidas.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = state.can,
            onValueChange = viewModel::onCanChanged,
            label = { Text("CAN") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
