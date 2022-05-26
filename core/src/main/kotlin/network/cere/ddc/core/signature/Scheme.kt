package network.cere.ddc.core.signature

import network.cere.ddc.core.extension.hexToBytes

sealed interface Scheme {

    val name: String
    val publicKeyHex: String

    companion object {
        const val SR_25519 = "sr25519"
        const val ED_25519 = "ed25519"
        const val SECP_256_K_1 = "secp256k1"

        fun create(scheme: String, seedHex: String): Scheme {
            val seed = runCatching { seedHex.hexToBytes() }
                .getOrElse { throw IllegalArgumentException("Couldn't parse to bytes seed hex") }
                .also { if (it.size != 32) throw IllegalArgumentException("Couldn't create scheme. Seed size should be 32 bytes") }

            return when (scheme.lowercase()) {
                SR_25519 -> Sr25519(seed)
                ED_25519 -> Ed25519(seed)
                SECP_256_K_1 -> Secp256k1(seed)
                else -> throw IllegalArgumentException("Unknown scheme $scheme")
            }
        }
    }


    fun sign(data: ByteArray): String

    fun verify(data: ByteArray, signature: String): Boolean
}
