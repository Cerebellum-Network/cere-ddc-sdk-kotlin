package network.cere.ddc.contract.mapping.event

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import network.cere.ddc.contract.blockchain.mapping.BalanceScale
import network.cere.ddc.contract.mapping.AccountIdScale
import network.cere.ddc.contract.model.event.DepositEvent

object DepositEventReader : AbstractEventReader<DepositEvent>() {
    override val index: Int = 5

    override fun parse(reader: ScaleCodecReader) = DepositEvent(
        accountId = reader.read(AccountIdScale),
        value = reader.read(BalanceScale)
    )
}