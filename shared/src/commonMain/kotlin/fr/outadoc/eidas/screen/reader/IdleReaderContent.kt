package fr.outadoc.eidas.screen.reader

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import eidas.shared.generated.resources.Res
import eidas.shared.generated.resources.cnie_fr_can
import fr.outadoc.eidas.icons.AppIcons
import fr.outadoc.eidas.icons.cancel
import fr.outadoc.eidas.settings.model.AppSettings
import fr.outadoc.eidas.settings.model.AuthenticationMethod
import org.jetbrains.compose.resources.painterResource

@Composable
fun IdleReaderContent(
    modifier: Modifier = Modifier,
    settings: AppSettings,
    onAuthenticationMethodChanged: (AuthenticationMethod) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onStartListeningClicked: () -> Unit,
    exception: Throwable? = null,
) {
    val showDialog = remember { mutableStateOf(false) }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            confirmButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text("OK")
                }
            },
            title = { Text("CAN on a French CNIe") },
            text = {
                Image(
                    painterResource(Res.drawable.cnie_fr_can),
                    contentDescription = "The CAN is printed at the bottom right of the verso of your card.",
                )
            },
        )
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Button(onClick = onStartListeningClicked) {
            Text("Read document")
        }

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
                            contentDescription = "Clear",
                        )
                    }
                },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                    ),
            )

            TextButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = { showDialog.value = true },
            ) {
                Text("Where can I find the CAN?")
            }

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
