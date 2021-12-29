package network.cere.ddc.core.extension

import org.komputing.khex.extensions.clean0xPrefix
import org.komputing.khex.extensions.hexToByteArray
import org.komputing.khex.extensions.prepend0xPrefix
import org.komputing.khex.extensions.toHexString
import org.komputing.khex.model.HexString
import java.security.MessageDigest

fun ByteArray.sha256(): String {
    return MessageDigest.getInstance("SHA-256").digest(this).toHex(false)
}

fun String.hexToBytes(): ByteArray = HexString(this).hexToByteArray()

fun ByteArray.toHex(withPrefix: Boolean = true): String =
    if (withPrefix) this.toHexString() else this.toHexString("")

fun String.hexPrefix(withPrefix: Boolean = true) = HexString(this)
    .let { if (withPrefix) it.prepend0xPrefix() else it.clean0xPrefix() }
    .string