package network.cere.ddc.crypto.signature

import network.cere.ddc.crypto.extension.hexToBytes
import network.cere.ddc.crypto.signature.Scheme.Companion.ETHEREUM
import org.kethereum.crypto.signedMessageToKey
import org.kethereum.extensions.toBigInteger
import org.kethereum.extensions.toHexStringZeroPadded
import org.kethereum.keccakshortcut.keccak
import org.kethereum.model.SignatureData
import org.komputing.khex.extensions.toNoPrefixHexString
import org.komputing.khex.model.HexString

class Ethereum(privateKey: String) : Scheme {

    override val name = ETHEREUM
    override val publicKey = privateKey

    override fun sign(data: ByteArray): String {
        TODO("Not yet implemented")
    }

    override fun verify(data: ByteArray, signature: String): Boolean {
        val signatureBytes = signature.hexToBytes()
        var v = signatureBytes[64].toInt()
        if (v < 27) {
            v += 27
        }

        val r = signatureBytes.sliceArray(0 until 32).toBigInteger()
        val s = signatureBytes.sliceArray(32 until 64).toBigInteger()

        val address = signedMessageToKey(data, SignatureData(r, s, v.toBigInteger()))
            .key
            .toHexStringZeroPadded(128, false)
            .let(::HexString)
            .string
            .hexToBytes()
            .keccak()
            .toNoPrefixHexString()
            .takeLast(40)

        return address.equals(publicKey, true)
    }


}