package network.cere.ddc.contract.mapping

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleReader
import io.emeraldpay.polkaj.scale.reader.ListReader
import network.cere.ddc.contract.model.BucketStatus

object BucketStatusReader : ScaleReader<BucketStatus> {

    private val dealStatusesReader = ListReader(DealStatusReader)
    private val writerIdsReader = ListReader(AccountIdScale)

    override fun read(reader: ScaleCodecReader) = BucketStatus(
        bucketId = reader.readUint32(),
        bucket = reader.read(BucketReader),
        writerIds = reader.read(writerIdsReader),
        dealStatuses = reader.read(dealStatusesReader)
    )

    private object BucketReader : ScaleReader<BucketStatus.Bucket> {

        val listLongReader = ListReader(ScaleCodecReader.UINT32)

        override fun read(reader: ScaleCodecReader) = BucketStatus.Bucket(
            ownerId = reader.read(AccountIdScale),
            clusterIds = reader.read(listLongReader),
            dealIds = reader.read(listLongReader),
            bucketParams = reader.readString()
        )
    }
}
