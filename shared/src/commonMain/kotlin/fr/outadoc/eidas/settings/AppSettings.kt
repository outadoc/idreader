package fr.outadoc.eidas.settings

enum class AuthenticationMethod {
    CAN,
    MRZ,
    PIN,
    PUK,
}

data class AppSettings(
    val password: String = "",
    val authenticationMethod: AuthenticationMethod = AuthenticationMethod.CAN,
)
