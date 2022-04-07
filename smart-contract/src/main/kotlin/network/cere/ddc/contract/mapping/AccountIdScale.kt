package network.cere.ddc.contract.mapping

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.emeraldpay.polkaj.scale.ScaleReader
import io.emeraldpay.polkaj.scale.ScaleWriter
import network.cere.ddc.contract.model.AccountId

object AccountIdScale : ScaleReader<AccountId>, ScaleWriter<AccountId> {

    override fun read(reader: ScaleCodecReader) = AccountId(reader.readUint256())

    override fun write(writer: ScaleCodecWriter, acc: AccountId) {
        writer.writeUint256(acc.pubKey)
    }
}