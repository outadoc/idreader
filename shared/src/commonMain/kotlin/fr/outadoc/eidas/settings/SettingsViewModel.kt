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

    data class State(val can: String = "")

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.can.collect { can ->
                _state.update { it.copy(can = can) }
            }
        }
    }

    fun onCanChanged(can: String) {
        viewModelScope.launch {
            repository.setCan(can)
        }
    }
}
