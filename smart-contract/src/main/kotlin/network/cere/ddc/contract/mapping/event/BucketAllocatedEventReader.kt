package network.cere.ddc.contract.mapping.event

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import network.cere.ddc.contract.model.event.BucketAllocatedEvent

object BucketAllocatedEventReader : AbstractEventReader<BucketAllocatedEvent>() {
    override val index: Int = 1

    override fun parse(reader: ScaleCodecReader) = BucketAllocatedEvent(
        bucketId = reader.readUint32(),
        clusterId = reader.readUint32()
    )
}