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
        val can: String = "",
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.settings.collect { settings ->
                _state.update { it.copy(can = settings.can) }
            }
        }
    }

    fun onCanChanged(can: String) {
        val sanitizedCan: String =
            can.replace(
                regex = Regex("[^0-9]"),
                replacement = ""
            )

        _state.update {
            it.copy(can = sanitizedCan)
        }

        viewModelScope.launch {
            repository.updateCan(sanitizedCan)
        }
    }
}
