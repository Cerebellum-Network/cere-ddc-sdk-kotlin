package network.cere.ddc.contract.mapping.event

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import network.cere.ddc.contract.mapping.AccountIdScale
import network.cere.ddc.contract.model.event.BucketCreatedEvent

object BucketCreatedEventReader : AbstractEventReader<BucketCreatedEvent>() {
    override val index: Int = 0

    override fun parse(reader: ScaleCodecReader) = BucketCreatedEvent(
        bucketId = reader.readUint32(),
        ownerId = reader.read(AccountIdScale)
    )
}