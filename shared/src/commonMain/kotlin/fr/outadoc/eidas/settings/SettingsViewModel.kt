package fr.outadoc.eidas.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository,
) : ViewModel() {
    data class State(
        val password: String = "",
        val authenticationMethod: AuthenticationMethod = AuthenticationMethod.CAN,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.settings.collect { settings ->
                _state.update { state ->
                    state.copy(
                        password = settings.password,
                        authenticationMethod = settings.authenticationMethod,
                    )
                }
            }
        }
    }

    fun onDismiss() {
        viewModelScope.launch {
            val currentState = state.value
            repository.saveSettings(
                AppSettings(
                    password = currentState.password,
                    authenticationMethod = currentState.authenticationMethod,
                ),
            )
        }
    }

    fun onPasswordChanged(password: String) {
        _state.update { state ->
            state.copy(password = password)
        }
    }

    fun onAuthenticationMethodChanged(method: AuthenticationMethod) {
        _state.update { state ->
            state.copy(authenticationMethod = method)
        }
    }
}
