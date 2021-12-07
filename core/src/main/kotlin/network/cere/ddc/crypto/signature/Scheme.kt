package network.cere.ddc.crypto.signature

interface Scheme {

    companion object {
        const val SR_25519 = "sr25519"
        const val ED_25519 = "ed25519"
        const val ETHEREUM = "ethereum"

        fun create(scheme: String): Scheme = when (scheme.lowercase()) {
            SR_25519 -> Sr25519()
            ED_25519 -> Ed25519()
            ETHEREUM -> Ed25519()
            else -> throw IllegalArgumentException("Unknown scheme")
        }
    }

    fun sign(data: String): String

    fun verify(data: String, signature: String): Boolean
}
