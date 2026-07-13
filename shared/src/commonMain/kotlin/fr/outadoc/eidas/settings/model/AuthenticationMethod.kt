package fr.outadoc.eidas.settings.model

enum class AuthenticationMethod {
    CAN,
    MRZ,
    PIN,
    PUK,
    ;

    companion object {
        val ENABLED = listOf(CAN, PIN, PUK)
    }
}
