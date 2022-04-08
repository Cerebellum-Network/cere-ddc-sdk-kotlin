package network.cere.ddc.contract.mapping.response

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleReader
import network.cere.ddc.contract.blockchain.mapping.BalanceScale
import network.cere.ddc.contract.model.response.Account

object AccountReader : ScaleReader<Account> {
    override fun read(reader: ScaleCodecReader) = Account(
        deposit = reader.read(BalanceScale),
        payableSchedule = reader.read(ScheduleReader)
    )
}