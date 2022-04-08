package network.cere.ddc.contract.mapping.response

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleReader
import network.cere.ddc.contract.blockchain.mapping.BalanceScale
import network.cere.ddc.contract.model.Schedule

object ScheduleReader : ScaleReader<Schedule> {

    override fun read(reader: ScaleCodecReader) = Schedule(
        rate = reader.read(BalanceScale),
        offset = reader.read(BalanceScale),
    )
}