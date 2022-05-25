package network.cere.ddc.core.signature

import network.cere.ddc.core.extension.hexToBytes
import network.cere.ddc.core.signature.Scheme.Companion.ED_25519
import org.bouncycastle.math.ec.rfc8032.Ed25519
import org.komputing.khex.extensions.toHexString

class Ed25519(privateKey: ByteArray) : Scheme {

    private val privateKey = privateKey.sliceArray(0 until Ed25519.SECRET_KEY_SIZE)
    private val publicKey = ByteArray(Ed25519.PUBLIC_KEY_SIZE).also {
        Ed25519.generatePublicKey(this.privateKey, 0, it, 0)
    }

    override val name = ED_25519
    override val publicKeyHex = publicKey.toHexString()

    override fun sign(data: ByteArray): String {
        val signatureBytes = ByteArray(Ed25519.SIGNATURE_SIZE)

        Ed25519.sign(privateKey, 0, publicKey, 0, data, 0, data.size, signatureBytes, 0)

        return signatureBytes.toHexString()
    }

    override fun verify(data: ByteArray, signature: String) =
        Ed25519.verify(signature.hexToBytes(), 0, publicKey, 0, data, 0, data.size)
}