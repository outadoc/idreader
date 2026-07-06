package fr.outadoc.eidas.crypto

data class Algorithm(
    val protocol: Protocol,
    val parameter: DomainParameter,
) {
    companion object {
        val preferredAlgorithms: List<Algorithm> =
            listOf(
                Algorithm(
                    protocol = Protocol.PACE_ECDH_GM_AES_CBC_CMAC_256,
                    parameter = DomainParameter.BRAINPOOLP256R1,
                ),
            )
    }
}
