package network.cere.ddc.core.signature

import com.debuggor.schnorrkel.sign.ExpansionMode
import com.debuggor.schnorrkel.sign.KeyPair
import com.debuggor.schnorrkel.sign.SigningContext
import network.cere.ddc.core.extension.hexToBytes
import network.cere.ddc.core.extension.toHex
import network.cere.ddc.core.signature.Scheme.Companion.SR_25519

class Sr25519(privateKey: String) : Scheme {

    private val keyPair = KeyPair.fromSecretSeed(privateKey.hexToBytes(), ExpansionMode.Ed25519)

    override val name = SR_25519
    override val publicKey = keyPair.publicKey.toPublicKey().toHex()

    override fun sign(data: ByteArray): String {
        val signingContext = SigningContext.createSigningContext("substrate".toByteArray())
        return keyPair.sign(signingContext.bytes(data)).to_bytes().toHex()
    }

    override fun verify(data: ByteArray, signature: String): Boolean {
        val signingContext = SigningContext.createSigningContext("substrate".toByteArray())
        return keyPair.verify(signingContext.bytes(data), signature.hexToBytes())
    }
}