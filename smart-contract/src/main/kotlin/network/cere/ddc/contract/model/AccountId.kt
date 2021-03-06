package network.cere.ddc.contract.model

import io.emeraldpay.polkaj.ss58.SS58Codec
import io.emeraldpay.polkaj.ss58.SS58Type
import network.cere.ddc.core.extension.hexToBytes

@JvmInline
value class AccountId(val pubKey: ByteArray) {
    override fun toString(): String {
        return SS58Codec.getInstance().encode(SS58Type.Network.SUBSTRATE, pubKey)
    }

    constructor(pubKeyHex: String) : this(pubKeyHex.hexToBytes())
}