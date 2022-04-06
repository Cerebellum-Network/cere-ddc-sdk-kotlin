package network.cere.ddc.contract.mapping

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleReader
import network.cere.ddc.contract.blockchain.mapping.UInt64Reader
import network.cere.ddc.contract.model.DealStatus
import java.time.Instant
import java.time.ZoneId

object DealStatusReader : ScaleReader<DealStatus> {

    override fun read(reader: ScaleCodecReader) = DealStatus(
        nodeId = reader.readUint32(),
        extimatedRentEnd = Instant.ofEpochMilli(reader.read(UInt64Reader).toLong()).atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    )
}