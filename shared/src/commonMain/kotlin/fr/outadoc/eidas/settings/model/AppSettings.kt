package fr.outadoc.eidas.settings.model

data class AppSettings(
    val password: String = "",
    val authenticationMethod: AuthenticationMethod = AuthenticationMethod.CAN,
)
