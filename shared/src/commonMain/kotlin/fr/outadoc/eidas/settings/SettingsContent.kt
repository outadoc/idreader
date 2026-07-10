package fr.outadoc.eidas.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.outadoc.eidas.ReaderViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    viewModel: ReaderViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = modifier.padding(16.dp)) {
        AuthenticationMethodDropdown(
            modifier = Modifier.fillMaxWidth(),
            selected = state.settings.authenticationMethod,
            onSelected = viewModel::onAuthenticationMethodChanged,
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.settings.password,
            onValueChange = viewModel::onPasswordChanged,
            label = { Text("Password for the selected method") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                ),
        )
    }
}
