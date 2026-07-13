package fr.outadoc.eidas.screen.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import fr.outadoc.eidas.icons.AppIcons
import fr.outadoc.eidas.icons.cancel
import fr.outadoc.eidas.icons.contactless
import fr.outadoc.eidas.settings.model.AppSettings
import fr.outadoc.eidas.settings.model.AuthenticationMethod

@Composable
fun IdleReaderContent(
    modifier: Modifier = Modifier,
    settings: AppSettings,
    onAuthenticationMethodChanged: (AuthenticationMethod) -> Unit,
    onPasswordChanged: (String) -> Unit,
    exception: Throwable? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Icon(
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(128.dp),
            imageVector = AppIcons.contactless,
            contentDescription = "Tap your card",
        )

        Column {
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
                trailingIcon = {
                    IconButton(onClick = { onPasswordChanged("") }) {
                        Icon(
                            imageVector = AppIcons.cancel,
                            contentDescription = "Clear"
                        )
                    }
                },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                    ),
            )

            exception?.let { e ->
                Box(modifier = Modifier.padding(16.dp)) {
                    Text(
                        color = MaterialTheme.colorScheme.error,
                        text =
                            buildString {
                                append(e::class.simpleName)
                                e.message?.let { message ->
                                    append(": ")
                                    append(message)
                                }
                            },
                    )
                }
            }
        }
    }
}
