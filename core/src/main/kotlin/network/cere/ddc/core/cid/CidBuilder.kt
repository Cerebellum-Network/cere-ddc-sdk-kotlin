package network.cere.ddc.core.cid

import io.ipfs.cid.Cid
import io.ipfs.multibase.Multibase
import io.ipfs.multihash.Multihash
import java.lang.RuntimeException
import java.security.MessageDigest

class CidBuilder(
    private val codec: Cid.Codec = Cid.Codec.Raw,
    private val type: Multihash.Type = Multihash.Type.sha2_256,
    private val base: Multibase.Base = Multibase.Base.Base32
) {

    private val typeToAlgorithm = mapOf(
        Multihash.Type.md5 to "MD5",
        Multihash.Type.sha1 to "SHA-1",
        Multihash.Type.sha2_256 to "SHA-256",
        Multihash.Type.sha2_512 to "SHA-512"
    )

    private val hashAlgorithm: String = typeToAlgorithm[type] ?: throw RuntimeException("Unsupported multihash type")

    fun build(data: ByteArray): String {
        val hash = MessageDigest.getInstance(hashAlgorithm).apply { this.update(data) }.digest()
        val cid = Cid.buildCidV1(codec, type, hash)
        val cidAsBytes = cid.toBytes()

        return Multibase.encode(base, cidAsBytes);
    }
}
