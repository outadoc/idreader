package fr.outadoc.eidas.navigation

import fr.outadoc.eidas.presentation.CardInfo

sealed class Screen {
    data object Reader : Screen()

    data object Logs : Screen()

    data class ScanResult(
        val cardInfo: CardInfo,
    ) : Screen()

    companion object {
        val DEFAULT = Reader
    }
}
