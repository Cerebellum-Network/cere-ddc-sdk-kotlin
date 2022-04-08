package network.cere.ddc.contract.mapping.response

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleReader
import io.emeraldpay.polkaj.scale.reader.ListReader
import network.cere.ddc.contract.blockchain.mapping.reader.UInt64Reader
import network.cere.ddc.contract.mapping.AccountIdScale
import network.cere.ddc.contract.model.response.BucketStatus
import java.time.Instant
import java.time.ZoneId

object BucketStatusReader : ScaleReader<BucketStatus> {

    private val writerIdsReader = ListReader(AccountIdScale)

    override fun read(reader: ScaleCodecReader) = BucketStatus(
        bucketId = reader.readUint32(),
        bucket = reader.read(BucketReader),
        params = reader.readString(),
        writerIds = reader.read(writerIdsReader),
        rentCoveredUntil = Instant.ofEpochMilli(reader.read(UInt64Reader).toLong()).atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    )

    private object BucketReader : ScaleReader<BucketStatus.Bucket> {

        override fun read(reader: ScaleCodecReader) = BucketStatus.Bucket(
            ownerId = reader.read(AccountIdScale),
            clusterId = reader.readUint32(),
            flow = reader.read(FlowReader),
            resourceReserved = reader.readUint32()
        )
    }

    private object FlowReader : ScaleReader<BucketStatus.Flow> {

        override fun read(reader: ScaleCodecReader) = BucketStatus.Flow(
            from = reader.read(AccountIdScale),
            schedule = reader.read(ScheduleReader)
        )
    }

}
