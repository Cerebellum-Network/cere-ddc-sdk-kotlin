package network.cere.ddc.core.signature

interface Scheme {

    val name: String
    val publicKey: String

    companion object {
        const val SR_25519 = "sr25519"
        const val ED_25519 = "ed25519"
        const val ETHEREUM = "ethereum"

        fun create(scheme: String, privateKey: String): Scheme = when (scheme.lowercase()) {
            SR_25519 -> Sr25519(privateKey)
            ED_25519 -> Ed25519(privateKey)
            ETHEREUM -> Ethereum(privateKey)
            else -> throw IllegalArgumentException("Unknown scheme")
        }
    }

    fun sign(data: ByteArray): String

    fun verify(data: ByteArray, signature: String): Boolean
}
