package fr.outadoc.eidas.navigation

sealed class Screen {
    data object Reader : Screen()

    data object Logs : Screen()
}
