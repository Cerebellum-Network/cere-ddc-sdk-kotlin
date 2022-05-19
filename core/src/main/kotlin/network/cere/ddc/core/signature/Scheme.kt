package network.cere.ddc.core.signature

sealed interface Scheme {

    val name: String
    val publicKeyHex: String

    companion object {
        const val SR_25519 = "sr25519"
        const val ED_25519 = "ed25519"
        const val SECP_256_K_1 = "secp256k1"

        fun create(scheme: String, seedHex: String): Scheme = when (scheme.lowercase()) {
            SR_25519 -> Sr25519(seedHex)
            ED_25519 -> Ed25519(seedHex)
            SECP_256_K_1 -> Secp256k1(seedHex)
            else -> throw IllegalArgumentException("Unknown scheme")
        }
    }

    fun sign(data: ByteArray): String

    fun verify(data: ByteArray, signature: String): Boolean
}
