package network.cere.ddc.core.extension

import org.bouncycastle.util.encoders.Hex
import java.security.MessageDigest

const val HEX_PREFIX = "0x"

fun ByteArray.sha256(): String {
    return MessageDigest.getInstance("SHA-256").digest(this).toHex(false)
}

fun String.hexToBytes(): ByteArray = Hex.decode(this.removePrefix(HEX_PREFIX))

fun ByteArray.toHex(withPrefix: Boolean = true): String {
    val hex = String(Hex.encode(this))
    return if (withPrefix) """$HEX_PREFIX$hex""" else hex
}