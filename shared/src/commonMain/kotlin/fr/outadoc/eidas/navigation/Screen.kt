package fr.outadoc.eidas.navigation

import fr.outadoc.eidas.presentation.CardInfoUiModel

sealed class Screen {
    data object Reader : Screen()

    data object Logs : Screen()

    data class ScanResult(
        val cardInfo: CardInfoUiModel,
    ) : Screen()
}
