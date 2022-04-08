package network.cere.ddc.contract.mapping.event

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import network.cere.ddc.contract.model.event.ClusterNodeReplacedEvent

object ClusterNodeReplacedEventReader : AbstractEventReader<ClusterNodeReplacedEvent>() {
    override val index: Int = 3

    override fun parse(reader: ScaleCodecReader) = ClusterNodeReplacedEvent(
        clusterId = reader.readUint32(),
        nodeId = reader.readUint32(),
        positionIndex = reader.readUint32(),
    )
}