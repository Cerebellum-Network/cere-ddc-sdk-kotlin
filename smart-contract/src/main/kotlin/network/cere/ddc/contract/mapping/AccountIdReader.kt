package network.cere.ddc.contract.mapping

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleReader
import network.cere.ddc.contract.model.AccountId

object AccountIdReader : ScaleReader<AccountId> {
    override fun read(reader: ScaleCodecReader) = AccountId(reader.readUint256())
}