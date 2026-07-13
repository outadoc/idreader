package fr.outadoc.eidas.navigation

import fr.outadoc.eidas.lds.model.DocumentPicture
import fr.outadoc.eidas.presentation.CardInfo

sealed class Screen {
    data object Reader : Screen()

    data object Logs : Screen()

    data class ScanResult(
        val cardInfo: CardInfo,
    ) : Screen()

    data class FullScreenPicture(
        val documentPicture: DocumentPicture,
    ) : Screen()

    companion object {
        val DEFAULT = Reader
    }
}
