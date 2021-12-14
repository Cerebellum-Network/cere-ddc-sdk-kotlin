package network.cere.ddc.crypto.signature

import network.cere.ddc.crypto.extension.hexToBytes
import network.cere.ddc.crypto.extension.toHex
import network.cere.ddc.crypto.signature.Scheme.Companion.ED_25519
import org.bouncycastle.math.ec.rfc8032.Ed25519

class Ed25519(privateKey: String) : Scheme {

    private val private: ByteArray = privateKey.hexToBytes().sliceArray(0 until Ed25519.SECRET_KEY_SIZE)
    private val public: ByteArray = ByteArray(Ed25519.PUBLIC_KEY_SIZE).also {
        Ed25519.generatePublicKey(private, 0, it, 0)
    }

    override val name = ED_25519
    override val publicKey = public.toHex()

    override fun sign(data: ByteArray): String {
        val signatureBytes = ByteArray(Ed25519.SIGNATURE_SIZE)

        Ed25519.sign(private, 0, public, 0, data, 0, data.size, signatureBytes, 0)

        return signatureBytes.toHex()
    }

    override fun verify(data: ByteArray, signature: String) =
        Ed25519.verify(signature.hexToBytes(), 0, public, 0, data, 0, data.size)
}