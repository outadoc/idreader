package fr.outadoc.eidas.screen.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import fr.outadoc.eidas.navigation.MainNavigationBar
import fr.outadoc.eidas.navigation.Screen
import fr.outadoc.eidas.settings.model.AppSettings
import fr.outadoc.eidas.settings.model.AuthenticationMethod
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    modifier: Modifier = Modifier,
    navigate: (Screen) -> Unit = {},
    viewModel: ReaderViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.startListening()

        viewModel.scanResults.collect { cardInfo ->
            navigate(
                Screen.ScanResult(cardInfo = cardInfo),
            )
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("eIDAS Reader") },
            )
        },
        bottomBar = {
            MainNavigationBar(
                selected = Screen.Reader,
                navigate = navigate,
            )
        },
    ) { insets ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(insets),
            contentAlignment = Alignment.Center,
        ) {
            Column {
                if (state.isReading) {
                    // TODO Display error on failure
                    // TODO Display graphics card -> phone with moving data on read
                    CircularProgressIndicator()
                } else {
                    IdleReaderContent(
                        settings = state.settings,
                        onAuthenticationMethodChanged = viewModel::onAuthenticationMethodChanged,
                        onPasswordChanged = viewModel::onPasswordChanged,
                    )
                }

                state.exception?.let { e ->
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text =
                                buildString {
                                    append(e::class.qualifiedName)
                                    e.message?.let { message ->
                                        append(": ")
                                        append(message)
                                    }
                                },
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IdleReaderContent(
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
