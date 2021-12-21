package network.cere.ddc.core.signature

sealed interface Scheme {

    val name: String
    val publicKeyHex: String

    companion object {
        const val SR_25519 = "sr25519"
        const val ED_25519 = "ed25519"
        const val ETHEREUM = "ethereum"

        fun create(scheme: String, privateKeyHex: String): Scheme = when (scheme.lowercase()) {
            SR_25519 -> Sr25519(privateKeyHex)
            ED_25519 -> Ed25519(privateKeyHex)
            ETHEREUM -> Ethereum(privateKeyHex)
            else -> throw IllegalArgumentException("Unknown scheme")
        }
    }

    fun sign(data: ByteArray): String

    fun verify(data: ByteArray, signature: String): Boolean
}
