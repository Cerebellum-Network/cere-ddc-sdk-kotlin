package network.cere.ddc.contract.mapping.event

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import network.cere.ddc.contract.mapping.AccountIdScale
import network.cere.ddc.contract.model.event.ClusterCreatedEvent

object ClusterCreatedEventReader : AbstractEventReader<ClusterCreatedEvent>() {
    override val index: Int = 2

    override fun parse(reader: ScaleCodecReader) = ClusterCreatedEvent(
        clusterId = reader.readUint32(),
        manager = reader.read(AccountIdScale),
        clusterParams = reader.readString()
    )
}