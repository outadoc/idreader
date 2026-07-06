package fr.outadoc.eidas.crypto

enum class DomainParameter(
    val parameterId: Int,
) {
    BRAINPOOLP256R1(13),
    ;

    companion object {
        fun fromParameterId(id: Int): DomainParameter? =
            entries.firstOrNull { entry ->
                entry.parameterId == id
            }
    }
}
