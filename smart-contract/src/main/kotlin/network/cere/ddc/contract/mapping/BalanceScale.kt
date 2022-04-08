package network.cere.ddc.contract.blockchain.mapping

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.emeraldpay.polkaj.scale.ScaleReader
import io.emeraldpay.polkaj.scale.ScaleWriter
import network.cere.ddc.contract.model.Balance

object BalanceScale : ScaleReader<Balance>, ScaleWriter<Balance> {

    override fun read(reader: ScaleCodecReader) = Balance(reader.readUint128())

    override fun write(writer: ScaleCodecWriter, balance: Balance) {
        writer.writeUint128(balance.value)
    }
}