package network.cere.ddc.contract.mapping

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleReader
import io.emeraldpay.polkaj.scale.reader.ListReader
import network.cere.ddc.contract.model.Bucket


object BucketReader : ScaleReader<Bucket> {

    private val listLongReader = ListReader(ScaleCodecReader.UINT32)

    override fun read(reader: ScaleCodecReader) = Bucket(
        ownerId = reader.read(AccountIdScale),
        clusterIds = reader.read(listLongReader),
        dealIds = reader.read(listLongReader),
        bucketParams = reader.readString()
    )
}
