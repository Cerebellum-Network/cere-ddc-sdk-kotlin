package network.cere.ddc.contract.mapping.event

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import network.cere.ddc.contract.blockchain.mapping.BalanceScale
import network.cere.ddc.contract.mapping.AccountIdScale
import network.cere.ddc.contract.model.event.NodeCreatedEvent

object NodeCreatedEventReader : AbstractEventReader<NodeCreatedEvent>() {
    override val index: Int = 4

    override fun parse(reader: ScaleCodecReader) = NodeCreatedEvent(
        nodeId = reader.readUint32(),
        providerId = reader.read(AccountIdScale),
        rentPerMonth = reader.read(BalanceScale),
        nodeParams = reader.readString()
    )
}