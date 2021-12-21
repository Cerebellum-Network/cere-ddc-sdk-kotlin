package network.cere.ddc.core.extension

import org.komputing.khex.extensions.hexToByteArray
import org.komputing.khex.extensions.toNoPrefixHexString
import org.komputing.khex.model.HexString
import java.security.MessageDigest

fun ByteArray.sha256(): String {
    return MessageDigest.getInstance("SHA-256").digest(this).toNoPrefixHexString()
}

fun String.hexToBytes(): ByteArray = HexString(this).hexToByteArray()