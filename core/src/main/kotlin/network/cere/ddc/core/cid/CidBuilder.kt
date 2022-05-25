package network.cere.ddc.core.cid

import io.ipfs.cid.Cid
import io.ipfs.multibase.Multibase
import io.ipfs.multihash.Multihash
import org.bouncycastle.jcajce.provider.digest.Blake2b
import java.security.MessageDigest

class CidBuilder(
    private val type: Multihash.Type = Multihash.Type.blake2b_256,
    private val base: Multibase.Base = Multibase.Base.Base32
) {

    private val standardAlgorithms = mapOf(
        Multihash.Type.sha1 to "SHA-1",
        Multihash.Type.sha2_256 to "SHA-256",
        Multihash.Type.sha2_512 to "SHA-512",
    )

    private val supportedAlgorithms = setOf(
        Multihash.Type.sha1,
        Multihash.Type.sha2_256,
        Multihash.Type.sha2_512,
        Multihash.Type.blake2b_256
    )

    init {
        if (!supportedAlgorithms.contains(type)) {
            throw RuntimeException("Unsupported multihash type")
        }
    }

    fun build(data: ByteArray): String {
        val hash = getMessageDigest(type).apply { this.update(data) }.digest()
        val cid = Cid.buildCidV1(Cid.Codec.Raw, type, hash)
        val cidAsBytes = cid.toBytes()

        return Multibase.encode(base, cidAsBytes);
    }

    private fun getMessageDigest(type: Multihash.Type): MessageDigest {
        if (type == Multihash.Type.blake2b_256) {
            return Blake2b.Blake2b256()
        }

        return MessageDigest.getInstance(standardAlgorithms[type])
    }
}
