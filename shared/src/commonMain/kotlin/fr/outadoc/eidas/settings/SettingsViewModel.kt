package fr.outadoc.eidas.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.i
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val logger: Logger,
) : ViewModel() {
    data class State(
        val can: String = "",
    )

    val state: StateFlow<State> =
        repository.settings
            .map { settings ->
                State(
                    can = settings.can,
                )
            }.stateIn(
                viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = State(),
            )

    fun onCanChanged(can: String) {
        logger.i("SettingsViewModel", "can: $can")
        viewModelScope.launch {
            repository.updateCan(can)
        }
    }
}
