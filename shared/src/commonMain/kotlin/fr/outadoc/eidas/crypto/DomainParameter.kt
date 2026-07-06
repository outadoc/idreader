package fr.outadoc.eidas.crypto

enum class DomainParameter(
    val parameterId: Int,
) {
    /**
     * 1024-bit MODP Group with 160-bit Prime Order Subgroup
     */
    GFP_1024_160(0),

    /**
     * 2048-bit MODP Group with 224-bit Prime Order Subgroup
     */
    GFP_2048_224(1),

    /**
     * 2048-bit MODP Group with 256-bit Prime Order Subgroup
     */
    GFP_2048_256(2),

    /**
     * NIST P-192 (secp192r1)
     */
    SECP192R1(8),

    /**
     * BrainpoolP192r1
     */
    BRAINPOOLP192R1(9),

    /**
     * NIST P-224 (secp224r1)
     */
    SECP224R1(10),

    /**
     * BrainpoolP224r1
     */
    BRAINPOOLP224R1(11),

    /**
     * NIST P-256 (secp256r1)
     */
    SECP256R1(12),

    /**
     * BrainpoolP256r1
     */
    BRAINPOOLP256R1(13),

    /**
     * BrainpoolP320r1
     */
    BRAINPOOLP320R1(14),

    /**
     * NIST P-384 (secp384r1)
     */
    SECP384R1(15),

    /**
     * BrainpoolP384r1
     */
    BRAINPOOLP384R1(16),

    /**
     * BrainpoolP512r1
     */
    BRAINPOOLP512R1(17),

    /**
     * NIST P-521 (secp521r1)
     */
    SECP521R1(18),
    ;

    companion object {
        fun fromParameterId(id: Int): DomainParameter? =
            entries.firstOrNull { entry ->
                entry.parameterId == id
            }
    }
}
