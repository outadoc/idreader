package fr.outadoc.eidas.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import fr.outadoc.eidas.settings.model.AppSettings
import fr.outadoc.eidas.settings.model.AuthenticationMethod

@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    settings: AppSettings,
    onAuthenticationMethodChanged: (AuthenticationMethod) -> Unit,
    onPasswordChanged: (String) -> Unit,
) {
    Column(modifier = modifier.padding(16.dp)) {
        AuthenticationMethodDropdown(
            modifier = Modifier.fillMaxWidth(),
            selected = settings.authenticationMethod,
            onSelected = onAuthenticationMethodChanged,
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = settings.password,
            onValueChange = onPasswordChanged,
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
