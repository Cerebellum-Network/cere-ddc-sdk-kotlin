package network.cere.ddc.core.signature

import network.cere.ddc.core.extension.HEX_PREFIX
import network.cere.ddc.core.extension.hexToBytes
import network.cere.ddc.core.signature.Scheme.Companion.ETHEREUM
import org.kethereum.crypto.*
import org.kethereum.extensions.toBigInteger
import org.kethereum.extensions.toHexStringZeroPadded
import org.kethereum.keccakshortcut.keccak
import org.kethereum.model.ECKeyPair
import org.kethereum.model.PrivateKey
import org.kethereum.model.SignatureData
import org.komputing.khex.extensions.toNoPrefixHexString
import org.komputing.khex.model.HexString
import java.security.SignatureException

class Ethereum(privateKey: String) : Scheme {

    private val pair: ECKeyPair

    override val name = ETHEREUM
    override val publicKey: String

    init {
        val private = PrivateKey(privateKey.hexToBytes())
        val public = publicKeyFromPrivate(private)

        pair = ECKeyPair(private, public)
        publicKey = pair.publicKey.toAddress().hex
    }

    override fun sign(data: ByteArray): String {
        return "${HEX_PREFIX}${pair.signMessage(data).toHex()}"
    }

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
                .toNoPrefixHexString()
                .takeLast(40)
        } catch (e: SignatureException) {
            return false
        }

        return address.equals(publicKey.removePrefix(HEX_PREFIX), true)
    }


}