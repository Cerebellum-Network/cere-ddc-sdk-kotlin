package network.cere.ddc.core.signature

import com.debuggor.schnorrkel.sign.ExpansionMode
import com.debuggor.schnorrkel.sign.KeyPair
import com.debuggor.schnorrkel.sign.SigningContext
import network.cere.ddc.core.extension.hexToBytes
import network.cere.ddc.core.signature.Scheme.Companion.SR_25519
import org.komputing.khex.extensions.toHexString

class Sr25519(seed: ByteArray) : Scheme {

    private val keyPair = KeyPair.fromSecretSeed(seed, ExpansionMode.Ed25519)

    override val name = SR_25519
    override val publicKeyHex = keyPair.publicKey.toPublicKey().toHexString()

    override fun sign(data: ByteArray): String {
        val signingContext = SigningContext.createSigningContext("substrate".toByteArray())
        return keyPair.sign(signingContext.bytes(data)).to_bytes().toHexString()
    }

    override fun verify(data: ByteArray, signature: String): Boolean {
        val signingContext = SigningContext.createSigningContext("substrate".toByteArray())
        return keyPair.verify(signingContext.bytes(data), signature.hexToBytes())
    }
}