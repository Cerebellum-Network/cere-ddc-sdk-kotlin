package network.cere.ddc.core.signature

import network.cere.ddc.core.extension.hexToBytes
import network.cere.ddc.core.extension.toHex
import network.cere.ddc.core.signature.Scheme.Companion.SECP_256_K_1
import org.kethereum.crypto.*
import org.kethereum.extensions.toBigInteger
import org.kethereum.extensions.toHexStringZeroPadded
import org.kethereum.keccakshortcut.keccak
import org.kethereum.model.ECKeyPair
import org.kethereum.model.PrivateKey
import org.kethereum.model.SignatureData
import org.komputing.khex.extensions.prepend0xPrefix
import org.komputing.khex.model.HexString
import java.security.SignatureException

class Secp256k1(privateKeyHex: String) : Scheme {

    private val keyPair: ECKeyPair

    override val name = SECP_256_K_1
    override val publicKeyHex: String

    init {
        val privateKey = PrivateKey(privateKeyHex.hexToBytes())
        val publicKey = publicKeyFromPrivate(privateKey)

        keyPair = ECKeyPair(privateKey, publicKey)
        publicKeyHex = keyPair.publicKey.toAddress().hex
    }

    override fun sign(data: ByteArray): String = HexString(keyPair.signMessage(data).toHex()).prepend0xPrefix().string

    override fun verify(data: ByteArray, signature: String): Boolean {
        val signatureBytes = signature.hexToBytes()
        var v = signatureBytes[64].toInt()
        if (v < 27) {
            v += 27
        }

        val r = signatureBytes.sliceArray(0 until 32).toBigInteger()
        val s = signatureBytes.sliceArray(32 until 64).toBigInteger()

        val address = try {
            signedMessageToKey(data, SignatureData(r, s, v.toBigInteger()))
                .key
                .toHexStringZeroPadded(128, false)
                .let(::HexString)
                .string
                .hexToBytes()
                .keccak()
                .toHex(false)
                .takeLast(40)
        } catch (e: SignatureException) {
            return false
        }

        return address.equals(publicKeyHex.substring(2), true)
    }


}